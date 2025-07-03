package com.example.composelearning.animcompose

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Constants for the animation, inspired by Material's CircularProgressIndicator
private const val DURATION_MILLIS = 1332
private const val NUM_SEGMENTS = 3
private const val SWEEP_ANGLE_PER_SEGMENT = 30f
private const val GAP_ANGLE = 5f // A small gap between segments
private const val START_ANGLE_OFFSET = -90f // Start at 12 o'clock

/**
 * An indeterminate circular progress indicator that mimics the standard Material behavior
 * but uses a "comet" of multiple colored segments.
 *
 * @param colors The list of colors for the segments. The number of segments is determined by this list's size.
 * @param strokeWidth The width of the progress bar's stroke.
 * @param modifier The modifier to be applied to the progress bar.
 * @param animationDuration The duration in milliseconds for one full animation cycle (grow and shrink).
 */
@Composable
fun MultiColorIndeterminateCircularProgressBar(
    colors: List<Color>,
    strokeWidth: Dp = 16.dp,
    modifier: Modifier = Modifier,
    animationDuration: Int = DURATION_MILLIS
) {
    val infiniteTransition = rememberInfiniteTransition(label = "multi-color progress")

    // 1. Animate the rotation of the entire group of arcs.
    // This gives the continuous spinning motion.
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    // 2. Animate the sweep angle. This creates the "grow and shrink" effect.
    // It starts small, grows to a max arc length, then shrinks back down.
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = animationDuration
                // Grow phase
                0f at 0 using FastOutSlowInEasing // Start with small sweep
                270f at animationDuration / 2 using FastOutLinearInEasing // Grow to 3/4 circle
                // Shrink phase (handled by how startAngle catches up)
                360f at animationDuration using FastOutLinearInEasing
            }
        ),
        label = "sweep"
    )

    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    }

    Canvas(modifier = modifier.size(200.dp)) {
        val diameterOffset = stroke.width / 2
        val arcSize = Size(size.minDimension - 2 * diameterOffset, size.minDimension - 2 * diameterOffset)
        val arcTopLeft = Offset(diameterOffset, diameterOffset)

        // Calculate the effective start and sweep angles from the animated values
        val currentStartAngle = (rotationAngle + sweepAngle) % 360f + START_ANGLE_OFFSET
        val currentSweepAngle = 270f * FastOutSlowInEasing.transform(
            (sweepAngle % 360f) / 360f
        ) - 270f * FastOutLinearInEasing.transform(
            ((sweepAngle - 180f).coerceAtLeast(0f) % 360f) / 360f
        )

        // 3. Draw each segment, but "clip" its sweep angle based on the animated sweep.
        val totalAnglePerSegment = SWEEP_ANGLE_PER_SEGMENT + GAP_ANGLE

        for (i in colors.indices) {
            val segmentStartAngle = currentStartAngle + (i * totalAnglePerSegment)

            // Calculate how much of this segment should be visible
            val segmentAngleInComet = i * totalAnglePerSegment

            // The sweep to draw for this specific segment
            val drawSweep = (currentSweepAngle - segmentAngleInComet)
                .coerceIn(0f, SWEEP_ANGLE_PER_SEGMENT)

            if (drawSweep > 0) {
                drawArc(
                    color = colors[i],
                    startAngle = segmentStartAngle,
                    sweepAngle = drawSweep,
                    useCenter = false,
                    style = stroke,
                    size = arcSize,
                    topLeft = arcTopLeft
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MultiColorIndeterminateCircularProgressBarPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        MultiColorIndeterminateCircularProgressBar(
            colors = listOf(
                Color(0xFFF44336), // Red
                Color(0xFFFFC107), // Amber
                Color(0xFF2196F3)  // Blue
            ),
            strokeWidth = 24.dp
        )
    }
}
