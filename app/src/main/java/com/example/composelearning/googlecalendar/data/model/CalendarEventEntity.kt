package com.example.composelearning.googlecalendar.data.model

/**
 * Raw event data from CalendarContract.Instances.
 */
data class CalendarEventEntity(
    val eventId: Long,
    val title: String,
    val dtStart: Long,      // epoch millis
    val dtEnd: Long,         // epoch millis
    val allDay: Boolean,
    val eventColor: Int,     // calendar display color (ARGB int)
    val calendarName: String,
    val eventLocation: String,
    val description: String
)
