package com.example.composelearning.googlecalendar.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WeekRow(
    days: List<LocalDate>,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventDots: Map<LocalDate, Int>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEach { date ->
            DayCell(
                date = date,
                currentMonth = currentMonth,
                isSelected = date == selectedDate,
                eventCount = eventDots[date] ?: 0,
                onClick = onDayClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
