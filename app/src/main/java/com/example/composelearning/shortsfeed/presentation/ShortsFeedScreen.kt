package com.example.composelearning.shortsfeed.presentation

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.example.composelearning.shortsfeed.data.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * Endless TikTok/Shorts-style vertical video feed.
 *
 * Route -> Screen -> Content hoisting: this Route owns the ViewModel and lifecycle
 * wiring; everything below is stateless and driven by [ShortsFeedUiState] + events.
 */
@Composable
fun ShortsFeedRoute() {
    val viewModel: ShortsFeedViewModel = viewModel(factory = ShortsFeedViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Instant pause when the app is backgrounded or the screen is covered; seamless
    // resume on return (unless the user had explicitly paused).
    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(ShortsFeedEvent.AppForegrounded)
        onPauseOrDispose {
            viewModel.onEvent(ShortsFeedEvent.AppBackgrounded)
        }
    }

    ShortsFeedScreen(
        state = uiState,
        player = viewModel.player,
        progressFraction = viewModel::progressFraction,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ShortsFeedScreen(
    state: ShortsFeedUiState,
    player: Player,
    progressFraction: () -> Float,
    onEvent: (ShortsFeedEvent) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.videos.size })

    // The single source of truth for "which video is active": only a fully SETTLED
    // page (zero half-screen visibility) starts playback — never targetPage, which
    // fires mid-fling.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page -> onEvent(ShortsFeedEvent.PageSettled(page)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Pre-compose one page on each side so the next page's UI (scrim, text,
            // placeholder) is ready the instant the fling starts — but NOT its video.
            beyondViewportPageCount = 1,
            // Hard snap: a fling moves exactly one page, so a clip is never skipped
            // accidentally and the pager always settles flush on a boundary.
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1)
            ),
            key = { state.videos[it].id }
        ) { page ->
            VideoPage(
                video = state.videos[page],
                isActive = page == pagerState.settledPage,
                state = state,
                player = player,
                progressFraction = progressFraction,
                onEvent = onEvent
            )
        }

        if (state.isLoadingFirstPage) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}

/** One full-screen page: video surface (active page only) + overlays. */
@Composable
private fun VideoPage(
    video: VideoItem,
    isActive: Boolean,
    state: ShortsFeedUiState,
    player: Player,
    progressFraction: () -> Float,
    onEvent: (ShortsFeedEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // The surface can overflow horizontally/vertically for aspect-fill; clip
            // the overflow to this page's bounds.
            .clipToBounds()
            .pointerInput(isActive) {
                detectTapGestures {
                    if (isActive) onEvent(ShortsFeedEvent.TogglePlayPause)
                }
            }
    ) {
        // The SurfaceView exists ONLY on the settled page. Off-screen pages render the
        // placeholder, so there is exactly one video surface alive at any moment and a
        // recycled page can never flash a stale frame from a previous clip.
        if (isActive) {
            VideoSurface(
                player = player,
                aspectRatio = state.playback.videoAspectRatio,
                modifier = Modifier.fillMaxSize()
            )
        }

        val showPlaceholder = !isActive ||
            state.playback.videoAspectRatio == 0f ||
            state.playback.isBuffering
        if (showPlaceholder && state.playback.errorMessage == null) {
            VideoPlaceholder(showSpinner = isActive)
        }

        if (isActive && state.userPaused) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Paused — tap to resume",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp)
            )
        }

        if (isActive && state.playback.errorMessage != null) {
            PlaybackError(
                message = state.playback.errorMessage,
                onRetry = { onEvent(ShortsFeedEvent.RetryPlayback) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        VideoMetadataOverlay(
            video = video,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        )

        if (isActive) {
            PlaybackProgressBar(
                isPlaying = state.playback.isPlaying,
                progressFraction = progressFraction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

/**
 * The actual video output: a raw [SurfaceView] (hardware overlay, cheapest possible
 * composition path) hosted in [AndroidView] and center-crop scaled to fill the page.
 */
@Composable
private fun VideoSurface(
    player: Player,
    aspectRatio: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surfaceView = remember { SurfaceView(context) }

    AndroidView(
        factory = { surfaceView },
        modifier = modifier.cropToFill(aspectRatio)
    )

    // Proactive cleanup: when this page stops being the settled page (or the screen
    // is popped), the surface is detached synchronously so the shared player never
    // renders into a dead/recycled surface and no frame leaks across pages.
    DisposableEffect(player, surfaceView) {
        player.setVideoSurfaceView(surfaceView)
        onDispose {
            // No-ops if the player has already attached to the next page's surface.
            player.clearVideoSurfaceView(surfaceView)
        }
    }
}

/**
 * Center-crop ("aspect fill") layout: measures the child at the smallest size that
 * covers the container while preserving [aspectRatio], then centers it. The overflow
 * is clipped by the page's `clipToBounds`. With ratio 0 (unknown yet) it just fills.
 */
private fun Modifier.cropToFill(aspectRatio: Float): Modifier = layout { measurable, constraints ->
    val containerWidth = constraints.maxWidth
    val containerHeight = constraints.maxHeight
    val (childWidth, childHeight) = if (aspectRatio <= 0f) {
        containerWidth to containerHeight
    } else {
        val containerAspect = containerWidth.toFloat() / containerHeight
        if (aspectRatio > containerAspect) {
            // Video is wider than the viewport: match height, crop the sides.
            (containerHeight * aspectRatio).roundToInt() to containerHeight
        } else {
            // Video is taller/narrower: match width, crop top and bottom.
            containerWidth to (containerWidth / aspectRatio).roundToInt()
        }
    }
    val placeable = measurable.measure(Constraints.fixed(childWidth, childHeight))
    layout(containerWidth, containerHeight) {
        placeable.place(
            x = (containerWidth - childWidth) / 2,
            y = (containerHeight - childHeight) / 2
        )
    }
}

@Composable
private fun VideoPlaceholder(showSpinner: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF0F0F1A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showSpinner) {
            CircularProgressIndicator(color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun PlaybackError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Playback failed",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text("Retry")
        }
    }
}

@Composable
private fun VideoMetadataOverlay(video: VideoItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                )
            )
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 72.dp, bottom = 24.dp, top = 48.dp)
    ) {
        Text(
            text = video.author,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = video.title,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = video.description,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Thin progress hairline polled at 4 Hz — only while this page is active and playing. */
@Composable
private fun PlaybackProgressBar(
    isPlaying: Boolean,
    progressFraction: () -> Float,
    modifier: Modifier = Modifier
) {
    val progress by produceState(initialValue = 0f, isPlaying) {
        while (true) {
            value = progressFraction()
            if (!isPlaying) return@produceState // Freeze at current value while paused.
            delay(250.milliseconds)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(2.dp)
                .background(Color.White)
        )
    }
}
