package com.example.composelearning.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class BarEntry(
    val label: String,
    val values: List<Float>,
)

enum class BarMode { Grouped, Stacked }

@Immutable
data class BarChartSpec(
    val mode: BarMode = BarMode.Grouped,
    val cornerRadius: Dp = 6.dp,
    val barSpacing: Dp = 6.dp,
    val groupSpacing: Dp = 18.dp,
    val maxBarWidth: Dp = 36.dp,
    val animate: Boolean = true,
    val yAxis: ChartAxis = ChartAxis(),
    val xAxis: ChartAxis = ChartAxis(),
    val leftAxisGutter: Dp = 40.dp,
    val rightAxisGutter: Dp = 12.dp,
    val topAxisGutter: Dp = 14.dp,
    val bottomAxisGutter: Dp = 28.dp,
    val gradient: Boolean = true,
)

/**
 * Customizable bar chart supporting grouped and stacked modes.
 *
 * Each [BarEntry] is one category on the x-axis; its `values` list maps to series colors from the
 * theme palette. [BarMode.Grouped] places bars side-by-side within a category; [BarMode.Stacked]
 * piles them on top of each other.
 *
 * The drawing path:
 *   1. Compute y-range from data (or [spec.yAxis] override).
 *   2. Walk each category. For grouped: lay out N bars side by side within the slot width.
 *      For stacked: a single bar whose height is the sum of values, painted bottom-up.
 *   3. Animate bar heights as a single shared progress factor multiplied into each bar's height.
 */
@Composable
fun BarChart(
    entries: List<BarEntry>,
    seriesLabels: List<String>,
    modifier: Modifier = Modifier,
    spec: BarChartSpec = BarChartSpec(),
    theme: ChartTheme = ChartDefaults.theme(),
    onBarSelected: ((categoryIndex: Int, seriesIndex: Int) -> Unit)? = null,
) {
    if (entries.isEmpty()) return
    val measurer = rememberTextMeasurer()
    val seriesCount = seriesLabels.size
    val yRange = remember(entries, spec.mode) {
        val values = when (spec.mode) {
            BarMode.Grouped -> entries.flatMap { it.values }
            BarMode.Stacked -> entries.map { it.values.sum() }
        }
        val hi = (values.maxOrNull() ?: 1f)
        AxisRange(min = 0f, max = hi).autoNice(spec.yAxis.tickCount)
    }
    val progress by rememberChartProgress(entries.hashCode(), initial = if (spec.animate) 0f else 1f)

    var selected by remember(entries) { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier.pointerInput(entries, spec) {
            detectTapGestures { tap ->
                val plot = barPlotRect(size.width.toFloat(), size.height.toFloat(), spec, this)
                val cat = (((tap.x - plot.left) / (plot.width / entries.size)).toInt())
                    .coerceIn(0, entries.lastIndex)
                val seriesIndex = if (spec.mode == BarMode.Grouped) {
                    val slotWidth = plot.width / entries.size
                    val xInSlot = tap.x - plot.left - cat * slotWidth
                    ((xInSlot / (slotWidth / seriesCount)).toInt()).coerceIn(0, seriesCount - 1)
                } else 0
                selected = cat to seriesIndex
                onBarSelected?.invoke(cat, seriesIndex)
            }
        }
    ) {
        drawBarChart(entries, seriesLabels, yRange, spec, theme, progress, selected, measurer)
    }
}

private fun DrawScope.drawBarChart(
    entries: List<BarEntry>,
    seriesLabels: List<String>,
    yRange: AxisRange,
    spec: BarChartSpec,
    theme: ChartTheme,
    progress: Float,
    selected: Pair<Int, Int>?,
    measurer: TextMeasurer,
) {
    val plot = Rect(
        left = spec.leftAxisGutter.toPx(),
        top = spec.topAxisGutter.toPx(),
        right = size.width - spec.rightAxisGutter.toPx(),
        bottom = size.height - spec.bottomAxisGutter.toPx(),
    )

    // Y grid + labels
    val ticks = niceTicks(yRange, spec.yAxis.tickCount)
    if (spec.yAxis.showGridLines) {
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, plot.bottom..plot.top)
            drawLine(
                color = theme.gridColor,
                start = Offset(plot.left, y),
                end = Offset(plot.right, y),
                strokeWidth = theme.gridLineWidth.toPx(),
            )
            if (spec.yAxis.showLabels) {
                val layout = measurer.measure(spec.yAxis.labelFormatter(v), style = theme.axisLabelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        plot.left - layout.size.width - spec.yAxis.labelGap.toPx(),
                        y - layout.size.height / 2f,
                    ),
                )
            }
        }
    }

    drawLine(
        color = theme.axisColor,
        start = Offset(plot.left, plot.bottom),
        end = Offset(plot.right, plot.bottom),
        strokeWidth = theme.axisLineWidth.toPx(),
    )

    val slotWidth = plot.width / entries.size
    val seriesCount = seriesLabels.size.coerceAtLeast(1)

    entries.forEachIndexed { catIdx, entry ->
        val slotLeft = plot.left + catIdx * slotWidth
        val slotCenter = slotLeft + slotWidth / 2f

        when (spec.mode) {
            BarMode.Grouped -> {
                val groupGap = spec.groupSpacing.toPx()
                val barGap = spec.barSpacing.toPx()
                val maxBarPx = spec.maxBarWidth.toPx()
                val availableWidth = slotWidth - groupGap
                val barWidth = ((availableWidth - barGap * (seriesCount - 1)) / seriesCount)
                    .coerceAtMost(maxBarPx)
                val totalBarsWidth = barWidth * seriesCount + barGap * (seriesCount - 1)
                val firstBarLeft = slotCenter - totalBarsWidth / 2f
                entry.values.forEachIndexed { sIdx, v ->
                    val color = theme.palette[sIdx % theme.palette.size]
                    val x = firstBarLeft + sIdx * (barWidth + barGap)
                    val barTop = lerpRange(v, yRange, plot.bottom..plot.top)
                    val animatedTop = plot.bottom - (plot.bottom - barTop) * progress
                    val highlighted = selected?.first == catIdx && selected.second == sIdx
                    drawBar(
                        topLeft = Offset(x, animatedTop),
                        size = Size(barWidth, plot.bottom - animatedTop),
                        color = if (highlighted) color else color.copy(alpha = 0.85f),
                        cornerRadius = spec.cornerRadius.toPx(),
                        gradient = spec.gradient,
                    )
                }
            }
            BarMode.Stacked -> {
                val barWidth = (slotWidth - spec.groupSpacing.toPx()).coerceAtMost(spec.maxBarWidth.toPx())
                val barLeft = slotCenter - barWidth / 2f
                var cumulative = 0f
                entry.values.forEachIndexed { sIdx, v ->
                    val color = theme.palette[sIdx % theme.palette.size]
                    val bottomVal = cumulative
                    cumulative += v
                    val topVal = cumulative
                    val bottomY = lerpRange(bottomVal, yRange, plot.bottom..plot.top)
                    val topY = lerpRange(topVal, yRange, plot.bottom..plot.top)
                    val animatedTopY = bottomY - (bottomY - topY) * progress
                    drawBar(
                        topLeft = Offset(barLeft, animatedTopY),
                        size = Size(barWidth, bottomY - animatedTopY),
                        color = color,
                        cornerRadius = if (sIdx == entry.values.lastIndex) spec.cornerRadius.toPx() else 0f,
                        gradient = spec.gradient,
                    )
                }
            }
        }

        if (spec.xAxis.show && spec.xAxis.showLabels) {
            val layout = measurer.measure(entry.label, style = theme.axisLabelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(slotCenter - layout.size.width / 2f, plot.bottom + spec.xAxis.labelGap.toPx()),
            )
        }
    }
}

private fun DrawScope.drawBar(
    topLeft: Offset,
    size: Size,
    color: Color,
    cornerRadius: Float,
    gradient: Boolean,
) {
    if (size.height <= 0f) return
    val brush = if (gradient) {
        Brush.verticalGradient(
            colors = listOf(color, color.copy(alpha = 0.7f)),
            startY = topLeft.y,
            endY = topLeft.y + size.height,
        )
    } else null
    if (brush != null) {
        drawRoundRect(
            brush = brush,
            topLeft = topLeft,
            size = size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        )
    } else {
        drawRoundRect(
            color = color,
            topLeft = topLeft,
            size = size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        )
    }
}

private fun barPlotRect(
    width: Float,
    height: Float,
    spec: BarChartSpec,
    density: androidx.compose.ui.unit.Density,
): Rect = with(density) {
    Rect(
        left = spec.leftAxisGutter.toPx(),
        top = spec.topAxisGutter.toPx(),
        right = width - spec.rightAxisGutter.toPx(),
        bottom = height - spec.bottomAxisGutter.toPx(),
    )
}