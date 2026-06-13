package com.example.composelearning.progress

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlin.math.floor

/**
 * A premium, high-performance Indeterminate Circular Progress Indicator.
 *
 * Architecture:
 * - Driven by a single continuously-accumulating clock via [withFrameNanos] (the same
 *   technique as [SmoothProgressBar]) so motion never snaps or "restarts".
 * - The grow/shrink (head/tail) cycle's contraction is *carried forward* between cycles:
 *   when the tail offset wraps from its max back to 0, the accumulated [completed]-cycle
 *   term increases by exactly the same amount, so the tail angle stays continuous across
 *   the seam. This is what the original two-transition version got wrong — its head/tail
 *   `RepeatMode.Restart` jumped the tail ~240° backwards once per cycle.
 * - All angle state is read inside the [Canvas] DrawScope to keep recomposition out of
 *   the hot path.
 */
@Composable
fun PremiumCircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    brush: Brush = Brush.sweepGradient(
        0.0f to Color(0xFF6A11CB),
        0.45f to Color(0xFF2575FC),
        0.55f to Color(0xFF2575FC),
        1.0f to Color(0xFF6A11CB)
    ),
    rotationPeriodMillis: Int = 2000,
    cyclePeriodMillis: Int = 1200
) {
    val minSweep = 30f
    val maxSweep = 270f
    val stretch = maxSweep - minSweep // how much the arc grows/shrinks each cycle

    val animationsEnabled = LocalAnimationsEnabled.current

    // Continuous, never-reset angle state. Wrapped to [0, 360) only for drawing.
    var rotation by remember { mutableFloatStateOf(0f) }
    var tailAngle by remember { mutableFloatStateOf(0f) }
    var sweep by remember { mutableFloatStateOf(minSweep) }

    LaunchedEffect(
        animationsEnabled,
        rotationPeriodMillis,
        cyclePeriodMillis
    ) {
        if (!animationsEnabled) return@LaunchedEffect
        var startNanos = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (startNanos == 0L) startNanos = now
            // Double keeps sub-frame precision even after the app runs for hours.
            val elapsedMs = (now - startNanos) / 1_000_000.0

            // Constant base spin.
            rotation = ((elapsedMs / rotationPeriodMillis) * 360.0 % 360.0).toFloat()

            // Grow/shrink cycle. `p` is the position within the current cycle [0,1);
            // `completed` is how many full cycles have elapsed.
            val totalCycles = elapsedMs / cyclePeriodMillis
            val completed = floor(totalCycles)
            val p = (totalCycles - completed).toFloat()

            // First half: head races ahead (arc grows). Second half: tail catches up
            // (arc shrinks). Eased for momentum, matching the original feel.
            val headDelta: Float
            val tailDelta: Float
            if (p < 0.5f) {
                headDelta = FastOutSlowInEasing.transform(p * 2f) * stretch
                tailDelta = 0f
            } else {
                headDelta = stretch
                tailDelta = FastOutSlowInEasing.transform((p - 0.5f) * 2f) * stretch
            }

            // Carrying `completed * stretch` makes the tail continuous: at a cycle seam
            // tailDelta drops by `stretch` exactly as `completed` adds `stretch`.
            tailAngle = ((completed.toFloat() * stretch + tailDelta) % 360f)
            sweep = minSweep + headDelta - tailDelta
        }
    }

    Canvas(
        modifier = modifier
            .size(48.dp) // Default size, override via modifier
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        // The diameter of the arc center line should be total size minus stroke width
        // to ensure the stroke (which spreads in both directions) stays within bounds.
        val arcSize = size.minDimension - strokeWidthPx
        val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

        // B. Draw the Progress Arc
        drawArc(
            brush = brush,
            startAngle = -90f + rotation + tailAngle,
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
private fun PreviewProgressLight() {
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
private fun PreviewProgressDark() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        PremiumCircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 12.dp,
            brush = Brush.sweepGradient(
                listOf(Color(0xFFFF00D4), Color(0xFF00DDFF), Color(0xFFFF00D4))
            )
        )
    }
}
