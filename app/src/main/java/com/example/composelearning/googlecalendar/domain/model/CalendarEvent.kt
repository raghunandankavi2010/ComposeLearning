package com.example.composelearning.googlecalendar.domain.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean,
    val color: Color,
    val calendarName: String = "",
    val location: String = "",
    val description: String = ""
) {
    val startDate: LocalDate get() = startTime.toLocalDate()
    val endDate: LocalDate get() = endTime.toLocalDate()

    val displayStartTime: LocalTime get() = startTime.toLocalTime()
    val displayEndTime: LocalTime get() = endTime.toLocalTime()

    val durationMinutes: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()

    fun overlaps(other: CalendarEvent): Boolean = startTime < other.endTime && endTime > other.startTime

    fun occursOnDate(date: LocalDate): Boolean = !startDate.isAfter(date) && !endDate.isBefore(date)
}
