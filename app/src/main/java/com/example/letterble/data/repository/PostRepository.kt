/**
 * PostRepository.kt
 *
 * 役割:
 * - ポストAPIからデータ取得
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.remote.OverpassPostDataSource
import com.example.letterble.domain.model.Post

/**
 * ポスト検索DataSourceを上位層へ公開するRepository。
 */
class PostRepository(
    private val overpassPostDataSource: OverpassPostDataSource
) {
    /**
     * 指定座標から1km以内のポスト候補を取得する。
     */
    suspend fun getNearbyPosts(
        latitude: Double,
        longitude: Double
    ): List<Post> {
        return overpassPostDataSource.fetchNearbyPosts(
            latitude = latitude,
            longitude = longitude,
            radiusMeters = NEARBY_POST_RADIUS_METERS
        )
    }

    companion object {
        private const val NEARBY_POST_RADIUS_METERS = 1_000
    }
}
