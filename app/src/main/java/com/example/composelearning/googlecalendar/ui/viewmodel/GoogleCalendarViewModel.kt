package com.example.composelearning.googlecalendar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.googlecalendar.domain.model.CalendarEvent
import com.example.composelearning.googlecalendar.domain.usecase.GetEventsUseCase
import com.example.composelearning.googlecalendar.ui.state.CalendarUiState
import com.example.composelearning.googlecalendar.ui.state.ViewMode
import com.example.composelearning.googlecalendar.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoogleCalendarViewModel(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, currentMonth = YearMonth.from(date)) }
        loadEventsForCurrentView()
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth) }
        loadMonthDots(yearMonth)
    }

    fun onViewModeChanged(viewMode: ViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
        loadEventsForCurrentView()
    }

    fun onScrolledToDate(date: LocalDate) {
        val currentMonth = _uiState.value.currentMonth
        val newMonth = YearMonth.from(date)
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentMonth = newMonth
            )
        }
        if (newMonth != currentMonth) {
            loadMonthDots(newMonth)
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDate = today,
                currentMonth = YearMonth.now()
            )
        }
        loadEventsForCurrentView()
    }

    fun loadEvents() {
        loadEventsForCurrentView()
        loadMonthDots(_uiState.value.currentMonth)
    }

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            getEventsUseCase.addEvent(event)
            // Refresh everything so the new event appears in Schedule, Day, Week and dot view.
            loadEventsForCurrentView()
            loadMonthDots(_uiState.value.currentMonth)
        }
    }

    private fun loadEventsForCurrentView() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val state = _uiState.value
                when (state.viewMode) {
                    ViewMode.SCHEDULE -> loadScheduleEvents(state.selectedDate)
                    ViewMode.DAY -> loadDayEvents(state.selectedDate)
                    ViewMode.WEEK -> loadWeekEvents(state.selectedDate)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadScheduleEvents(baseDate: LocalDate) {
        // Load 2 months of events for infinite scrolling
        val startDate = baseDate.minusMonths(1).withDayOfMonth(1)
        val endDate = baseDate.plusMonths(2).withDayOfMonth(1).minusDays(1)
        val grouped = getEventsUseCase.getEventsGroupedByDay(startDate, endDate)
        _uiState.update { it.copy(eventsByDate = grouped, isLoading = false) }
    }

    private suspend fun loadDayEvents(date: LocalDate) {
        val events = getEventsUseCase.getEventsForDay(date)
        _uiState.update { it.copy(dayEvents = events, isLoading = false) }
    }

    private suspend fun loadWeekEvents(date: LocalDate) {
        val weekStart = DateUtils.startOfWeek(date)
        val weekEnd = DateUtils.endOfWeek(date)
        val grouped = getEventsUseCase.getEventsGroupedByDay(weekStart, weekEnd)
        _uiState.update { it.copy(weekEvents = grouped, isLoading = false) }
    }

    private fun loadMonthDots(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                val dots = getEventsUseCase.getEventDatesForMonth(yearMonth)
                _uiState.update { it.copy(eventDotsByDate = dots) }
            } catch (_: Exception) {
                /* Silently ignore dot loading failures */
            }
        }
    }

    class Factory(
        private val getEventsUseCase: GetEventsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GoogleCalendarViewModel(getEventsUseCase) as T
    }
}
