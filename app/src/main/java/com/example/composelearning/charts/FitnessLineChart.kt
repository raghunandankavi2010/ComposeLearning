package com.example.composelearning.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DAYS_PER_PAGE = 6

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
    pageWidth: Dp = 260.dp,
    pageHeight: Dp = 220.dp,
    initialPages: Int = 3,
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

    val (yMin, yMax) = remember(pages.value) {
        val all = pages.value.flatten()
        if (all.isEmpty()) 0f to 1f else {
            val lo = all.minOf { it.steps }.toFloat()
            val hi = all.maxOf { it.steps }.toFloat()
            // Pad the range so the curve doesn't graze the top/bottom edges.
            val pad = (hi - lo).coerceAtLeast(1f) * 0.15f
            (lo - pad).coerceAtLeast(0f) to (hi + pad)
        }
    }

    Column(modifier) {
        FitnessHeader(loading = loading.value, theme = theme)
        Spacer(Modifier.height(8.dp))
        LazyRow(
            state = listState,
            reverseLayout = true,
            modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun FitnessHeader(loading: Boolean, theme: ChartTheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Daily steps — scroll left to load older days", style = theme.titleStyle)
        Spacer(Modifier.weight(1f))
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.width(16.dp).height(16.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
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

        // Y range
        val yRange = AxisRange(yMin, yMax).autoNice(4)
        val ticks = niceTicks(yRange, 4)
        ticks.forEach { v ->
            val y = lerpRange(v, yRange, plot.bottom..plot.top)
            drawLine(
                color = theme.gridColor,
                start = Offset(plot.left, y),
                end = Offset(plot.right, y),
                strokeWidth = theme.gridLineWidth.toPx(),
            )
        }

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

        // No explicit clipRect — Compose's Canvas clips draws to its bounds, so the portion of the
        // curve that extends to slots -1, -2, N, N+1 is naturally trimmed to the visible page. The
        // matching half-segments rendered by adjacent pages complete the curve across joins.
        val areaPath = Path().apply {
            addPath(path)
            closeAreaPath(this, extendedPoints, plot.bottom)
        }
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

        // Dots + day labels
        daysVisual.forEachIndexed { i, d ->
            val x = xForSlot(i.toFloat())
            val y = yForSteps(d.steps)
            drawCircle(theme.surface, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(primaryColor, radius = 2.5.dp.toPx(), center = Offset(x, y))

            val date = LocalDate.now().minusDays(d.dayOffset.toLong())
            val dayLabel = date.format(dayFormatter)
            val dateLabel = date.format(dateFormatter)
            val dayLayout = measurer.measure(dayLabel, style = theme.axisLabelStyle)
            val dateLayout = measurer.measure(dateLabel, style = theme.axisLabelStyle.copy(color = theme.axisLabelStyle.color.copy(alpha = 0.6f)))
            drawText(
                textLayoutResult = dayLayout,
                topLeft = Offset(x - dayLayout.size.width / 2f, plot.bottom + 4.dp.toPx()),
            )
            drawText(
                textLayoutResult = dateLayout,
                topLeft = Offset(x - dateLayout.size.width / 2f, plot.bottom + 4.dp.toPx() + dayLayout.size.height),
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