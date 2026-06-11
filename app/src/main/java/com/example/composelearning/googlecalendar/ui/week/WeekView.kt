package com.example.composelearning.googlecalendar.ui.week

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import com.example.composelearning.googlecalendar.ui.day.HOUR_HEIGHT_DP
import com.example.composelearning.googlecalendar.util.DateUtils
import com.example.composelearning.googlecalendar.util.OverlapCalculator
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged

private const val WEEK_PAGE_COUNT = 104 // ~1 year before and after
private const val WEEK_CENTER_PAGE = WEEK_PAGE_COUNT / 2

@Composable
fun WeekView(
    selectedDate: LocalDate,
    weekEvents: Map<LocalDate, List<CalendarEvent>>,
    onDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = WEEK_CENTER_PAGE) { WEEK_PAGE_COUNT }

    val todayWeekStart = DateUtils.startOfWeek(LocalDate.now())

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val offset = (page - WEEK_CENTER_PAGE).toLong()
                val weekStart = todayWeekStart.plusWeeks(offset)
                val newDate = weekStart // select first day of week
                if (DateUtils.startOfWeek(newDate) != DateUtils.startOfWeek(selectedDate)) {
                    onDateChanged(newDate)
                }
            }
    }

    val weekDays = remember(selectedDate) { DateUtils.weekDays(selectedDate) }

    Column(modifier = modifier.fillMaxSize()) {
        WeekDayHeader(
            weekDays = weekDays,
            selectedDate = selectedDate
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val offset = (page - WEEK_CENTER_PAGE).toLong()
            val pageWeekStart = todayWeekStart.plusWeeks(offset)
            val pageWeekDays = DateUtils.weekDays(pageWeekStart)

            WeekTimeline(
                weekDays = pageWeekDays,
                eventsByDay = if (pageWeekStart == DateUtils.startOfWeek(selectedDate)) {
                    weekEvents
                } else {
                    emptyMap()
                }
            )
        }
    }
}

@Composable
fun WeekTimeline(
    weekDays: List<LocalDate>,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val hourHeight = (HOUR_HEIGHT_DP * 0.75f).dp // Slightly shorter for week view
    val totalHeight = hourHeight * 24
    val density = LocalDensity.current

    // Scroll to current hour
    LaunchedEffect(Unit) {
        val currentHour = LocalTime.now().hour
        val scrollTarget = with(density) { (hourHeight * currentHour).toPx().toInt() }
        scrollState.scrollTo(scrollTarget)
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Row(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Time column — compact labels for the week view
        Column(modifier = Modifier.width(40.dp)) {
            (0..23).forEach { hour ->
                WeekTimeLabel(hour = hour, hourHeight = hourHeight)
            }
        }

        // Divider between time column and day grid
        VerticalDivider(
            color = gridColor,
            modifier = Modifier.height(totalHeight)
        )

        // 7 day columns
        weekDays.forEachIndexed { index, date ->
            val dayEvents = eventsByDay[date]?.filter { !it.isAllDay } ?: emptyList()
            val layoutEvents = remember(dayEvents) {
                OverlapCalculator.calculateLayout(dayEvents)
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeight)
            ) {
                val columnWidth = maxWidth

                // Hour grid lines
                Column(modifier = Modifier.fillMaxSize()) {
                    (0..23).forEach { _ ->
                        Box(modifier = Modifier.height(hourHeight)) {
                            HorizontalDivider(
                                color = gridColor,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                    }
                }

                // Right-edge vertical divider (skip last column)
                if (index < weekDays.size - 1) {
                    VerticalDivider(
                        color = gridColor,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxHeight()
                    )
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

                // Render events
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
                    val eventWidth = columnWidth / layoutEvent.totalColumns
                    val xOffset = eventWidth * layoutEvent.column

                    WeekEventChip(
                        title = event.title,
                        color = event.color,
                        topOffset = topOffset,
                        height = eventHeight,
                        width = eventWidth - 1.dp,
                        xOffset = xOffset
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekTimeLabel(
    hour: Int,
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(hourHeight)
            .width(40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (hour != 0) {
            val displayHour = if (hour > 12) {
                hour - 12
            } else if (hour == 0) {
                12
            } else {
                hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            Text(
                text = "$displayHour $amPm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
                modifier = Modifier.offset(y = (-5).dp)
            )
        }
    }
}

@Composable
private fun WeekEventChip(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    topOffset: Dp,
    height: Dp,
    width: Dp,
    xOffset: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(x = xOffset, y = topOffset)
            .width(width)
            .height(height)
            .padding(0.5.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            .padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = if (height > 24.dp) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 10.sp
        )
    }
}
