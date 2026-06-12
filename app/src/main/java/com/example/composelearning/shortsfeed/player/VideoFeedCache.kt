package com.example.composelearning.shortsfeed.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Process-wide singleton holding the Media3 disk cache for the shorts feed.
 *
 * MUST be a singleton: Media3 throws if two [SimpleCache] instances are opened on the
 * same directory. The cache is intentionally never released — it lives for the process
 * lifetime so cached spans survive screen open/close, and the OS reclaims the lock on
 * process death. Eviction is handled by the LRU evictor, not by releasing the cache.
 */
@androidx.annotation.OptIn(UnstableApi::class)
object VideoFeedCache {

    private const val CACHE_DIR = "shorts_video_cache"
    private const val MAX_CACHE_BYTES = 256L * 1024 * 1024 // 256 MB LRU budget

    @Volatile
    private var cache: SimpleCache? = null

    fun get(context: Context): SimpleCache =
        cache ?: synchronized(this) {
            cache ?: SimpleCache(
                File(context.applicationContext.cacheDir, CACHE_DIR),
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES),
                StandaloneDatabaseProvider(context.applicationContext)
            ).also { cache = it }
        }

    /**
     * DataSource factory used by BOTH playback and pre-caching, so bytes written by the
     * pre-cacher are read back by the player (same cache, same default cache-key = URI).
     */
    fun dataSourceFactory(context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(get(context))
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory()
                    .setUserAgent(Util.getUserAgent(context, "ComposeLearning"))
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(10_000)
                    .setReadTimeoutMs(10_000)
            )
            // A corrupt/locked cache must degrade to plain network, never break playback.
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}
