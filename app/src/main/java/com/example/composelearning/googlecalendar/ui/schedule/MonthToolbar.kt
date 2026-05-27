package com.example.composelearning.googlecalendar.ui.schedule

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.googlecalendar.util.DateUtils
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Fully-collapsible month grid with horizontal month paging.
 *
 * When [isExpanded] is **true**, the full month grid (day-of-week header +
 * all week rows) is visible. When **false**, the entire toolbar collapses
 * to zero height — matching the real Google Calendar behaviour where only
 * the top-bar "June 2026 ▾" arrow controls visibility.
 */

private val WEEK_ROW_HEIGHT: Dp = 48.dp
private val DAY_HEADER_HEIGHT: Dp = 28.dp
private const val MAX_WEEKS = 6
private const val MONTH_PAGE_COUNT = 25   // ±12 months
private const val MONTH_CENTER_PAGE = 12

@Composable
fun MonthToolbar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    isExpanded: Boolean,
    eventDots: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit,
    onMonthSwiped: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseMonth = remember { YearMonth.now() }
    val pagerState = rememberPagerState(initialPage = MONTH_CENTER_PAGE) { MONTH_PAGE_COUNT }

    // ── Sync pager ← currentMonth (driven by event-list scroll) ─────
    LaunchedEffect(currentMonth) {
        val diff = monthDiff(baseMonth, currentMonth)
        val target = (MONTH_CENTER_PAGE + diff).coerceIn(0, MONTH_PAGE_COUNT - 1)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    // ── Sync pager → onMonthSwiped (user swipes the grid) ───────────
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val month = baseMonth.plusMonths((page - MONTH_CENTER_PAGE).toLong())
                if (month != currentMonth) onMonthSwiped(month)
            }
    }

    // ── Animated height: always 6-row grid (no resize on month switch) ─
    val expandedHeight = DAY_HEADER_HEIGHT + WEEK_ROW_HEIGHT * MAX_WEEKS

    val targetHeight by animateDpAsState(
        targetValue = if (isExpanded) expandedHeight else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "toolbar_height"
    )

    // The outer Box clips to the animated height.
    // The inner Column uses requiredHeight so the pager always gets
    // a proper measurement even when the Box is 0dp.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(targetHeight)
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(expandedHeight)
        ) {
            DayOfWeekHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DAY_HEADER_HEIGHT)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val month = baseMonth.plusMonths((page - MONTH_CENTER_PAGE).toLong())
                val weeks = remember(month) { DateUtils.monthWeeks(month) }

                // Pad to 6 rows so every month has the same grid height
                val paddedWeeks = remember(weeks) {
                    if (weeks.size < MAX_WEEKS) {
                        val lastWeekEnd = weeks.last().last()
                        val extra = (weeks.size until MAX_WEEKS).map { i ->
                            val start = lastWeekEnd.plusDays(1L + (i - weeks.size) * 7L)
                            (0L..6L).map { start.plusDays(it) }
                        }
                        weeks + extra
                    } else weeks
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    paddedWeeks.forEach { weekDays ->
                        WeekRow(
                            days = weekDays,
                            currentMonth = month,
                            selectedDate = selectedDate,
                            eventDots = eventDots,
                            onDayClick = onDayClick,
                            modifier = Modifier.height(WEEK_ROW_HEIGHT)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Internal components
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun DayOfWeekHeader(modifier: Modifier = Modifier) {
    val days = remember {
        listOf(
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
    }

    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────

private fun monthDiff(from: YearMonth, to: YearMonth): Int {
    return (to.year - from.year) * 12 + (to.monthValue - from.monthValue)
}

private fun isSameMonth(date: LocalDate, month: YearMonth): Boolean {
    return date.year == month.year && date.monthValue == month.monthValue
}
