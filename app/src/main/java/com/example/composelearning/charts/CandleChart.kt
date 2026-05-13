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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One OHLC bar. */
@Immutable
data class Candle(
    val label: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float = 0f,
) {
    val isBullish: Boolean get() = close >= open
}

@Immutable
data class CandleChartSpec(
    val bullishColor: Color = Color(0xFF26A69A),
    val bearishColor: Color = Color(0xFFEF5350),
    val wickWidth: Dp = 1.5.dp,
    val bodyCornerRadius: Dp = 1.dp,
    val candleSpacing: Dp = 4.dp,
    val maxCandleWidth: Dp = 14.dp,
    val showVolume: Boolean = false,
    val volumeFraction: Float = 0.18f,
    val yAxis: ChartAxis = ChartAxis(),
    val xAxis: ChartAxis = ChartAxis(showLabels = true, tickCount = 5),
    val animate: Boolean = true,
    val leftAxisGutter: Dp = 44.dp,
    val rightAxisGutter: Dp = 12.dp,
    val topAxisGutter: Dp = 12.dp,
    val bottomAxisGutter: Dp = 28.dp,
)

/**
 * Customizable OHLC candle chart with optional volume strip beneath.
 *
 * Drawing strategy:
 *   • Wick = thin vertical line from low to high (animated linearly out of low).
 *   • Body = rounded rectangle between open and close, filled with bullish/bearish color.
 *   • Volume strip = optional bars below the candle plot, sharing the same x positions.
 *
 * No path objects are allocated per candle — we use primitive `drawLine` / `drawRoundRect` calls
 * for predictable performance across long series.
 */
@Composable
fun CandleChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    spec: CandleChartSpec = CandleChartSpec(),
    theme: ChartTheme = ChartDefaults.theme(),
    onCandleSelected: ((index: Int) -> Unit)? = null,
) {
    if (candles.isEmpty()) return
    val measurer = rememberTextMeasurer()

    val yRange = remember(candles) {
        val lo = candles.minOf { it.low }
        val hi = candles.maxOf { it.high }
        val pad = (hi - lo).coerceAtLeast(1f) * 0.05f
        AxisRange(lo - pad, hi + pad).autoNice(spec.yAxis.tickCount)
    }
    val volumeMax = remember(candles) { candles.maxOfOrNull { it.volume } ?: 0f }

    val progress by rememberChartProgress(candles.hashCode(), initial = if (spec.animate) 0f else 1f)
    var selected by remember(candles) { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier.pointerInput(candles, spec) {
            detectTapGestures { offset ->
                val plot = candlePriceRect(size.width.toFloat(), size.height.toFloat(), spec, this)
                if (!plot.contains(offset)) {
                    selected = null
                    return@detectTapGestures
                }
                val slotWidth = plot.width / candles.size
                val idx = ((offset.x - plot.left) / slotWidth).toInt().coerceIn(0, candles.lastIndex)
                selected = idx
                onCandleSelected?.invoke(idx)
            }
        }
    ) {
        drawCandleChart(candles, yRange, volumeMax, spec, theme, progress, selected, measurer)
    }
}

private fun DrawScope.drawCandleChart(
    candles: List<Candle>,
    yRange: AxisRange,
    volumeMax: Float,
    spec: CandleChartSpec,
    theme: ChartTheme,
    progress: Float,
    selected: Int?,
    measurer: TextMeasurer,
) {
    val full = Rect(
        left = spec.leftAxisGutter.toPx(),
        top = spec.topAxisGutter.toPx(),
        right = size.width - spec.rightAxisGutter.toPx(),
        bottom = size.height - spec.bottomAxisGutter.toPx(),
    )
    val volumeHeight = if (spec.showVolume) full.height * spec.volumeFraction else 0f
    val priceRect = Rect(full.left, full.top, full.right, full.bottom - volumeHeight)
    val volumeRect = if (spec.showVolume) Rect(full.left, priceRect.bottom + 4.dp.toPx(), full.right, full.bottom) else null

    // Y grid + labels
    val ticks = niceTicks(yRange, spec.yAxis.tickCount)
    if (spec.yAxis.showGridLines) {
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, priceRect.bottom..priceRect.top)
            drawLine(
                color = theme.gridColor,
                start = Offset(priceRect.left, y),
                end = Offset(priceRect.right, y),
                strokeWidth = theme.gridLineWidth.toPx(),
            )
            if (spec.yAxis.showLabels) {
                val layout = measurer.measure(spec.yAxis.labelFormatter(v), style = theme.axisLabelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        priceRect.left - layout.size.width - spec.yAxis.labelGap.toPx(),
                        y - layout.size.height / 2f,
                    ),
                )
            }
        }
    }

    val slotWidth = priceRect.width / candles.size
    val bodyWidth = (slotWidth - spec.candleSpacing.toPx()).coerceIn(2.dp.toPx(), spec.maxCandleWidth.toPx())

    candles.forEachIndexed { i, c ->
        val center = priceRect.left + slotWidth * (i + 0.5f)
        val color = if (c.isBullish) spec.bullishColor else spec.bearishColor
        val isSelected = selected == i

        val openY = lerpRange(c.open, yRange, priceRect.bottom..priceRect.top)
        val closeY = lerpRange(c.close, yRange, priceRect.bottom..priceRect.top)
        val highY = lerpRange(c.high, yRange, priceRect.bottom..priceRect.top)
        val lowY = lerpRange(c.low, yRange, priceRect.bottom..priceRect.top)

        // Animate by scaling vertically from the median (open+close average) so candles grow
        // outward rather than from the baseline — feels appropriate for price charts.
        val median = (openY + closeY) / 2f
        fun scale(y: Float): Float = median + (y - median) * progress

        val animOpenY = scale(openY)
        val animCloseY = scale(closeY)
        val animHighY = scale(highY)
        val animLowY = scale(lowY)

        // Wick
        drawLine(
            color = color,
            start = Offset(center, animHighY),
            end = Offset(center, animLowY),
            strokeWidth = spec.wickWidth.toPx(),
        )

        // Body
        val bodyTop = kotlin.math.min(animOpenY, animCloseY)
        val bodyBottom = kotlin.math.max(animOpenY, animCloseY)
        val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(1.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(center - bodyWidth / 2f, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(spec.bodyCornerRadius.toPx(), spec.bodyCornerRadius.toPx()),
        )

        if (isSelected) {
            // Highlight box around the candle
            drawRoundRect(
                color = theme.tooltipBackground.copy(alpha = 0.18f),
                topLeft = Offset(priceRect.left + i * slotWidth, priceRect.top),
                size = Size(slotWidth, priceRect.height),
            )
        }

        if (volumeRect != null && volumeMax > 0f) {
            val v = c.volume / volumeMax
            val vHeight = volumeRect.height * v * progress
            drawRoundRect(
                color = color.copy(alpha = 0.6f),
                topLeft = Offset(center - bodyWidth / 2f, volumeRect.bottom - vHeight),
                size = Size(bodyWidth, vHeight),
                cornerRadius = CornerRadius(spec.bodyCornerRadius.toPx(), spec.bodyCornerRadius.toPx()),
            )
        }
    }

    // X labels — show a subset to keep things readable.
    if (spec.xAxis.showLabels) {
        val tickCount = spec.xAxis.tickCount.coerceAtLeast(2)
        val step = (candles.size / tickCount).coerceAtLeast(1)
        for (i in candles.indices step step) {
            val center = priceRect.left + slotWidth * (i + 0.5f)
            val layout = measurer.measure(candles[i].label, style = theme.axisLabelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(center - layout.size.width / 2f, full.bottom + spec.xAxis.labelGap.toPx()),
            )
        }
    }

    drawLine(
        color = theme.axisColor,
        start = Offset(priceRect.left, priceRect.bottom),
        end = Offset(priceRect.right, priceRect.bottom),
        strokeWidth = theme.axisLineWidth.toPx(),
    )
}

private fun candlePriceRect(
    width: Float,
    height: Float,
    spec: CandleChartSpec,
    density: androidx.compose.ui.unit.Density,
): Rect = with(density) {
    val volH = if (spec.showVolume) (height - spec.topAxisGutter.toPx() - spec.bottomAxisGutter.toPx()) * spec.volumeFraction else 0f
    Rect(
        left = spec.leftAxisGutter.toPx(),
        top = spec.topAxisGutter.toPx(),
        right = width - spec.rightAxisGutter.toPx(),
        bottom = height - spec.bottomAxisGutter.toPx() - volH,
    )
}