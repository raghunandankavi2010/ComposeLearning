package com.example.composelearning.googlecalendar.domain.model

/**
 * A CalendarEvent enriched with layout position information for day/week views.
 * [column] is the 0-based column index within an overlap group.
 * [totalColumns] is how many columns the overlap group spans.
 */
data class LayoutEvent(
    val event: CalendarEvent,
    val column: Int,
    val totalColumns: Int
)
