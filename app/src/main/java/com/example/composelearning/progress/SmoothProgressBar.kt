package com.example.composelearning.progress

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlin.math.abs

/**
 * Compose port of castorflex/SmoothProgressBar (indeterminate horizontal).
 *
 * The bar shows N equal-width "sections" of colored stroke that slide continuously and re-emerge
 * with the next color from [colors] cycling through. An [easing] curve (default x*x — the same as
 * Android's AccelerateInterpolator) warps section widths as they cross the bar.
 */
@Composable
fun SmoothProgressBar(
    modifier: Modifier = Modifier,
    colors: List<Color> = DefaultSmoothColors,
    sectionsCount: Int = 4,
    speed: Float = 1f,
    strokeWidth: Dp = 4.dp,
    separatorLength: Dp = 4.dp,
    mirrorMode: Boolean = false,
    reversed: Boolean = false,
    easing: Easing = AccelerateEasing
) {
    require(sectionsCount >= 1) { "sectionsCount must be >= 1" }
    require(colors.isNotEmpty()) { "colors must not be empty" }

    val maxOffset = 1f / sectionsCount
    var offset by remember { mutableFloatStateOf(0f) }
    var colorIndex by remember { mutableIntStateOf(0) }

    val animationsEnabled = LocalAnimationsEnabled.current

    LaunchedEffect(speed, sectionsCount, colors.size, animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect
        var lastFrameNanos = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (lastFrameNanos != 0L) {
                val frames = (now - lastFrameNanos) / FRAME_NANOS
                var next = offset + OFFSET_PER_FRAME * speed * frames
                while (next >= maxOffset) {
                    next -= maxOffset
                    colorIndex = (colorIndex + 1) % colors.size
                }
                offset = next
            }
            lastFrameNanos = now
        }
    }

    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    val separatorPx = with(density) { separatorLength.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth)
    ) {
        if (reversed) {
            scale(scaleX = -1f, scaleY = 1f) {
                drawSections(
                    offset = offset,
                    startColorIndex = colorIndex,
                    sectionsCount = sectionsCount,
                    colors = colors,
                    strokePx = strokePx,
                    separatorPx = separatorPx,
                    mirrorMode = mirrorMode,
                    easing = easing
                )
            }
        } else {
            drawSections(
                offset = offset,
                startColorIndex = colorIndex,
                sectionsCount = sectionsCount,
                colors = colors,
                strokePx = strokePx,
                separatorPx = separatorPx,
                mirrorMode = mirrorMode,
                easing = easing
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSections(
    offset: Float,
    startColorIndex: Int,
    sectionsCount: Int,
    colors: List<Color>,
    strokePx: Float,
    separatorPx: Float,
    mirrorMode: Boolean,
    easing: Easing
) {
    val totalWidth = size.width
    val centerY = size.height / 2f
    val drawWidth = if (mirrorMode) totalWidth / 2f else totalWidth
    val xSectionWidth = 1f / sectionsCount

    var prevValue = 0f
    var idx = startColorIndex

    for (i in 0..sectionsCount) {
        val xOffset = xSectionWidth * i + offset
        val prev = maxOf(0f, xOffset - xSectionWidth)
        val ratio = abs(easing.transform(prev) - easing.transform(minOf(xOffset, 1f)))
        val sectionWidth = (drawWidth * ratio).toInt().toFloat()
        val spaceLen = minOf(sectionWidth, separatorPx)
        val drawLen = maxOf(0f, sectionWidth - spaceLen)
        val end = prevValue + drawLen

        if (end > prevValue) {
            val color = colors[idx]
            drawLine(
                color = color,
                start = Offset(prevValue, centerY),
                end = Offset(end, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Butt
            )
            if (mirrorMode) {
                drawLine(
                    color = color,
                    start = Offset(totalWidth - prevValue, centerY),
                    end = Offset(totalWidth - end, centerY),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Butt
                )
            }
        }
        prevValue = end + spaceLen
        idx = (idx + 1) % colors.size
    }
}

private const val OFFSET_PER_FRAME = 0.01f
private const val FRAME_NANOS = 16_666_667f

private val AccelerateEasing: Easing = Easing { it * it }

/** Holo blue — the single default color used by the original library when none configured. */
val DefaultSmoothColors: List<Color> = listOf(Color(0xFF33B5E5))
