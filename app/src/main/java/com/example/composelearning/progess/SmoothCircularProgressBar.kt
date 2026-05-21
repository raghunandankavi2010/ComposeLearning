package com.example.composelearning.progess

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Compose port of castorflex/SmoothCircularProgressBar (fr.castorflex.android.circularprogressbar).
 *
 * Indeterminate Material-style spinner. Two animations run concurrently:
 *  - Global rotation: linear 0..360, period [rotationDurationMs] / [rotationSpeed].
 *  - Sweep oscillation: each phase ([sweepDurationMs] / [sweepSpeed]) eases the sweep angle
 *    between [minSweepAngle] and [maxSweepAngle]. On appearing the leading edge advances; on
 *    disappearing the trailing edge anchors and the start advances. When a disappearing phase
 *    ends the color advances to the next in [colors].
 */
@Composable
fun SmoothCircularProgressBar(
    modifier: Modifier = Modifier,
    colors: List<Color> = DefaultSmoothColors,
    strokeWidth: Dp = 4.dp,
    minSweepAngle: Float = 20f,
    maxSweepAngle: Float = 300f,
    rotationSpeed: Float = 1f,
    sweepSpeed: Float = 1f,
    rounded: Boolean = true,
    sweepEasing: Easing = FastOutSlowInEasing,
    size: Dp = 48.dp,
    rotationDurationMs: Int = 2000,
    sweepDurationMs: Int = 600,
) {
    require(colors.isNotEmpty())
    require(maxSweepAngle in (minSweepAngle + 1f)..360f)

    var rotation by remember { mutableFloatStateOf(0f) }
    var offsetAngle by remember { mutableFloatStateOf(0f) }
    var phase by remember { mutableFloatStateOf(0f) }
    var appearing by remember { mutableStateOf(true) }
    var colorIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(rotationSpeed, sweepSpeed, colors.size, minSweepAngle, maxSweepAngle) {
        var last = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (last != 0L) {
                val dtMs = (now - last) / 1_000_000f
                rotation = (rotation + dtMs / rotationDurationMs * 360f * rotationSpeed) % 360f
                phase += dtMs / sweepDurationMs * sweepSpeed
                while (phase >= 1f) {
                    phase -= 1f
                    if (appearing) {
                        appearing = false
                        offsetAngle += minSweepAngle
                    } else {
                        appearing = true
                        offsetAngle += 360f - maxSweepAngle
                        colorIndex = (colorIndex + 1) % colors.size
                    }
                }
            }
            last = now
        }
    }

    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }

    Canvas(modifier = modifier.size(size)) {
        val eased = sweepEasing.transform(phase)
        val sweep: Float
        val startAngle: Float
        if (appearing) {
            sweep = minSweepAngle + eased * (maxSweepAngle - minSweepAngle)
            startAngle = rotation - offsetAngle
        } else {
            sweep = maxSweepAngle - eased * (maxSweepAngle - minSweepAngle)
            // Anchor trailing edge: start moves forward as sweep shrinks.
            startAngle = rotation - offsetAngle + (maxSweepAngle - sweep)
        }

        val inset = strokePx / 2f
        val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
        drawArc(
            color = colors[colorIndex],
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(
                width = strokePx,
                cap = if (rounded) StrokeCap.Round else StrokeCap.Butt,
            ),
        )
    }
}