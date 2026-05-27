package com.example.composelearning.googlecalendar.ui.state

import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val viewMode: ViewMode = ViewMode.SCHEDULE,

    // Events grouped by date for schedule view
    val eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),

    // Event dot counts for the month toolbar
    val eventDotsByDate: Map<LocalDate, Int> = emptyMap(),

    // Events for current day view
    val dayEvents: List<CalendarEvent> = emptyList(),

    // Events for current week view (grouped by date)
    val weekEvents: Map<LocalDate, List<CalendarEvent>> = emptyMap(),

    val isLoading: Boolean = false,
    val error: String? = null
) {
    val selectedWeekStart: LocalDate
        get() = com.example.composelearning.googlecalendar.util.DateUtils.startOfWeek(selectedDate)

    val selectedWeekDays: List<LocalDate>
        get() = com.example.composelearning.googlecalendar.util.DateUtils.weekDays(selectedDate)
}
