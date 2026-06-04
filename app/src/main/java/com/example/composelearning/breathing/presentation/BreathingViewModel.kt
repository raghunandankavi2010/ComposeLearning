package com.example.composelearning.breathing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.breathing.data.BreathingRepositoryImpl
import com.example.composelearning.breathing.domain.usecase.GetBreathingSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BreathingViewModel(
    private val getSession: GetBreathingSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BreathingState())
    val state: StateFlow<BreathingState> = _state.asStateFlow()

    init {
        onIntent(BreathingIntent.Load)
    }

    fun onIntent(intent: BreathingIntent) {
        when (intent) {
            BreathingIntent.Load -> load()
            BreathingIntent.TogglePlay -> _state.update { it.copy(isPlaying = !it.isPlaying) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val session = getSession()
            _state.update { it.copy(isLoading = false, session = session) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BreathingViewModel(GetBreathingSessionUseCase(BreathingRepositoryImpl())) as T
    }
}
