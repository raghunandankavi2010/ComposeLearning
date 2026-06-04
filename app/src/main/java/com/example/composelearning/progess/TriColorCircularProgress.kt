package com.example.composelearning.progess

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.LocalAnimationsEnabled
import kotlin.math.floor

/**
 * A premium, high-performance Indeterminate Circular Progress Indicator whose arc is
 * split into THREE equal solid-colour segments instead of a single sweep gradient.
 *
 * This is a sibling of [PremiumCircularProgressIndicator]: the head/tail grow-shrink
 * cycle, the continuous (never-resetting) clock, and the carried-forward tail term are
 * all identical, so the motion is exactly the same "premium" feel. The only difference
 * is rendering: whatever the current total progress sweep is (e.g. 75°), it is divided
 * into 3 parts (75 / 3 = 25° each) and each third is drawn in its own colour.
 *
 * Because the split is proportional, the three bands grow and shrink together as the arc
 * stretches and contracts — the colour boundaries always sit at 1/3 and 2/3 of the arc.
 *
 * @param colors exactly three colours, one per segment (head -> tail order).
 */
@Composable
fun TriColorCircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 8.dp,
    colors: List<Color> = listOf(
        Color(0xFF6A11CB), // segment 1 (head)
        Color(0xFF2575FC), // segment 2 (middle)
        Color(0xFF00C2A8), // segment 3 (tail)
    ),
    rotationPeriodMillis: Int = 2000,
    cyclePeriodMillis: Int = 1200,
) {
    require(colors.size == 3) { "TriColorCircularProgressIndicator needs exactly 3 colors" }

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
        cyclePeriodMillis) {
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
        val arcSize = size.minDimension - strokeWidthPx
        val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
        val arcDimen = Size(arcSize, arcSize)
        val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

        // Split the total progress sweep into 3 equal segments (e.g. 75° -> 25° each).
        val segmentSweep = sweep / 3f
        // The tail (start) of the whole arc.
        val baseStart = -90f + rotation + tailAngle

        // Draw tail segment first, then middle, then head last so the head's rounded
        // cap sits on top — this keeps the leading edge clean while the arc stretches.
        for (i in 2 downTo 0) {
            drawArc(
                color = colors[i],
                startAngle = baseStart + segmentSweep * i,
                sweepAngle = segmentSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcDimen,
                style = stroke,
            )
        }
    }
}

@Preview(name = "Tri-color Light", showBackground = true)
@Composable
fun PreviewTriColorLight() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        TriColorCircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 10.dp,
        )
    }
}

@Preview(name = "Tri-color Dark", showBackground = true)
@Composable
fun PreviewTriColorDark() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        TriColorCircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            strokeWidth = 12.dp,
            colors = listOf(
                Color(0xFFFF00D4),
                Color(0xFF8A2BE2),
                Color(0xFF00DDFF),
            ),
        )
    }
}