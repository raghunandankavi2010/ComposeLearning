package com.example.composelearning.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.hypot

@Immutable
data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color? = null,
)

@Immutable
data class DonutChartSpec(
    val strokeWidth: Dp = 28.dp,
    val sliceGapDegrees: Float = 2f,
    val animate: Boolean = true,
    val selectedExtraRadius: Dp = 6.dp,
    val startAngle: Float = -90f,
)

/**
 * Customizable donut chart with a center slot for headline text, animated sweep entry, and
 * tap-to-select segment isolation.
 *
 * Performance: a single Canvas draws all slices as arc strokes. Tap hit-testing uses polar
 * coordinates rather than path containment, which is O(slices) and allocation-free.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    spec: DonutChartSpec = DonutChartSpec(),
    theme: ChartTheme = ChartDefaults.theme(),
    centerContent: @Composable (selectedIndex: Int?) -> Unit = { idx ->
        DefaultDonutCenter(slices = slices, selectedIndex = idx, theme = theme)
    },
    onSliceSelected: ((index: Int) -> Unit)? = null,
) {
    if (slices.isEmpty()) return
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f) }
    val progress by rememberChartProgress(slices.hashCode(), initial = if (spec.animate) 0f else 1f)
    var selected by remember(slices) { mutableStateOf<Int?>(null) }

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(slices, spec) {
                detectTapGestures { offset ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distance = hypot(dx, dy)
                    val outerRadius = kotlin.math.min(size.width, size.height) / 2f
                    val innerRadius = outerRadius - spec.strokeWidth.toPx()
                    if (distance < innerRadius || distance > outerRadius + spec.selectedExtraRadius.toPx()) {
                        selected = null
                        return@detectTapGestures
                    }
                    val angleDeg = (Math.toDegrees(atan2(dy, dx).toDouble())).toFloat()
                    val normalized = ((angleDeg - spec.startAngle) % 360f + 360f) % 360f
                    var cursor = 0f
                    var hit: Int? = null
                    for (i in slices.indices) {
                        val sweep = slices[i].value / total * 360f
                        if (normalized in cursor..(cursor + sweep)) {
                            hit = i; break
                        }
                        cursor += sweep
                    }
                    selected = hit
                    if (hit != null) onSliceSelected?.invoke(hit)
                }
            }
        ) {
            val stroke = spec.strokeWidth.toPx()
            val outerRadius = kotlin.math.min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcSize = Size(outerRadius * 2f - stroke, outerRadius * 2f - stroke)
            val topLeft = Offset(center.x - arcSize.width / 2f, center.y - arcSize.height / 2f)
            var startAngle = spec.startAngle
            slices.forEachIndexed { i, slice ->
                val color = slice.color ?: theme.palette[i % theme.palette.size]
                val sweep = (slice.value / total) * 360f * progress
                val gap = spec.sliceGapDegrees.coerceAtMost(sweep / 2f)
                val isSelected = selected == i
                val extra = if (isSelected) spec.selectedExtraRadius.toPx() else 0f
                val effectiveTopLeft = if (extra > 0f) Offset(topLeft.x - extra, topLeft.y - extra) else topLeft
                val effectiveSize = if (extra > 0f) Size(arcSize.width + extra * 2f, arcSize.height + extra * 2f) else arcSize
                drawArc(
                    color = color,
                    startAngle = startAngle + gap / 2f,
                    sweepAngle = (sweep - gap).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = effectiveTopLeft,
                    size = effectiveSize,
                    style = Stroke(width = stroke),
                )
                startAngle += slice.value / total * 360f
            }
        }
        centerContent(selected)
    }
}

@Composable
private fun DefaultDonutCenter(
    slices: List<DonutSlice>,
    selectedIndex: Int?,
    theme: ChartTheme,
) {
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (selectedIndex != null) {
            val s = slices[selectedIndex]
            val pct = (s.value / total * 100f)
            Text(s.label, style = theme.valueLabelStyle)
            Text("%.0f%%".format(pct), style = theme.titleStyle)
        } else {
            Text("Total", style = theme.axisLabelStyle)
            Text("%.0f".format(total), style = theme.titleStyle)
        }
    }
}

/** Legend that pairs nicely with [DonutChart]. */
@Composable
fun DonutLegend(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    theme: ChartTheme = ChartDefaults.theme(),
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slices.forEachIndexed { i, s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(s.color ?: theme.palette[i % theme.palette.size])
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text(s.label, style = theme.axisLabelStyle, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.size(8.dp))
                Text("${s.value.toInt()}", style = theme.valueLabelStyle)
            }
        }
    }
}