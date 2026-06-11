package com.example.composelearning.googlecalendar.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.provider.CalendarContract
import androidx.compose.ui.graphics.Color
import com.example.composelearning.googlecalendar.data.model.CalendarEventEntity
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarRepositoryImpl(
    private val contentResolver: ContentResolver
) : CalendarRepository {

    companion object {
        private val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DISPLAY_COLOR,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION
        )

        private const val COL_EVENT_ID = 0
        private const val COL_TITLE = 1
        private const val COL_BEGIN = 2
        private const val COL_END = 3
        private const val COL_ALL_DAY = 4
        private const val COL_COLOR = 5
        private const val COL_CALENDAR_NAME = 6
        private const val COL_LOCATION = 7
        private const val COL_DESCRIPTION = 8

        // Default colors when calendar has no color set
        private val DEFAULT_COLORS = listOf(
            Color(0xFF4285F4), // Google Blue
            Color(0xFF0B8043), // Green
            Color(0xFFC53929), // Red
            Color(0xFFF4511E), // Orange
            Color(0xFF7986CB), // Lavender
            Color(0xFF616161) // Graphite
        )
    }

    override suspend fun getEvents(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val entities = mutableListOf<CalendarEventEntity>()
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                builder.build(),
                INSTANCE_PROJECTION,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )
            cursor?.let {
                while (it.moveToNext()) {
                    entities.add(cursorToEntity(it))
                }
            }
        } finally {
            cursor?.close()
        }

        entities.map { mapToDomain(it) }
    }

    override suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent> = getEvents(date, date)

    override suspend fun addEvent(event: CalendarEvent): CalendarEvent = withContext(Dispatchers.IO) {
        val calendarId = findWritableCalendarId()
            ?: error("No writable calendar found on this device")

        val zone = ZoneId.systemDefault()
        val startMillis = event.startTime.atZone(zone).toInstant().toEpochMilli()
        val endMillis = event.endTime.atZone(zone).toInstant().toEpochMilli()

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (event.description.isNotBlank()) {
                put(CalendarContract.Events.DESCRIPTION, event.description)
            }
            if (event.location.isNotBlank()) {
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
            }
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: error("Failed to insert event")
        val newId = ContentUris.parseId(uri)
        event.copy(id = newId)
    }

    private fun findWritableCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.VISIBLE
        )
        // Owner or contributor access is required to insert events.
        val selection = "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?"
        val args = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                args,
                "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
            )
            if (cursor != null && cursor.moveToFirst()) cursor.getLong(0) else null
        } finally {
            cursor?.close()
        }
    }

    private fun cursorToEntity(cursor: Cursor): CalendarEventEntity = CalendarEventEntity(
        eventId = cursor.getLong(COL_EVENT_ID),
        title = cursor.getString(COL_TITLE) ?: "",
        dtStart = cursor.getLong(COL_BEGIN),
        dtEnd = cursor.getLong(COL_END),
        allDay = cursor.getInt(COL_ALL_DAY) == 1,
        eventColor = cursor.getInt(COL_COLOR),
        calendarName = cursor.getString(COL_CALENDAR_NAME) ?: "",
        eventLocation = cursor.getString(COL_LOCATION) ?: "",
        description = cursor.getString(COL_DESCRIPTION) ?: ""
    )

    private fun mapToDomain(entity: CalendarEventEntity): CalendarEvent {
        val zone = ZoneId.systemDefault()
        val startDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.dtStart),
            zone
        )
        val endDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.dtEnd),
            zone
        )

        val color = if (entity.eventColor != 0) {
            Color(entity.eventColor)
        } else {
            // Deterministic color based on event id
            DEFAULT_COLORS[(entity.eventId % DEFAULT_COLORS.size).toInt()]
        }

        return CalendarEvent(
            id = entity.eventId,
            title = entity.title.ifBlank { "(No title)" },
            startTime = startDateTime,
            endTime = endDateTime,
            isAllDay = entity.allDay,
            color = color,
            calendarName = entity.calendarName,
            location = entity.eventLocation,
            description = entity.description
        )
    }
}
