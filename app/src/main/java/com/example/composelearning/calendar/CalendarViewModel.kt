package com.example.composelearning.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.calendar.model.CalendarState
import java.time.LocalDate
import kotlinx.coroutines.launch

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
