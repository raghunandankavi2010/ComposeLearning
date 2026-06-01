package com.example.composelearning.progess

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A premium, high-performance Indeterminate Circular Progress Indicator.
 *
 * Architecture:
 * - Uses [rememberInfiniteTransition] for high-frequency animation.
 * - Reads [State] values strictly inside the [Canvas] DrawScope to bypass composition
 *   and keep the UI thread free for layout.
 * - Simulates fluid momentum via asymmetric start/end angle acceleration.
 */
@Composable
fun PremiumCircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    trackColor: Color = Color.LightGray.copy(alpha = 0.2f),
    brush: Brush = Brush.sweepGradient(
        0.0f to Color(0xFF6A11CB),
        0.45f to Color(0xFF2575FC),
        0.55f to Color(0xFF2575FC),
        1.0f to Color(0xFF6A11CB)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PremiumProgress")

    // 1. Global Rotation: Constant spin (360 degrees every 2 seconds)
    val globalRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlobalRotation"
    )

    // 2. Head/Tail Progress (0.0 to 1.0): Drives the stretch/contract logic
    val animationProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimationProgress"
    )

    Canvas(
        modifier = modifier
            .size(48.dp) // Default size, override via modifier
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        // The diameter of the arc center line should be total size minus stroke width
        // to ensure the stroke (which spreads in both directions) stays within bounds.
        val arcSize = size.minDimension - strokeWidthPx
        val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

        // A. Draw the Static Track
        drawCircle(
            color = trackColor,
            style = Stroke(width = strokeWidthPx),
            radius = arcSize / 2,
            center = center
        )

        // B. Calculate the Dynamic Arc
        val progress = animationProgress.value
        val rotation = globalRotation.value
        
        val minSweep = 30f
        val maxSweep = 270f
        
        val sweep: Float
        val startOffset: Float

        if (progress < 0.5f) {
            val p = progress * 2f
            sweep = minSweep + (maxSweep - minSweep) * p
            startOffset = 0f
        } else {
            val p = (progress - 0.5f) * 2f
            sweep = maxSweep - (maxSweep - minSweep) * p
            startOffset = (maxSweep - minSweep) * p
        }

        val finalStartAngle = -90f + rotation + startOffset

        // C. Draw the Progress Arc
        drawArc(
            brush = brush,
            startAngle = finalStartAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcSize, arcSize),
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewProgressLight() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        PremiumCircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 10.dp
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun PreviewProgressDark() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        PremiumCircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 12.dp,
            trackColor = Color.White.copy(alpha = 0.1f),
            brush = Brush.sweepGradient(
                listOf(Color(0xFFFF00D4), Color(0xFF00DDFF), Color(0xFFFF00D4))
            )
        )
    }
}
