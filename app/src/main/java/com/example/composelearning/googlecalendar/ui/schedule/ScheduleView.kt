package com.example.composelearning.googlecalendar.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import com.example.composelearning.googlecalendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

/**
 * Schedule view: collapsible month toolbar + endless event list.
 *
 * Key design decisions that prevent the old infinite-loop bug:
 * 1. [dateRange] is stable (centred on today, NOT selectedDate).
 * 2. Each LazyColumn item maps to a [ScheduleListItem] with a known [date],
 *    so firstVisibleItemIndex → date look-up is correct.
 * 3. Scrolls triggered by user actions (day tap, month swipe, Today button)
 *    go through explicit scroll-target state, guarded by [isProgrammaticScroll].
 * 4. There is NO LaunchedEffect(selectedDate) that scrolls the list.
 */
@Composable
fun ScheduleView(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    isToolbarExpanded: Boolean,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    eventDots: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onScrolledToDate: (LocalDate) -> Unit,
    onToolbarExpandedChanged: (Boolean) -> Unit,
    scrollToDate: LocalDate? = null,
    onScrollConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    // ── 1. Stable date range: 6 months centred on today ──────────────
    val dateRange = remember {
        val start = today.minusDays(90)
        (0L until 180L).map { start.plusDays(it) }
    }

    // ── 2. Flat item list + date → index map ─────────────────────────
    val scheduleItems = remember(dateRange, eventsByDate) {
        buildScheduleItems(dateRange, eventsByDate)
    }
    val dateToIndex = remember(scheduleItems) {
        buildDateToIndex(scheduleItems)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = dateToIndex[selectedDate]
            ?: dateToIndex[today]
            ?: 0
    )

    // ── 3. Programmatic-scroll guard ─────────────────────────────────
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Internal target set by MonthToolbar interactions (day tap / month swipe).
    var internalScrollTarget by remember { mutableStateOf<LocalDate?>(null) }

    // Handle internal scroll targets
    LaunchedEffect(internalScrollTarget) {
        internalScrollTarget?.let { date ->
            dateToIndex[date]?.let { idx ->
                try {
                    isProgrammaticScroll = true
                    listState.animateScrollToItem(idx)
                } finally {
                    isProgrammaticScroll = false
                }
            }
            internalScrollTarget = null
        }
    }

    // Handle external scroll targets (e.g. Today button)
    LaunchedEffect(scrollToDate) {
        scrollToDate?.let { date ->
            dateToIndex[date]?.let { idx ->
                try {
                    isProgrammaticScroll = true
                    listState.animateScrollToItem(idx)
                } finally {
                    isProgrammaticScroll = false
                }
            }
            onScrollConsumed()
        }
    }

    // ── 4. Scroll observation: visible date + collapse / expand ──────
    // rememberUpdatedState keeps these values fresh inside the long-lived
    // LaunchedEffect(listState) coroutine — without it the closure captures
    // the initial value and never sees updates.
    val latestScheduleItems by rememberUpdatedState(scheduleItems)
    val latestToolbarExpanded by rememberUpdatedState(isToolbarExpanded)
    val latestOnScrolledToDate by rememberUpdatedState(onScrolledToDate)
    val latestOnToolbarExpandedChanged by rememberUpdatedState(onToolbarExpandedChanged)

    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset
        var lastReportedDate: LocalDate? = null

        snapshotFlow {
            ScrollSnapshot(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
                isScrollInProgress = listState.isScrollInProgress,
                canScrollBackward = listState.canScrollBackward
            )
        }.collect { snap ->
            // ─ Update visible date (only for user scrolls) ─
            if (!isProgrammaticScroll &&
                snap.firstVisibleItemIndex in latestScheduleItems.indices
            ) {
                val date = latestScheduleItems[snap.firstVisibleItemIndex].date
                if (date != lastReportedDate) {
                    lastReportedDate = date
                    latestOnScrolledToDate(date)
                }
            }

            // ─ Collapse on scroll-down, expand on scroll-up ─
            if (snap.isScrollInProgress && !isProgrammaticScroll) {
                val scrollingDown =
                    snap.firstVisibleItemIndex > prevIndex ||
                        (
                            snap.firstVisibleItemIndex == prevIndex &&
                                snap.firstVisibleItemScrollOffset > prevOffset
                            )

                if (scrollingDown && latestToolbarExpanded) {
                    latestOnToolbarExpandedChanged(false)
                } else if (!scrollingDown && !latestToolbarExpanded) {
                    latestOnToolbarExpandedChanged(true)
                }
            }

            prevIndex = snap.firstVisibleItemIndex
            prevOffset = snap.firstVisibleItemScrollOffset
        }
    }

    // ── 5. Layout ────────────────────────────────────────────────────
    Column(modifier = modifier.fillMaxSize()) {
        MonthToolbar(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            isExpanded = isToolbarExpanded,
            eventDots = eventDots,
            onDayClick = { date ->
                onDayClick(date)
                onToolbarExpandedChanged(false)
                internalScrollTarget = date
            },
            onMonthSwiped = { month ->
                onMonthChanged(month)
                internalScrollTarget = month.atDay(1)
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = scheduleItems.size,
                key = { scheduleItems[it].key }
            ) { index ->
                when (val item = scheduleItems[index]) {
                    is ScheduleListItem.Header -> DayHeader(
                        dateText = DateUtils.formatDayMonth(item.date),
                        isToday = DateUtils.isToday(item.date)
                    )

                    is ScheduleListItem.Event -> EventListItem(event = item.event)

                    is ScheduleListItem.NoEvents -> NoEventsPlaceholder()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────

private data class ScrollSnapshot(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val isScrollInProgress: Boolean,
    val canScrollBackward: Boolean
)

/** Flat list items for the schedule LazyColumn. */
sealed class ScheduleListItem(val key: String, val date: LocalDate) {
    class Header(date: LocalDate) : ScheduleListItem("header_$date", date)

    class Event(date: LocalDate, val event: CalendarEvent, index: Int) : ScheduleListItem("event_${date}_${event.id}_$index", date)

    class NoEvents(date: LocalDate) : ScheduleListItem("empty_$date", date)
}

private fun buildScheduleItems(
    dateRange: List<LocalDate>,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>
): List<ScheduleListItem> = buildList {
    dateRange.forEach { date ->
        add(ScheduleListItem.Header(date))
        val dayEvents = eventsByDate[date]
        if (dayEvents.isNullOrEmpty()) {
            add(ScheduleListItem.NoEvents(date))
        } else {
            dayEvents.forEachIndexed { idx, event ->
                add(ScheduleListItem.Event(date, event, idx))
            }
        }
    }
}

private fun buildDateToIndex(
    items: List<ScheduleListItem>
): Map<LocalDate, Int> = buildMap {
    items.forEachIndexed { index, item ->
        if (item is ScheduleListItem.Header) {
            put(item.date, index)
        }
    }
}
