package com.example.composelearning.googlecalendar.domain.usecase

import com.example.composelearning.googlecalendar.data.CalendarRepository
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth

class GetEventsUseCase(
    private val repository: CalendarRepository
) {

    suspend fun getEventsForMonth(yearMonth: YearMonth): List<CalendarEvent> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return repository.getEvents(start, end)
    }

    suspend fun getEventsForRange(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent> {
        return repository.getEvents(startDate, endDate)
    }

    suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent> {
        return repository.getEventsForDay(date)
    }

    /**
     * Returns events grouped by date for the schedule view.
     * Each date key maps to the list of events occurring on that date.
     */
    suspend fun getEventsGroupedByDay(
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, List<CalendarEvent>> {
        val events = repository.getEvents(startDate, endDate)
        val grouped = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()

        for (event in events) {
            // Multi-day events appear under each day they span
            var date = event.startDate.coerceAtLeast(startDate)
            val lastDate = event.endDate.coerceAtMost(endDate)
            while (!date.isAfter(lastDate)) {
                grouped.getOrPut(date) { mutableListOf() }.add(event)
                date = date.plusDays(1)
            }
        }

        // Sort events within each day: all-day first, then by start time
        return grouped.mapValues { (_, events) ->
            events.sortedWith(
                compareByDescending<CalendarEvent> { it.isAllDay }
                    .thenBy { it.startTime }
            )
        }
    }

    /**
     * Returns dates that have at least one event in the given month.
     * Used for rendering event dots on the month toolbar.
     */
    suspend fun getEventDatesForMonth(yearMonth: YearMonth): Map<LocalDate, Int> {
        val events = getEventsForMonth(yearMonth)
        val dateCounts = mutableMapOf<LocalDate, Int>()

        for (event in events) {
            var date = event.startDate.coerceAtLeast(yearMonth.atDay(1))
            val lastDate = event.endDate.coerceAtMost(yearMonth.atEndOfMonth())
            while (!date.isAfter(lastDate)) {
                dateCounts[date] = (dateCounts[date] ?: 0) + 1
                date = date.plusDays(1)
            }
        }
        return dateCounts
    }
}
