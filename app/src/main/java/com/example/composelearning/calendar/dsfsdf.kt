package com.example.composelearning.calendar


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.YearMonth
import java.time.temporal.WeekFields

@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.getNumberWeeks(weekFields: WeekFields = CALENDAR_STARTS_ON): Int {
    val firstWeekNumber = this.atDay(1)[weekFields.weekOfMonth()]
    val lastWeekNumber = this.atEndOfMonth()[weekFields.weekOfMonth()]
    return lastWeekNumber - firstWeekNumber + 1 // Both weeks inclusive
}
