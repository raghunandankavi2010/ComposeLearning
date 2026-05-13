package com.example.composelearning.charts

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Describes how an axis renders ticks, gridlines and labels.
 *
 * The chart computes nice-rounded tick positions through [niceTicks] so axes line up across data
 * sets without callers having to pre-compute scales.
 */
@Immutable
data class ChartAxis(
    val show: Boolean = true,
    val showGridLines: Boolean = true,
    val showLabels: Boolean = true,
    val labelFormatter: (Float) -> String = { it.toString() },
    val tickCount: Int = 5,
    val labelGap: Dp = 4.dp,
    val labelRotationDegrees: Float = 0f,
)

/** Bounds along one axis. Use [autoNice] to expand to a tick-aligned range. */
@Immutable
data class AxisRange(
    val min: Float,
    val max: Float,
) {
    val span: Float get() = max - min
}

/**
 * Compute "nice" tick positions across [range] for [maxTicks] ticks.
 *
 * Uses the classic Heckbert algorithm. Returns a list of tick values that are aligned to a power
 * of 10 so the labels read cleanly (10, 20, 30 rather than 10.3, 23.6, 36.9).
 */
fun niceTicks(range: AxisRange, maxTicks: Int): List<Float> {
    if (range.span == 0f) return listOf(range.min)
    val niceRange = niceNum(range.span, round = false)
    val tickSpacing = niceNum(niceRange / (maxTicks - 1).coerceAtLeast(1), round = true)
    val niceMin = floor(range.min / tickSpacing) * tickSpacing
    val niceMax = ceil(range.max / tickSpacing) * tickSpacing
    val result = mutableListOf<Float>()
    var v = niceMin
    while (v <= niceMax + tickSpacing * 0.5f) {
        result += v
        v += tickSpacing
    }
    return result
}

private fun niceNum(range: Float, round: Boolean): Float {
    val exponent = floor(log10(abs(range).toDouble())).toFloat()
    val fraction = range / 10f.pow(exponent)
    val niceFraction = if (round) {
        when {
            fraction < 1.5f -> 1f
            fraction < 3f -> 2f
            fraction < 7f -> 5f
            else -> 10f
        }
    } else {
        when {
            fraction <= 1f -> 1f
            fraction <= 2f -> 2f
            fraction <= 5f -> 5f
            else -> 10f
        }
    }
    return niceFraction * 10f.pow(exponent)
}

/** Expand this range to align with nice ticks so axes look clean. */
fun AxisRange.autoNice(tickCount: Int = 5): AxisRange {
    val ticks = niceTicks(this, tickCount)
    if (ticks.isEmpty()) return this
    return AxisRange(ticks.first(), ticks.last())
}