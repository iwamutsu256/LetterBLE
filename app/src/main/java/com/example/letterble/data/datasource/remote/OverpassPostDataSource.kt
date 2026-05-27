
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

/**
 * OpenStreetMap の Overpass API から周辺ポストを取得するDataSource。
 */
class OverpassPostDataSource(
    private val endpointUrl: String = "https://overpass-api.de/api/interpreter"
) {
    /**
     * 指定座標から半径 radiusMeters 以内の amenity=post_box を取得する。
     */
    suspend fun fetchNearbyPosts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): List<Post> = withContext(Dispatchers.IO) {
        val query = buildPostBoxQuery(latitude, longitude, radiusMeters)
        val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
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
            parsePosts(responseText)
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
            [out:json][timeout:10];
            node(around:$radiusMeters,$latitude,$longitude)["amenity"="post_box"];
            out body;
        """.trimIndent()
    }

    private fun parsePosts(responseText: String): List<Post> {
        val elements = JSONObject(responseText).optJSONArray("elements") ?: return emptyList()
        return buildList {
            for (index in 0 until elements.length()) {
                val element = elements.optJSONObject(index) ?: continue
                val id = element.optLong("id").takeIf { it != 0L }?.toString() ?: continue
                val latitude = element.optDouble("lat", Double.NaN)
                val longitude = element.optDouble("lon", Double.NaN)
                if (latitude.isNaN() || longitude.isNaN()) {
                    continue
                }

                val tags = element.optJSONObject("tags")
                val name = tags?.optString("name")?.takeIf { it.isNotBlank() } ?: "郵便ポスト"
                add(
                    Post(
                        id = id,
                        name = name,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            }
        }
    }

    private companion object {
        const val MAX_ERROR_BODY_LENGTH = 200
    }
}
