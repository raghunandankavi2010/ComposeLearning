package com.example.composelearning.googlecalendar.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import com.example.composelearning.googlecalendar.domain.model.LayoutEvent
import com.example.composelearning.googlecalendar.util.DateUtils
import com.example.composelearning.googlecalendar.util.OverlapCalculator
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged

private const val PAGE_COUNT = 365 * 2 // ~1 year before and after
private const val CENTER_PAGE = PAGE_COUNT / 2

@Composable
fun DayView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = CENTER_PAGE) { PAGE_COUNT }

    // Sync pager with selected date
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val offset = page - CENTER_PAGE
                val newDate = LocalDate.now().plusDays(offset.toLong())
                if (newDate != selectedDate) {
                    onDateChanged(newDate)
                }
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Date header
        Text(
            text = DateUtils.formatDayMonth(selectedDate),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageDate = LocalDate.now().plusDays((page - CENTER_PAGE).toLong())
            DayTimeline(
                date = pageDate,
                events = if (pageDate == selectedDate) events else emptyList()
            )
        }
    }
}

@Composable
fun DayTimeline(
    date: LocalDate,
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val hourHeight = HOUR_HEIGHT_DP.dp
    val totalHeight = hourHeight * 24

    // Scroll to current hour on first display
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        val currentHour = LocalTime.now().hour
        val scrollTarget = with(density) { (hourHeight * currentHour).toPx().toInt() }
        scrollState.scrollTo(scrollTarget)
    }

    // Calculate overlapping layout
    val timedEvents = remember(events) {
        events.filter { !it.isAllDay }
    }
    val layoutEvents = remember(timedEvents) {
        OverlapCalculator.calculateLayout(timedEvents)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Time column
        Column(modifier = Modifier.width(52.dp)) {
            (0..23).forEach { hour ->
                TimeLabel(hour = hour, hourHeight = hourHeight)
            }
        }

        // Event area
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(totalHeight)
        ) {
            val availableWidth = maxWidth

            // Hour grid lines
            Column(modifier = Modifier.fillMaxSize()) {
                (0..23).forEach { _ ->
                    Box(modifier = Modifier.height(hourHeight)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
            }

            // Current time indicator
            if (DateUtils.isToday(date)) {
                val now = LocalTime.now()
                val minutesSinceMidnight = now.hour * 60 + now.minute
                val indicatorOffset = with(density) {
                    (minutesSinceMidnight * hourHeight.toPx() / 60f).toDp()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = indicatorOffset)
                        .background(MaterialTheme.colorScheme.error)
                )
            }

            // Render events with overlap layout
            layoutEvents.forEach { layoutEvent ->
                val event = layoutEvent.event
                val startMinutes = event.displayStartTime.hour * 60 + event.displayStartTime.minute
                val endMinutes = event.displayEndTime.hour * 60 + event.displayEndTime.minute
                val durationMinutes = (endMinutes - startMinutes).coerceAtLeast(15)

                val topOffset = with(density) {
                    (startMinutes * hourHeight.toPx() / 60f).toDp()
                }
                val eventHeight = with(density) {
                    (durationMinutes * hourHeight.toPx() / 60f).toDp()
                }

                val columnWidth = availableWidth / layoutEvent.totalColumns
                val xOffset = columnWidth * layoutEvent.column

                EventBlockPositioned(
                    layoutEvent = layoutEvent,
                    topOffset = topOffset,
                    height = eventHeight,
                    width = columnWidth - 2.dp,
                    xOffset = xOffset,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun EventBlockPositioned(
    layoutEvent: LayoutEvent,
    topOffset: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    xOffset: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val event = layoutEvent.event
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .offset(x = xOffset, y = topOffset)
            .width(width)
            .height(height)
            .padding(1.dp)
            .background(event.color.copy(alpha = 0.15f), shape)
            .then(
                Modifier.background(
                    event.color.copy(alpha = 0.15f),
                    shape
                )
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Column {
            Text(
                text = event.title,
                color = event.color,
                fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = FontWeight.Medium,
                maxLines = if (height > 40.dp) 2 else 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            if (height >= 36.dp) {
                Text(
                    text = DateUtils.formatEventTimeRange(event.startTime, event.endTime),
                    color = event.color.copy(alpha = 0.7f),
                    fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                    maxLines = 1,
                    lineHeight = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
    }
}
