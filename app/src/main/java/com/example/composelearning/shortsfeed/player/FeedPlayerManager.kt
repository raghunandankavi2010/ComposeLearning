package com.example.composelearning.shortsfeed.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import com.example.composelearning.shortsfeed.data.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

/** Playback state exposed to the presentation layer (player internals stay here). */
data class FeedPlaybackState(
    val activeVideoId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    /** width/height of the decoded video; 0 until the first frame's format is known. */
    val videoAspectRatio: Float = 0f,
    val errorMessage: String? = null,
)

/**
 * Owns the ONE [ExoPlayer] shared by every page of the feed ("Shared Active Player") **plus**
 * a Media3 [DefaultPreloadManager] that warms upcoming items for instant first frames.
 *
 * Preloading strategy — Instagram Reels "adjacent" (per the Media3 PreloadManager blog):
 * the manager prepares the source, selects tracks, and loads the first
 * [PRELOAD_DURATION_MS] of every item within ±[PRELOAD_WINDOW] of the current page, ranked
 * by distance from [DefaultPreloadManager.setCurrentPlayingIndex]. Far items return `null`
 * (not preloaded), capping memory. Playback then reuses the manager's already-warm
 * [androidx.media3.exoplayer.source.MediaSource] instead of preparing from scratch.
 *
 * This replaces the previous hand-rolled `CacheWriter` pre-cache: the manager shares the
 * player's load control / bandwidth meter / renderers (built from the same builder), preloads
 * multiple items in parallel, and self-prioritizes — far less code, fewer foot-guns.
 *
 * The on-disk [VideoFeedCache] is kept as the upstream of the media-source factory, so
 * preloaded bytes also persist across screen open/close (the manager only holds RAM).
 *
 * Threading: all [Player] calls happen on the main thread (ViewModel + composables). The
 * manager runs its own preload looper internally.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class FeedPlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val cacheDataSourceFactory = VideoFeedCache.dataSourceFactory(appContext)

    private val _playbackState = MutableStateFlow(FeedPlaybackState())
    val playbackState: StateFlow<FeedPlaybackState> = _playbackState.asStateFlow()

    /** Feed index -> MediaItem, kept parallel to the items already added to the manager. */
    private val mediaItems = mutableListOf<MediaItem>()
    private var currentIndex = -1

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
                (videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height
            }
            _playbackState.update { it.copy(videoAspectRatio = ratio) }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.update { it.copy(errorMessage = error.errorCodeName) }
        }
    }

    /**
     * Decides how much of each item to preload, by distance from the current page. Returns
     * `null` for items outside the window so the manager evicts/never-loads them.
     */
    private class AdjacentPreloadControl : TargetPreloadStatusControl<Int, DefaultPreloadManager.PreloadStatus> {
        @Volatile var currentPlayingIndex = 0

        override fun getTargetPreloadStatus(rankingData: Int): DefaultPreloadManager.PreloadStatus {
            if (abs(rankingData - currentPlayingIndex) > PRELOAD_WINDOW) {
                return DefaultPreloadManager.PreloadStatus.PRELOAD_STATUS_NOT_PRELOADED
            }
            // Prepare + select tracks + load the first PRELOAD_DURATION_MS for an instant start.
            return DefaultPreloadManager.PreloadStatus.specifiedRangeLoaded(PRELOAD_DURATION_MS)
        }
    }

    private val preloadControl = AdjacentPreloadControl()

    // Route loads through the disk cache; small back-buffer so the current clip doesn't
    // starve preloading of the next ones.
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ 5_000,
            /* maxBufferMs = */ 20_000,
            /* bufferForPlaybackMs = */ 1_000,
            /* bufferForPlaybackAfterRebufferMs = */ 2_000
        )
        .build()

    private val preloadManagerBuilder = DefaultPreloadManager.Builder(appContext, preloadControl)
        .setDataSourceFactory(cacheDataSourceFactory)
        .setLoadControl(loadControl)

    private val preloadManager: DefaultPreloadManager = preloadManagerBuilder.build()

    /** The single player, built from the SAME builder so it shares the manager's resources. */
    val player: ExoPlayer = preloadManagerBuilder.buildExoPlayer(
        ExoPlayer.Builder(appContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setHandleAudioBecomingNoisy(true)
    ).apply {
        repeatMode = Player.REPEAT_MODE_ONE // Shorts loop until swiped away.
        addListener(playerListener)
    }

    /**
     * Registers any newly appended [items] with the preload manager (rankingData = feed index)
     * and re-evaluates the preload window. Idempotent: only items beyond what's already added
     * are registered, so paging is cheap.
     */
    fun setItems(items: List<VideoItem>) {
        if (items.size <= mediaItems.size) return
        for (i in mediaItems.size until items.size) {
            val mediaItem = MediaItem.Builder()
                .setUri(items[i].videoUrl)
                .setMediaId(items[i].id)
                .build()
            mediaItems += mediaItem
            preloadManager.add(mediaItem, i) // rankingData = index
        }
        preloadManager.invalidate()
    }

    /** Plays the settled [index], reusing the manager's pre-warmed source for an instant start. */
    fun playIndex(index: Int) {
        val mediaItem = mediaItems.getOrNull(index) ?: return
        if (currentIndex == index && _playbackState.value.errorMessage == null) {
            player.playWhenReady = true
            return
        }
        currentIndex = index

        // Tell the manager what's playing so it re-prioritizes the window around `index`.
        preloadControl.currentPlayingIndex = index
        preloadManager.setCurrentPlayingIndex(index)
        preloadManager.invalidate()

        _playbackState.update { FeedPlaybackState(activeVideoId = mediaItem.mediaId) }

        // Prefer the preloaded source; fall back to a fresh one if it wasn't in the window.
        val source = preloadManager.getMediaSource(mediaItem)
        if (source != null) player.setMediaSource(source) else player.setMediaItem(mediaItem)
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
        val index = currentIndex
        currentIndex = -1 // Force a full re-prepare of the same item.
        playIndex(index)
    }

    /** 0..1 fraction of the current clip, for the progress hairline. */
    fun progressFraction(): Float {
        val duration = player.duration
        if (duration <= 0) return 0f
        return (player.currentPosition.toFloat() / duration).coerceIn(0f, 1f)
    }

    /** Releases the player and the preload manager (frees all preloaded sources). Call once. */
    fun release() {
        player.removeListener(playerListener)
        player.release()
        preloadManager.release()
    }

    companion object {
        /** Preload items within ±this many pages of the current one (Instagram "adjacent"). */
        private const val PRELOAD_WINDOW = 2

        /** Load this much of each in-window item — enough for an instant first frame. */
        private const val PRELOAD_DURATION_MS = 3_000L
    }
}
