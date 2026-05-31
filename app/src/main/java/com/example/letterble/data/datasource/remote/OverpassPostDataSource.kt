
/**
 * OverpassPostDataSource.kt
 *
 * 役割:
 * - Overpass API から実在する郵便ポスト候補を取得する
 */
package com.example.letterble.data.datasource.remote

import com.example.letterble.domain.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * OpenStreetMap の Overpass API から周辺ポストを取得するDataSource。
 */
class OverpassPostDataSource(
    private val endpointUrls: List<String> = DefaultEndpointUrls
) {
    constructor(endpointUrl: String) : this(listOf(endpointUrl))

    /**
     * 指定座標から半径 radiusMeters 以内の amenity=post_box / post_office を取得する。
     */
    suspend fun fetchNearbyPosts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<Post> = withContext(Dispatchers.IO) {
        val query = buildPostBoxQuery(latitude, longitude, radiusMeters)
        var lastFailure: IOException? = null

        for (endpointUrl in endpointUrls) {
            try {
                return@withContext requestPosts(
                    endpointUrl = endpointUrl,
                    query = query,
                    originLatitude = latitude,
                    originLongitude = longitude
                )
            } catch (exception: IOException) {
                lastFailure = exception
            }
        }

        throw lastFailure ?: IOException("Overpass API request failed")
    }

    private fun requestPosts(
        endpointUrl: String,
        query: String,
        originLatitude: Double,
        originLongitude: Double
    ): List<Post> {
        val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }

        try {
            val body = "data=${URLEncoder.encode(query, Charsets.UTF_8.name())}"
            connection.outputStream.use { outputStream ->
                outputStream.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorText = connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                // API障害やrate limitを「0件」と誤表示しないよう、失敗としてViewModelへ伝える。
                throw IOException(
                    "Overpass API request failed: HTTP $responseCode ${errorText.take(MAX_ERROR_BODY_LENGTH)}"
                )
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            return parsePosts(
                responseText = responseText,
                originLatitude = originLatitude,
                originLongitude = originLongitude
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun buildPostBoxQuery(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): String {
        return """
            [out:json][timeout:15];
            (
              nwr(around:$radiusMeters,$latitude,$longitude)["amenity"~"^(post_box|post_office)$"];
              nwr(around:$radiusMeters,$latitude,$longitude)["post_office"="post_partner"];
            );
            out body center;
        """.trimIndent()
    }

    private fun parsePosts(
        responseText: String,
        originLatitude: Double,
        originLongitude: Double
    ): List<Post> {
        val elements = JSONObject(responseText).optJSONArray("elements") ?: return emptyList()
        return buildList {
            for (index in 0 until elements.length()) {
                val element = elements.optJSONObject(index) ?: continue
                val id = element.optLong("id").takeIf { it != 0L } ?: continue
                val type = element.optString("type").takeIf { it.isNotBlank() } ?: "node"
                val center = element.optJSONObject("center")
                val latitude = element.optDouble("lat", center?.optDouble("lat", Double.NaN) ?: Double.NaN)
                val longitude = element.optDouble("lon", center?.optDouble("lon", Double.NaN) ?: Double.NaN)
                if (latitude.isNaN() || longitude.isNaN()) {
                    continue
                }

                val tags = element.optJSONObject("tags")
                val amenity = tags?.optString("amenity").orEmpty()
                val isPostPartner = tags.tag("post_office") == "post_partner"
                val defaultName = when {
                    amenity == "post_office" || isPostPartner -> "郵便局"
                    else -> "郵便ポスト"
                }
                val name = tags?.optString("name")?.takeIf { it.isNotBlank() } ?: defaultName
                val description = tags.toPostDescription(defaultName)
                add(
                    Post(
                        id = "$type/$id",
                        name = name,
                        latitude = latitude,
                        longitude = longitude,
                        description = description
                    )
                )
            }
        }.distinctBy { post ->
            post.id
        }.sortedBy { post ->
            distanceMeters(originLatitude, originLongitude, post.latitude, post.longitude)
        }
    }

    private fun distanceMeters(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double
    ): Double {
        val fromLatRad = Math.toRadians(fromLatitude)
        val toLatRad = Math.toRadians(toLatitude)
        val latDelta = Math.toRadians(toLatitude - fromLatitude)
        val lonDelta = Math.toRadians(toLongitude - fromLongitude)
        val a = sin(latDelta / 2).pow(2) +
            cos(fromLatRad) * cos(toLatRad) * sin(lonDelta / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EarthRadiusMeters * c
    }

    private fun JSONObject?.toPostDescription(defaultName: String): String {
        if (this == null) {
            return defaultName
        }

        return listOfNotNull(
            addressText(),
            tag("operator")?.let { operator -> "運営: $operator" },
            tag("collection_times")?.let { collectionTimes -> "集荷: $collectionTimes" },
            tag("ref")?.let { ref -> "番号: $ref" }
        ).joinToString("\n").ifBlank { defaultName }
    }

    private fun JSONObject.addressText(): String? {
        tag("addr:full")?.let { return it }

        val addressParts = listOfNotNull(
            tag("addr:province"),
            tag("addr:city"),
            tag("addr:ward"),
            tag("addr:suburb"),
            tag("addr:quarter"),
            tag("addr:neighbourhood"),
            tag("addr:block_number"),
            tag("addr:housenumber")
        )
        return addressParts.joinToString("").takeIf { it.isNotBlank() }
    }

    private fun JSONObject?.tag(key: String): String? {
        return this?.optString(key)?.takeIf { it.isNotBlank() }
    }

    private companion object {
        val DefaultEndpointUrls = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.private.coffee/api/interpreter",
            "https://overpass.osm.ch/api/interpreter"
        )
        const val MAX_ERROR_BODY_LENGTH = 200
        const val EarthRadiusMeters = 6_371_000.0
    }
}
