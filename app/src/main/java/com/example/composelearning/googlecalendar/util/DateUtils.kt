package com.example.composelearning.googlecalendar.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateUtils {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val shortDayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dayNumberFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())

    fun formatTime(time: LocalTime): String = time.format(timeFormatter)

    fun formatTime(dateTime: LocalDateTime): String = dateTime.toLocalTime().format(timeFormatter)

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonthFormatter)

    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.format(monthYearFormatter)

    fun formatShortDay(date: LocalDate): String = date.format(shortDayFormatter)

    fun formatDayNumber(date: LocalDate): String = date.format(dayNumberFormatter)

    fun formatEventTimeRange(start: LocalDateTime, end: LocalDateTime): String = "${formatTime(start)} - ${formatTime(end)}"

    /**
     * Returns the start of the week (Sunday) for the given date.
     */
    fun startOfWeek(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    /**
     * Returns the end of the week (Saturday) for the given date.
     */
    fun endOfWeek(date: LocalDate): LocalDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    /**
     * Returns all 7 days of the week containing [date], starting from Sunday.
     */
    fun weekDays(date: LocalDate): List<LocalDate> {
        val start = startOfWeek(date)
        return (0L..6L).map { start.plusDays(it) }
    }

    /**
     * Returns all weeks (as list of 7-day lists) for a given month.
     * Weeks start on Sunday. Days outside the month are included for padding.
     */
    fun monthWeeks(yearMonth: YearMonth): List<List<LocalDate>> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val weekStart = startOfWeek(firstDay)
        val weekEnd = endOfWeek(lastDay)

        val weeks = mutableListOf<List<LocalDate>>()
        var current = weekStart
        while (!current.isAfter(weekEnd)) {
            val week = (0L..6L).map { current.plusDays(it) }
            weeks.add(week)
            current = current.plusWeeks(1)
        }
        return weeks
    }

    /**
     * Returns the week index (0-based) within the month for a given date.
     */
    fun weekIndexInMonth(date: LocalDate, yearMonth: YearMonth): Int {
        val weeks = monthWeeks(yearMonth)
        return weeks.indexOfFirst { week -> week.contains(date) }.coerceAtLeast(0)
    }

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isSameMonth(date: LocalDate, yearMonth: YearMonth): Boolean = date.year == yearMonth.year && date.month == yearMonth.month
}
