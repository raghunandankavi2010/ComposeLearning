package com.example.composelearning.animcompose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleCallingRoute(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Calling Animation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        GoogleCallingScreenAnimation(
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun GoogleCallingScreenAnimation(
    modifier: Modifier = Modifier,
    isSwipeUpToAnswer: Boolean = true, // Toggle false if you want it to point downward for decline
    onAnswer: () -> Unit = {}
) {
    // 1. Create the infinite loop controller
    val infiniteTransition = rememberInfiniteTransition(label = "CallingScreenLoop")

    // 2. FAB Bounce Animation (Y-axis translation)
    val fabBounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSwipeUpToAnswer) -24f else 24f, // Distance of bounce in pixels/dp bounds
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FabBounce"
    )

    // Tracks whether the call has been answered via tap or swipe-up gesture
    var answered by remember { mutableStateOf(false) }

    fun answerCall() {
        if (!answered) {
            answered = true
            onAnswer()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Dark Material background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Layout order changes dynamically depending on direction
            if (isSwipeUpToAnswer) {
                ArrowIndicatorTrack(infiniteTransition = infiniteTransition, arrowUp = true)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // The Animated Floating Action Button.
            // Answers on tap, or on a swipe-up gesture when isSwipeUpToAnswer is enabled.
            IconButton(
                onClick = { answerCall() },
                modifier = Modifier
                    .testTag("AnswerCallButton")
                    .size(72.dp)
                    .offset { IntOffset(0, fabBounceOffset.dp.roundToPx()) }
                    .background(Color(0xFF1A73E8), shape = CircleShape)
                    .then(
                        if (isSwipeUpToAnswer) {
                            Modifier.swipeUpToAnswer(onSwipeUp = { answerCall() })
                        } else {
                            Modifier
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Answer Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (!isSwipeUpToAnswer) {
                Spacer(modifier = Modifier.height(24.dp))
                ArrowIndicatorTrack(infiniteTransition = infiniteTransition, arrowUp = false)
            }
        }

        // Visible confirmation once the call is answered
        if (answered) {
            Text(
                text = "Call Connected",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

/**
 * Detects an upward swipe and invokes [onSwipeUp] once the accumulated vertical
 * drag passes the threshold (negative Y == upward on screen).
 */
private fun Modifier.swipeUpToAnswer(
    swipeThresholdPx: Float = 120f,
    onSwipeUp: () -> Unit
): Modifier = this.pointerInput(Unit) {
    var totalDragY = 0f
    var triggered = false
    detectVerticalDragGestures(
        onDragStart = {
            totalDragY = 0f
            triggered = false
        },
        onVerticalDrag = { change, dragAmount ->
            change.consume()
            totalDragY += dragAmount
            // dragAmount is negative when moving up; fire as soon as the threshold is crossed
            if (!triggered && totalDragY <= -swipeThresholdPx) {
                triggered = true
                onSwipeUp()
            }
        },
        onDragEnd = {
            // Fallback for fast flings where the per-step total never crossed mid-drag
            if (!triggered && abs(totalDragY) >= swipeThresholdPx && totalDragY < 0f) {
                onSwipeUp()
            }
        }
    )
}

@Composable
fun ArrowIndicatorTrack(
    infiniteTransition: InfiniteTransition,
    arrowUp: Boolean
) {
    // We create a master progress variable loop from 0.0 to 1.0
    // to easily stagger child items manually using simple offsets
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ArrowTrackProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((-8).dp) // Tightly stack the chevrons
    ) {
        // Render 3 fading arrows stacked vertically
        // Index 2, 1, 0 reverses or maintains order based on direction
        val arrowIndices = if (arrowUp) listOf(0, 1, 2) else listOf(2, 1, 0)

        arrowIndices.forEach { index ->
            // Stagger each arrow's cycle by 15% intervals
            val delayFactor = index * 0.15f
            val individualProgress = (animationProgress - delayFactor).let { progress ->
                if (progress < 0f) progress + 1f else progress // Wraps around clean
            }

            // Derive customized opacity and slide offset from the single loop timeline
            val alpha = calculateArrowAlpha(individualProgress)
            val slideY = calculateArrowSlide(individualProgress, arrowUp)

            Icon(
                imageVector = if (arrowUp) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .offset { IntOffset(0, slideY.dp.roundToPx()) }
                    .alpha(alpha)
            )
        }
    }
}

// Math logic to calculate smooth fade-in -> fade-out ranges
private fun calculateArrowAlpha(progress: Float): Float = when {
    progress < 0.2f -> progress / 0.2f

    // Quick fade in
    progress < 0.6f -> 1f - ((progress - 0.2f) / 0.4f)

    // Smooth fade out
    else -> 0f // Invisible for remainder of window frame
}

// Math logic to transform loop progress into subtle physical directional shifts
private fun calculateArrowSlide(progress: Float, movingUp: Boolean): Float {
    val distance = 16f // Total distance travel in dp bounds
    val directionalMultiplier = if (movingUp) -1f else 1f
    return (progress * distance * directionalMultiplier)
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun GoogleCallingAnimationPreview_Light() {
    MaterialTheme {
        GoogleCallingScreenAnimation()
    }
}

@Preview(showBackground = true, name = "Dark Mode", backgroundColor = 0xFF121212)
@Composable
private fun GoogleCallingAnimationPreview_Dark() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        GoogleCallingScreenAnimation()
    }
}

@Preview(showBackground = true, name = "Decline Mode")
@Composable
private fun GoogleCallingAnimationPreview_Decline() {
    MaterialTheme {
        GoogleCallingScreenAnimation(isSwipeUpToAnswer = false)
    }
}
