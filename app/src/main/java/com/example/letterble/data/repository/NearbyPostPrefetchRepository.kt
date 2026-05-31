/**
 * NearbyPostPrefetchRepository.kt
 *
 * 役割:
 * - ポスト選択画面を開く前に、低精度位置で近隣ポスト候補を先読みする
 */
package com.example.letterble.data.repository

import android.location.Location
import com.example.letterble.data.datasource.location.CurrentLocationDataSource
import com.example.letterble.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class NearbyPostPrefetch(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val posts: List<Post>,
    val createdAtMillis: Long
) {
    fun isFresh(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis - createdAtMillis <= CACHE_TTL_MILLIS
    }

    fun distanceMetersTo(location: Location): Float {
        val cachedLocation = Location("nearby_post_prefetch").apply {
            latitude = this@NearbyPostPrefetch.latitude
            longitude = this@NearbyPostPrefetch.longitude
        }
        return cachedLocation.distanceTo(location)
    }

    companion object {
        const val CACHE_TTL_MILLIS = 5 * 60 * 1_000L
    }
}

class NearbyPostPrefetchRepository(
    private val currentLocationDataSource: CurrentLocationDataSource,
    private val postRepository: PostRepository
) {
    private val mutex = Mutex()
    private val _cachedPrefetch = MutableStateFlow<NearbyPostPrefetch?>(null)
    val cachedPrefetch: StateFlow<NearbyPostPrefetch?> = _cachedPrefetch.asStateFlow()

    suspend fun prefetchNearbyPosts(): NearbyPostPrefetch? = mutex.withLock {
        val cached = _cachedPrefetch.value
        if (cached != null && cached.isFresh()) {
            return@withLock cached
        }

        val location = currentLocationDataSource.getApproximateLocation() ?: return@withLock null
        val accuracyMeters = location.takeIf { it.hasAccuracy() }?.accuracy
        if (accuracyMeters != null && accuracyMeters > MAX_PREFETCH_ACCURACY_METERS) {
            return@withLock null
        }

        val posts = postRepository.getNearbyPosts(
            latitude = location.latitude,
            longitude = location.longitude
        )
        val prefetch = NearbyPostPrefetch(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = accuracyMeters,
            posts = posts,
            createdAtMillis = System.currentTimeMillis()
        )
        _cachedPrefetch.value = prefetch
        prefetch
    }

    fun clearCachedPrefetch() {
        _cachedPrefetch.value = null
    }

    companion object {
        const val MAX_PREFETCH_ACCURACY_METERS = 800f
        const val MAX_REUSE_DISTANCE_METERS = 250f
    }
}
