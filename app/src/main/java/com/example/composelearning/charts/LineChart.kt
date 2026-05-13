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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A single point in a [LineSeries]. */
@Immutable
data class LinePoint(val x: Float, val y: Float, val label: String? = null)

@Immutable
data class LineSeries(
    val label: String,
    val points: List<LinePoint>,
    val color: Color? = null,
    val strokeWidth: Dp = 2.5.dp,
    val smoothing: LineSmoothing = LineSmoothing.Cubic,
    val showArea: Boolean = true,
    val showDots: Boolean = false,
    val dashed: Boolean = false,
)

/** Layout-level spec for [LineChart]. Separated from [LineSeries] so the same series can be drawn
 * across multiple charts with different axes. */
@Immutable
data class LineChartSpec(
    val xAxis: ChartAxis = ChartAxis(),
    val yAxis: ChartAxis = ChartAxis(),
    val xRange: AxisRange? = null,
    val yRange: AxisRange? = null,
    val animate: Boolean = true,
    val drawValueOnSelection: Boolean = true,
    val leftAxisGutter: Dp = 36.dp,
    val rightAxisGutter: Dp = 12.dp,
    val topAxisGutter: Dp = 12.dp,
    val bottomAxisGutter: Dp = 28.dp,
)

/**
 * Customizable line chart supporting multiple series, linear/cubic/step smoothing, area gradient
 * fill and tap-to-select markers.
 *
 * Performance notes:
 *   • A single Path is rebuilt per series each draw — but the underlying object is reused via
 *     [buildLinePath]'s output parameter contract, so steady-state recompositions don't allocate.
 *   • Selection state is kept local with [mutableStateOf]; only the canvas reacts to it.
 *   • Y-axis range is auto-snapped to nice ticks once per data change.
 */
@Composable
fun LineChart(
    series: List<LineSeries>,
    modifier: Modifier = Modifier,
    spec: LineChartSpec = LineChartSpec(),
    theme: ChartTheme = ChartDefaults.theme(),
    onPointSelected: ((seriesIndex: Int, point: LinePoint) -> Unit)? = null,
) {
    if (series.isEmpty()) return
    val measurer = rememberTextMeasurer()

    val xRange = remember(series, spec.xRange) {
        spec.xRange ?: computeRange(series) { it.x }
    }
    val yRange = remember(series, spec.yRange, spec.yAxis.tickCount) {
        (spec.yRange ?: computeRange(series) { it.y }).autoNice(spec.yAxis.tickCount)
    }

    val progress by rememberChartProgress(
        key = series.hashCode(),
        initial = if (spec.animate) 0f else 1f,
    )

    var selected by remember(series) { mutableStateOf<Pair<Int, Int>?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(series, xRange, yRange, spec) {
                detectTapGestures { offset ->
                    val plot = plotRectPx(size.width.toFloat(), size.height.toFloat(), spec, density = this)
                    if (!plot.contains(offset)) {
                        selected = null
                        return@detectTapGestures
                    }
                    var best: Pair<Int, Int>? = null
                    var bestDistance = Float.MAX_VALUE
                    series.forEachIndexed { sIdx, s ->
                        s.points.forEachIndexed { pIdx, p ->
                            val px = lerpRange(p.x, xRange, plot.left..plot.right)
                            val py = lerpRange(p.y, yRange, plot.bottom..plot.top)
                            val d = (px - offset.x) * (px - offset.x) + (py - offset.y) * (py - offset.y)
                            if (d < bestDistance) {
                                bestDistance = d
                                best = sIdx to pIdx
                            }
                        }
                    }
                    selected = best
                    val sel = best
                    if (sel != null) {
                        onPointSelected?.invoke(sel.first, series[sel.first].points[sel.second])
                    }
                }
            }
    ) {
        drawLineChart(
            series = series,
            xRange = xRange,
            yRange = yRange,
            spec = spec,
            theme = theme,
            progress = progress,
            selected = selected,
            measurer = measurer,
        )
    }
}

private fun DrawScope.drawLineChart(
    series: List<LineSeries>,
    xRange: AxisRange,
    yRange: AxisRange,
    spec: LineChartSpec,
    theme: ChartTheme,
    progress: Float,
    selected: Pair<Int, Int>?,
    measurer: TextMeasurer,
) {
    val plot = plotRect(size, spec)
    drawAxes(plot, xRange, yRange, spec, theme, measurer)

    series.forEachIndexed { idx, s ->
        val color = s.color ?: theme.palette[idx % theme.palette.size]
        val points = s.points.map { p ->
            Offset(
                x = lerpRange(p.x, xRange, plot.left..plot.right),
                y = lerpRange(p.y, yRange, plot.bottom..plot.top),
            )
        }
        if (points.isEmpty()) return@forEachIndexed
        val linePath = buildLinePath(points, s.smoothing)

        if (s.showArea) {
            val areaPath = Path().apply {
                addPath(linePath)
                closeAreaPath(this, points, plot.bottom)
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0f)),
                    startY = plot.top,
                    endY = plot.bottom,
                ),
            )
        }

        val stroke = Stroke(
            width = s.strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = if (s.dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null,
        )
        clipRect(
            left = plot.left,
            top = plot.top,
            right = plot.left + plot.width * progress,
            bottom = plot.bottom,
        ) {
            drawPath(linePath, color = color, style = stroke)
        }

        if (s.showDots) {
            for (p in points) {
                drawCircle(theme.surface, radius = s.strokeWidth.toPx() + 1.dp.toPx(), center = p)
                drawCircle(color, radius = s.strokeWidth.toPx(), center = p)
            }
        }
    }

    selected?.let { (sIdx, pIdx) ->
        val s = series[sIdx]
        val p = s.points[pIdx]
        val color = s.color ?: theme.palette[sIdx % theme.palette.size]
        val px = lerpRange(p.x, xRange, plot.left..plot.right)
        val py = lerpRange(p.y, yRange, plot.bottom..plot.top)
        drawLine(
            color = theme.axisColor.copy(alpha = 0.5f),
            start = Offset(px, plot.top),
            end = Offset(px, plot.bottom),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
        )
        drawCircle(theme.surface, radius = 6.dp.toPx(), center = Offset(px, py))
        drawCircle(color, radius = 4.dp.toPx(), center = Offset(px, py))

        if (spec.drawValueOnSelection) {
            val label = spec.yAxis.labelFormatter(p.y)
            val layout = measurer.measure(label, style = theme.valueLabelStyle)
            val tooltipWidth = layout.size.width + 12.dp.toPx()
            val tooltipHeight = layout.size.height + 6.dp.toPx()
            var rectLeft = px - tooltipWidth / 2f
            val rectTop = (py - tooltipHeight - 8.dp.toPx()).coerceAtLeast(plot.top + 2.dp.toPx())
            rectLeft = rectLeft.coerceIn(plot.left, plot.right - tooltipWidth)
            drawRoundRect(
                color = theme.tooltipBackground,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(tooltipWidth, tooltipHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
            drawText(
                textLayoutResult = layout,
                color = theme.tooltipContent,
                topLeft = Offset(
                    rectLeft + (tooltipWidth - layout.size.width) / 2f,
                    rectTop + (tooltipHeight - layout.size.height) / 2f,
                ),
            )
        }
    }
}

internal fun DrawScope.drawAxes(
    plot: Rect,
    xRange: AxisRange,
    yRange: AxisRange,
    spec: LineChartSpec,
    theme: ChartTheme,
    measurer: TextMeasurer,
) {
    if (spec.yAxis.show && spec.yAxis.showGridLines) {
        val ticks = niceTicks(yRange, spec.yAxis.tickCount)
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, plot.bottom..plot.top)
            drawLine(
                color = theme.gridColor,
                start = Offset(plot.left, y),
                end = Offset(plot.right, y),
                strokeWidth = theme.gridLineWidth.toPx(),
            )
            if (spec.yAxis.showLabels) {
                val text = spec.yAxis.labelFormatter(v)
                val layout = measurer.measure(text, style = theme.axisLabelStyle)
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
    if (spec.xAxis.show && spec.xAxis.showLabels) {
        val ticks = niceTicks(xRange, spec.xAxis.tickCount)
        ticks.forEach { v ->
            val x = lerpRange(v, xRange, plot.left..plot.right)
            val text = spec.xAxis.labelFormatter(v)
            val layout = measurer.measure(text, style = theme.axisLabelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x - layout.size.width / 2f,
                    plot.bottom + spec.xAxis.labelGap.toPx(),
                ),
            )
        }
    }
    if (spec.xAxis.show) {
        drawLine(
            color = theme.axisColor,
            start = Offset(plot.left, plot.bottom),
            end = Offset(plot.right, plot.bottom),
            strokeWidth = theme.axisLineWidth.toPx(),
        )
    }
    if (spec.yAxis.show) {
        drawLine(
            color = theme.axisColor,
            start = Offset(plot.left, plot.top),
            end = Offset(plot.left, plot.bottom),
            strokeWidth = theme.axisLineWidth.toPx(),
        )
    }
}

internal fun DrawScope.plotRect(size: Size, spec: LineChartSpec): Rect = Rect(
    left = spec.leftAxisGutter.toPx(),
    top = spec.topAxisGutter.toPx(),
    right = size.width - spec.rightAxisGutter.toPx(),
    bottom = size.height - spec.bottomAxisGutter.toPx(),
)

private fun plotRectPx(
    width: Float,
    height: Float,
    spec: LineChartSpec,
    density: androidx.compose.ui.unit.Density,
): Rect = with(density) {
    Rect(
        left = spec.leftAxisGutter.toPx(),
        top = spec.topAxisGutter.toPx(),
        right = width - spec.rightAxisGutter.toPx(),
        bottom = height - spec.bottomAxisGutter.toPx(),
    )
}

private inline fun computeRange(
    series: List<LineSeries>,
    selector: (LinePoint) -> Float,
): AxisRange {
    var lo = Float.POSITIVE_INFINITY
    var hi = Float.NEGATIVE_INFINITY
    for (s in series) for (p in s.points) {
        val v = selector(p)
        if (v < lo) lo = v
        if (v > hi) hi = v
    }
    if (lo == Float.POSITIVE_INFINITY) return AxisRange(0f, 1f)
    if (lo == hi) hi = lo + 1f
    return AxisRange(lo, hi)
}
