package com.example.composelearning.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.composelearning.calendar.model.CalendarState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    val calendarState = CalendarState()

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDaySelected(daySelected: LocalDate) {
        viewModelScope.launch {
            calendarState.setSelectedDay(daySelected)
        }
    }
}
