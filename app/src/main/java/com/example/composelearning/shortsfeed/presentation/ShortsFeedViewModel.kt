package com.example.composelearning.shortsfeed.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.common.Player
import com.example.composelearning.shortsfeed.data.VideoFeedRepository
import com.example.composelearning.shortsfeed.data.VideoItem
import com.example.composelearning.shortsfeed.player.FeedPlaybackState
import com.example.composelearning.shortsfeed.player.FeedPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Single immutable snapshot the UI renders from. */
data class ShortsFeedUiState(
    val videos: List<VideoItem> = emptyList(),
    val playback: FeedPlaybackState = FeedPlaybackState(),
    /** True when the user explicitly tapped to pause the settled page. */
    val userPaused: Boolean = false,
    val isLoadingFirstPage: Boolean = true
)

/** Everything the UI can do, funneled through one entry point. */
sealed interface ShortsFeedEvent {
    /** The pager finished snapping and [page] is fully settled in the viewport. */
    data class PageSettled(val page: Int) : ShortsFeedEvent

    /** Tap on the active video. */
    data object TogglePlayPause : ShortsFeedEvent

    /** Host lifecycle moved to RESUMED. */
    data object AppForegrounded : ShortsFeedEvent

    /** Host lifecycle left RESUMED (backgrounded, or screen covered/popped). */
    data object AppBackgrounded : ShortsFeedEvent

    /** Retry after a playback error on the settled page. */
    data object RetryPlayback : ShortsFeedEvent
}

class ShortsFeedViewModel(application: Application) : ViewModel() {

    private val repository = VideoFeedRepository()
    private val playerManager = FeedPlayerManager(application)

    /**
     * The shared player instance, exposed read-only for surface attachment and the
     * progress hairline. The UI never calls play/pause/setMediaItem on it directly —
     * all commands go through [onEvent].
     */
    val player: Player get() = playerManager.player

    private val videos = MutableStateFlow<List<VideoItem>>(emptyList())
    private val userPaused = MutableStateFlow(false)

    private var settledPage = 0
    private var nextPageToLoad = 0
    private var isLoadingPage = false

    val uiState: StateFlow<ShortsFeedUiState> =
        combine(videos, userPaused, playerManager.playbackState) { items, paused, playback ->
            ShortsFeedUiState(
                videos = items,
                playback = playback,
                userPaused = paused,
                isLoadingFirstPage = items.isEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShortsFeedUiState())

    init {
        loadNextPage()
    }

    fun onEvent(event: ShortsFeedEvent) {
        when (event) {
            is ShortsFeedEvent.PageSettled -> onPageSettled(event.page)
            ShortsFeedEvent.TogglePlayPause -> {
                val paused = !userPaused.value
                userPaused.value = paused
                if (paused) playerManager.pause() else playerManager.resume()
            }
            ShortsFeedEvent.AppForegrounded ->
                if (!userPaused.value) playerManager.resume()
            ShortsFeedEvent.AppBackgrounded ->
                playerManager.pause()
            ShortsFeedEvent.RetryPlayback ->
                playerManager.retry()
        }
    }

    fun progressFraction(): Float = playerManager.progressFraction()

    private fun onPageSettled(page: Int) {
        settledPage = page
        userPaused.value = false // A fresh page always starts playing.

        val items = videos.value
        items.getOrNull(page)?.let(playerManager::play)
        // Pre-buffer the NEXT clip silently while this one plays.
        items.getOrNull(page + 1)?.let(playerManager::precacheNext)

        if (page >= items.size - LOAD_AHEAD_THRESHOLD) loadNextPage()
    }

    private fun loadNextPage() {
        if (isLoadingPage) return
        isLoadingPage = true
        viewModelScope.launch {
            try {
                val wasEmpty = videos.value.isEmpty()
                val newItems = repository.loadPage(nextPageToLoad)
                nextPageToLoad++
                videos.value = videos.value + newItems
                // First page just arrived: the pager already settled on page 0 while
                // the list was empty, so kick playback off manually.
                if (wasEmpty) onPageSettled(settledPage)
            } finally {
                isLoadingPage = false
            }
        }
    }

    override fun onCleared() {
        // Screen popped from the back stack: kill audio/codecs immediately.
        playerManager.release()
    }

    companion object {
        /** Append the next page when the user is this many items from the end. */
        private const val LOAD_AHEAD_THRESHOLD = 4

        /** Manual factory — this project deliberately uses no DI framework. */
        val Factory = viewModelFactory {
            initializer {
                ShortsFeedViewModel(checkNotNull(this[APPLICATION_KEY]))
            }
        }
    }
}
