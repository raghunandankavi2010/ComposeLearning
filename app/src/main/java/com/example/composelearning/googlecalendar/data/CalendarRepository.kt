package com.example.composelearning.googlecalendar.data

import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import java.time.LocalDate

interface CalendarRepository {
    suspend fun getEvents(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent>
    suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent>
}
