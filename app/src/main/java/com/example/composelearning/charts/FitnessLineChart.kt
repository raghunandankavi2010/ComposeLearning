package com.example.composelearning.charts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.lerp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Tuned so each page holds roughly a fortnight of data; combined with the wider pageWidth below
// this leaves enough horizontal room per day for the "EEE" + "MMM d" label stack to read cleanly.
private const val DAYS_PER_PAGE = 12

/**
 * Continuous line chart that pages through fitness data backwards in time.
 *
 * The day-page model:
 *   • [DAYS_PER_PAGE] days form a "page".
 *   • Page index 0 contains the most recent days (today included). Higher indices reach further
 *     into the past.
 *   • The LazyRow renders with `reverseLayout = true` so the user starts seeing the latest data
 *     and can scroll left to load older history (similar to a chat backlog).
 *
 * Line continuity:
 *   • Each page draws a line over its [DAYS_PER_PAGE] points.
 *   • To eliminate visible "joins" between adjacent pages, every page also takes one neighbour
 *     point from the page on each side and extends its draw range by half a slot. Strokes are
 *     clipped to the page bounds so neighbours never paint past each other but the curve appears
 *     continuous when fitted together.
 *
 * Performance:
 *   • Each page is a [Canvas] sized to a fixed width derived from screen width. LazyRow handles
 *     view recycling.
 *   • Path building is local to one page so the per-frame cost is bounded by [DAYS_PER_PAGE], not
 *     total loaded days.
 *   • Older pages are loaded asynchronously through [FitnessRepository.loadDays] only when the
 *     user scrolls past a threshold — measured with [LazyListState.layoutInfo.visibleItemsInfo].
 */
@Composable
fun FitnessLineChart(
    modifier: Modifier = Modifier,
    daysPerPage: Int = DAYS_PER_PAGE,
    pageWidth: Dp = 420.dp,
    pageHeight: Dp = 220.dp,
    initialPages: Int = 5,
    theme: ChartTheme = ChartDefaults.theme(),
) {
    val pages = remember { mutableStateOf<List<List<FitnessDay>>>(emptyList()) }
    val loading = remember { mutableStateOf(false) }

    LaunchedEffect(initialPages, daysPerPage) {
        val collected = mutableListOf<List<FitnessDay>>()
        for (i in 0 until initialPages) {
            val start = i * daysPerPage
            collected += FitnessRepository.loadDays(start, daysPerPage, simulatedDelayMs = 0)
        }
        pages.value = collected
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState, pages.value, daysPerPage) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible
        }
            .distinctUntilChanged()
            .collect { lastVisible ->
                val total = pages.value.size
                if (lastVisible >= total - 2 && !loading.value) {
                    loading.value = true
                    val next = FitnessRepository.loadDays(
                        startOffset = total * daysPerPage,
                        count = daysPerPage,
                        simulatedDelayMs = 350,
                    )
                    pages.value = pages.value + listOf(next)
                    loading.value = false
                }
            }
    }

    // Per-page Y range. Each page gets its own (min, max) padded the same way as before — so a
    // calm week reads "calm" (axis caps just above its peak) while a heavy week's axis grows to
    // accommodate the spike. The Y axis then animates between these ranges as the user scrolls,
    // matching the old Google Fit weekly chart behaviour.
    val pageYRanges = remember(pages.value) {
        pages.value.mapIndexed { index, page ->
            // Each page renders its own days PLUS up to two days from each adjacent page (as
            // leftExtras/rightExtras for cubic curve continuity). The Y range must include those
            // neighbour days too, otherwise an unusually low/high neighbour will plot outside the
            // page's plot rect — visible as a curve segment dipping below the X axis.
            val left = pages.value.getOrNull(index + 1)?.take(2).orEmpty()
            val right = pages.value.getOrNull(index - 1)?.takeLast(2).orEmpty()
            val combined = page + left + right
            if (combined.isEmpty()) 0f to 1f
            else {
                val lo = combined.minOf { it.steps }.toFloat()
                val hi = combined.maxOf { it.steps }.toFloat()
                val pad = (hi - lo).coerceAtLeast(1f) * 0.15f
                (lo - pad).coerceAtLeast(0f) to (hi + pad)
            }
        }
    }

    // Fractional page index = where the scroll has dragged us between adjacent pages. Reading
    // layoutInfo through derivedStateOf scopes recomposition to "the float actually changed",
    // not every frame the snapshot reads anything else.
    val scrollPageFloat by remember(pageYRanges) {
        derivedStateOf {
            val info = listState.layoutInfo
            val first = info.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf 0f
            val slot = first.size.coerceAtLeast(1)
            first.index + (-first.offset.toFloat() / slot)
        }
    }

    // Interpolate the Y range linearly between the floor and ceiling pages by the fractional
    // offset. Combined with animateFloatAsState below, this gives the smooth "axis rising as the
    // peak day comes into view" feel of the old Google Fit chart.
    val (targetYMin, targetYMax) = run {
        if (pageYRanges.isEmpty()) {
            0f to 1f
        } else {
            val frac = scrollPageFloat.coerceIn(0f, pageYRanges.lastIndex.toFloat())
            val lower = frac.toInt().coerceIn(0, pageYRanges.lastIndex)
            val upper = (lower + 1).coerceAtMost(pageYRanges.lastIndex)
            val t = (frac - lower).coerceIn(0f, 1f)
            val lo = lerp(pageYRanges[lower].first, pageYRanges[upper].first, t)
            val hi = lerp(pageYRanges[lower].second, pageYRanges[upper].second, t)
            lo to hi
        }
    }

    // Spring on the resolved bounds. While the finger is dragging, scrollPageFloat updates the
    // target continuously and the spring tracks it; on release the spring keeps the axis smooth
    // through the fling settle. Medium-low stiffness is the closest match to Google Fit's feel
    // — quick enough to follow the swipe, slow enough that the labels don't jitter.
    val yMin by animateFloatAsState(
        targetValue = targetYMin,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fitness-yMin",
    )
    val yMax by animateFloatAsState(
        targetValue = targetYMax,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "fitness-yMax",
    )

    Column(modifier) {
      Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Persistent Y-axis on the left edge of the screen. It reads the same animated
            // yMin/yMax the pages do, so its tick labels rise/fall in sync with the chart as
            // the user scrolls between pages.
            YAxisColumn(
                yMin = yMin,
                yMax = yMax,
                height = pageHeight,
                theme = theme,
            )
            LazyRow(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.weight(1f),
            ) {
            items(
                count = pages.value.size,
                key = { it },
            ) { pageIndex ->
                val current = pages.value[pageIndex]
                val newer = pages.value.getOrNull(pageIndex - 1)
                val older = pages.value.getOrNull(pageIndex + 1)
                // Pass up to TWO neighbour days on each side so the cubic curve's control points
                // around the page boundary are computed from the same four points in both adjacent
                // pages — guaranteeing the line tangent matches across the join.
                //
                // leftExtras[0] sits at slot -1 (just left of slot 0), leftExtras[1] at slot -2.
                // Older page is to the LEFT on screen; older's "two newest days" (pageDays.first()
                // and pageDays[1] in load order) are the days immediately older than this page's
                // leftmost-displayed day.
                val leftExtras = older?.take(2).orEmpty()
                // rightExtras[0] sits at slot N (just right of slot N-1), rightExtras[1] at slot N+1.
                // Newer page is to the RIGHT; newer's "two oldest days" (pageDays.last() and
                // pageDays[size-2]) are the days immediately newer than this page's rightmost
                // displayed day.
                val rightExtras = newer?.takeLast(2)?.reversed().orEmpty()
                FitnessPage(
                    pageDays = current,
                    leftExtras = leftExtras,
                    rightExtras = rightExtras,
                    yMin = yMin,
                    yMax = yMax,
                    width = pageWidth,
                    height = pageHeight,
                    theme = theme,
                    isLatestPage = pageIndex == 0,
                )
            }
            }
        }
        // Older-page lazy-load indicator. With reverseLayout = true the older pages stack on the
        // left, so the spinner appears on the left edge of the chart while the next chunk is
        // fetched from FitnessRepository.loadDays.
        if (loading.value) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(22.dp),
            )
        }
      }
    }
}

@Composable
private fun YAxisColumn(
    yMin: Float,
    yMax: Float,
    height: Dp,
    theme: ChartTheme,
) {
    val measurer = rememberTextMeasurer()
    // Matches FitnessPage's vertical padding/inset so the tick rows align with the chart's grid.
    Canvas(
        modifier = Modifier
            .width(46.dp)
            .height(height)
            .padding(vertical = 8.dp),
    ) {
        val plotTop = 12.dp.toPx()
        val plotBottom = size.height - 36.dp.toPx()
        // Raw animated range — labels and positions move smoothly with the spring instead of
        // snapping to niceTicks rounded values that read as "static" between pages with similar
        // peaks.
        val yRange = AxisRange(yMin, yMax)
        val ticks = fitnessYTicks(yMin, yMax, 4)
        // Right-edge baseline visually anchors the labels to the chart panel.
        drawLine(
            color = theme.axisColor,
            start = Offset(size.width, plotTop),
            end = Offset(size.width, plotBottom),
            strokeWidth = theme.axisLineWidth.toPx(),
        )
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, plotBottom..plotTop)
            val label = formatSteps(v)
            val layout = measurer.measure(
                label,
                style = theme.axisLabelStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            )
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    size.width - layout.size.width - 6.dp.toPx(),
                    y - layout.size.height / 2f,
                ),
            )
        }
    }
}

// Evenly-spaced ticks drawn from the raw animated range. Pages and the Y-axis column share this
// so the gridlines and the labels are guaranteed to line up frame-by-frame as yMin/yMax animate.
internal fun fitnessYTicks(yMin: Float, yMax: Float, segments: Int = 4): List<Float> {
    if (yMax - yMin < 0.001f) return listOf(yMin)
    val step = (yMax - yMin) / segments
    return (0..segments).map { yMin + step * it }
}

private fun formatSteps(v: Float): String = when {
    v < 1000f -> v.toInt().toString()
    v < 10000f -> "%.1fk".format(v / 1000f)
    else -> "${(v / 1000f).toInt()}k"
}

@Composable
private fun FitnessPage(
    pageDays: List<FitnessDay>,
    leftExtras: List<FitnessDay>,
    rightExtras: List<FitnessDay>,
    yMin: Float,
    yMax: Float,
    width: Dp,
    height: Dp,
    theme: ChartTheme,
    isLatestPage: Boolean,
) {
    val measurer = rememberTextMeasurer()
    val primaryColor = theme.palette.first()
    val formatter = remember { NumberFormat.getIntegerInstance() }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }

    // NOTE: no horizontal padding here — adjacent pages must touch in the LazyRow so the line
    // tips across pages line up. Vertical padding stays so we have room for labels.
    Canvas(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(vertical = 8.dp),
    ) {
        // Display order: oldest-on-left, newest-on-right. Pages are loaded newest-first so reverse.
        val daysVisual = pageDays.asReversed()
        val n = daysVisual.size
        // Plot rect spans the full canvas horizontally so slot N-1 sits at right edge and slot 0
        // at left edge — adjacent pages then align by construction.
        val plot = Rect(
            left = 0f,
            top = 12.dp.toPx(),
            right = size.width,
            bottom = size.height - 36.dp.toPx(),
        )
        val slotWidth = plot.width / n

        // Raw animated Y range — match YAxisColumn exactly so labels and gridlines stay aligned.
        val yRange = AxisRange(yMin, yMax)
        val ticks = fitnessYTicks(yMin, yMax, 4)
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, plot.bottom..plot.top)
            drawLine(
                color = theme.gridColor,
                start = Offset(plot.left, y),
                end = Offset(plot.right, y),
                strokeWidth = theme.gridLineWidth.toPx(),
            )
        }

        // Vertical grid lines — one per day slot. Lighter than the Y grid so the horizontal
        // rows remain the primary read while the columns still anchor each day visually.
        for (i in 0 until n) {
            val xCol = plot.left + slotWidth * (i + 0.5f)
            drawLine(
                color = theme.gridColor.copy(alpha = 0.35f),
                start = Offset(xCol, plot.top),
                end = Offset(xCol, plot.bottom),
                strokeWidth = theme.gridLineWidth.toPx() * 0.6f,
            )
        }

        // X-axis baseline along the bottom of the plot. Drawn before the line/area so the data
        // sits on top of the axis rather than getting half-covered by it.
        drawLine(
            color = theme.axisColor,
            start = Offset(plot.left, plot.bottom),
            end = Offset(plot.right, plot.bottom),
            strokeWidth = theme.axisLineWidth.toPx(),
        )

        // Slot k has center at (k + 0.5) * slotWidth. Slot -1 is the day immediately LEFT of slot 0
        // (just outside this canvas); slot N is the day immediately RIGHT of slot N-1.
        fun xForSlot(slotIndex: Float): Float = plot.left + slotWidth * (slotIndex + 0.5f)
        fun yForSteps(steps: Int): Float = lerpRange(steps.toFloat(), yRange, plot.bottom..plot.top)

        // Build the extended point list in left-to-right slot order:
        //   [slot -2, slot -1, slot 0, ..., slot N-1, slot N, slot N+1]
        // Missing extras (page is the oldest or newest loaded) are simply skipped, which makes the
        // Catmull-Rom clamp at the boundary — fine for the screen edges.
        val extendedPoints = mutableListOf<Offset>()
        // leftExtras[0] is at slot -1 (closest to page), leftExtras[1] at slot -2.
        // Add them in slot order (-2 first, then -1).
        if (leftExtras.size >= 2) {
            extendedPoints += Offset(xForSlot(-2f), yForSteps(leftExtras[1].steps))
        }
        if (leftExtras.isNotEmpty()) {
            extendedPoints += Offset(xForSlot(-1f), yForSteps(leftExtras[0].steps))
        }
        daysVisual.forEachIndexed { i, d ->
            extendedPoints += Offset(xForSlot(i.toFloat()), yForSteps(d.steps))
        }
        if (rightExtras.isNotEmpty()) {
            extendedPoints += Offset(xForSlot(n.toFloat()), yForSteps(rightExtras[0].steps))
        }
        if (rightExtras.size >= 2) {
            extendedPoints += Offset(xForSlot((n + 1).toFloat()), yForSteps(rightExtras[1].steps))
        }

        val path = buildLinePath(extendedPoints, LineSmoothing.Cubic)

        // Compose's Canvas clips draws to its bounds horizontally, but vertically the curve can
        // overshoot plot.top / plot.bottom (cubic interpolation through low-value neighbours, or a
        // brief mismatch while the shared yRange is animating between two pages). Clip the area
        // and line to the plot rect so any overshoot disappears into the axis rather than visibly
        // spilling into the X-axis label zone.
        val areaPath = Path().apply {
            addPath(path)
            closeAreaPath(this, extendedPoints, plot.bottom)
        }
        clipRect(
            left = plot.left,
            top = plot.top,
            right = plot.right,
            bottom = plot.bottom,
        ) {
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0f)),
                    startY = plot.top,
                    endY = plot.bottom,
                ),
            )
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        // Dots + day labels
        daysVisual.forEachIndexed { i, d ->
            val x = xForSlot(i.toFloat())
            val y = yForSteps(d.steps)
            drawCircle(theme.surface, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(primaryColor, radius = 2.5.dp.toPx(), center = Offset(x, y))

            // Small downward tick mark under each slot — sits between the X baseline and the
            // day label so the axis reads like a proper categorical scale.
            drawLine(
                color = theme.axisColor,
                start = Offset(x, plot.bottom),
                end = Offset(x, plot.bottom + 4.dp.toPx()),
                strokeWidth = theme.axisLineWidth.toPx(),
            )

            val date = LocalDate.now().minusDays(d.dayOffset.toLong())
            val dayLabel = date.format(dayFormatter)
            val dateLabel = date.format(dateFormatter)
            // Bumped sizes so the X-axis day labels read at a glance instead of dissolving into
            // the chart background. Day-of-week is the primary label, date is the subdued caption.
            val dayLayout = measurer.measure(
                dayLabel,
                style = theme.axisLabelStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            )
            val dateLayout = measurer.measure(
                dateLabel,
                style = theme.axisLabelStyle.copy(
                    fontSize = 10.sp,
                    color = theme.axisLabelStyle.color.copy(alpha = 0.7f),
                ),
            )
            drawText(
                textLayoutResult = dayLayout,
                topLeft = Offset(x - dayLayout.size.width / 2f, plot.bottom + 8.dp.toPx()),
            )
            drawText(
                textLayoutResult = dateLayout,
                topLeft = Offset(x - dateLayout.size.width / 2f, plot.bottom + 8.dp.toPx() + dayLayout.size.height),
            )
        }

        if (isLatestPage) {
            // Annotate the newest data point (visually rightmost) with its value.
            val newest = daysVisual.lastOrNull()
            if (newest != null) {
                val x = xForSlot(daysVisual.lastIndex.toFloat())
                val y = yForSteps(newest.steps)
                val label = formatter.format(newest.steps)
                val layout = measurer.measure(label, style = theme.valueLabelStyle)
                val w = layout.size.width + 10.dp.toPx()
                val h = layout.size.height + 4.dp.toPx()
                val rx = (x - w / 2f).coerceIn(plot.left, plot.right - w)
                val ry = (y - h - 8.dp.toPx()).coerceAtLeast(plot.top + 2.dp.toPx())
                drawRoundRect(
                    color = theme.tooltipBackground,
                    topLeft = Offset(rx, ry),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
                drawText(
                    textLayoutResult = layout,
                    color = theme.tooltipContent,
                    topLeft = Offset(rx + (w - layout.size.width) / 2f, ry + (h - layout.size.height) / 2f),
                )
            }
        }
    }
}