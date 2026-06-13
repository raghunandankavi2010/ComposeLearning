package com.example.composelearning.shortsfeed.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.composelearning.shortsfeed.data.VideoItem
import com.example.composelearning.shortsfeed.player.FeedPlayerManager.Companion.PRECACHE_BYTES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Playback state exposed to the presentation layer (player internals stay here). */
data class FeedPlaybackState(
    val activeVideoId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    /** width/height of the decoded video; 0 until the first frame's format is known. */
    val videoAspectRatio: Float = 0f,
    val errorMessage: String? = null
)

/**
 * Owns the ONE [ExoPlayer] instance shared by every page of the feed ("Shared Active
 * Player" strategy) plus the background pre-cache engine for the upcoming item.
 *
 * Why a single player instead of one per page: each ExoPlayer holds codec instances,
 * audio sessions and renderer threads — per-item players OOM and stutter on fling.
 * The pager only ever has one *audible, visible, settled* page, so one player that
 * hops between SurfaceViews is sufficient; "instant" starts come from the disk cache
 * having pre-warmed the next item's first bytes (see [precacheNext]).
 *
 * Threading: all [Player] calls must happen on the main thread (callers are the
 * ViewModel + composables, so this holds). Only the cache writer runs on IO.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class FeedPlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val cacheDataSourceFactory = VideoFeedCache.dataSourceFactory(appContext)

    private val _playbackState = MutableStateFlow(FeedPlaybackState())
    val playbackState: StateFlow<FeedPlaybackState> = _playbackState.asStateFlow()

    private var currentItem: VideoItem? = null

    // NOTE: declared before `player` — Kotlin initializes properties top-to-bottom and
    // the player's init block below registers this listener.
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.update { it.copy(isBuffering = state == Player.STATE_BUFFERING) }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            val ratio = if (videoSize.height == 0) {
                0f
            } else {
                videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
            }
            _playbackState.update { it.copy(videoAspectRatio = ratio) }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update { it.copy(errorMessage = error.errorCodeName) }
        }
    }

    val player: ExoPlayer = ExoPlayer.Builder(appContext)
        // Route ALL media loads through the cache so playback itself also fills it.
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        // Shorts tuning: small back-buffer, modest max buffer. Over-buffering the
        // current clip steals bandwidth from pre-caching the next one.
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */
                    5_000,
                    /* maxBufferMs = */
                    20_000,
                    /* bufferForPlaybackMs = */
                    1_000,
                    /* bufferForPlaybackAfterRebufferMs = */
                    2_000
                )
                .build()
        )
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build(),
            /* handleAudioFocus = */
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()
        .apply {
            repeatMode = Player.REPEAT_MODE_ONE // Shorts loop until swiped away.
            addListener(playerListener)
        }

    /** Starts (or restarts) playback of [item], replacing whatever was playing. */
    fun play(item: VideoItem) {
        if (currentItem?.id == item.id && _playbackState.value.errorMessage == null) {
            player.playWhenReady = true
            return
        }
        currentItem = item
        _playbackState.update {
            FeedPlaybackState(activeVideoId = item.id) // Fresh state for the new clip.
        }
        player.setMediaItem(MediaItem.fromUri(item.videoUrl))
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun resume() {
        player.playWhenReady = true
    }

    fun retry() {
        val item = currentItem ?: return
        currentItem = null // Force a full re-prepare.
        play(item)
    }

    /** 0..1 fraction of the current clip, for the progress hairline. */
    fun progressFraction(): Float {
        val duration = player.duration
        if (duration <= 0) return 0f
        return (player.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
    }

    // ── Pre-caching ─────────────────────────────────────────────────────────

    private val precacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var precacheJob: Job? = null
    private var activeCacheWriter: CacheWriter? = null

    /**
     * Silently warms the disk cache with the first [PRECACHE_BYTES] of [item] so that
     * when the user swipes to it, prepare() reads the moov atom + first GOPs from disk
     * instead of the network. Only one pre-cache runs at a time; swiping again cancels
     * the in-flight write (the partial spans are kept and reused).
     */
    fun precacheNext(item: VideoItem) {
        precacheJob?.cancel()
        activeCacheWriter?.cancel()

        if (VideoFeedCache.get(appContext).isCached(item.videoUrl, 0, PRECACHE_BYTES)) return

        precacheJob = precacheScope.launch {
            val dataSpec = DataSpec.Builder()
                .setUri(item.videoUrl)
                .setPosition(0)
                .setLength(PRECACHE_BYTES)
                .build()
            val writer = CacheWriter(
                cacheDataSourceFactory.createDataSource(),
                dataSpec,
                /* temporaryBuffer = */
                null,
                /* progressListener = */
                null
            )
            activeCacheWriter = writer
            try {
                writer.cache()
            } catch (_: Exception) {
                // Cancelled or network error — pre-caching is best-effort by design;
                // playback falls back to streaming and must never be affected.
            }
        }
    }

    /** Releases the player and stops any in-flight pre-cache. Call exactly once. */
    fun release() {
        precacheJob?.cancel()
        activeCacheWriter?.cancel()
        precacheScope.cancel()
        player.removeListener(playerListener)
        player.release()
    }

    companion object {
        /** ~3s of a 1080p MP4 — enough for instant first frame without hogging bandwidth. */
        private const val PRECACHE_BYTES = 3L * 1024 * 1024
    }
}
