package com.example.composelearning.applerings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.applerings.data.ActivityRingsRepositoryImpl
import com.example.composelearning.applerings.domain.usecase.GetActivityRingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivityRingsViewModel(
    private val getRings: GetActivityRingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityRingsState())
    val state: StateFlow<ActivityRingsState> = _state.asStateFlow()

    init {
        onIntent(ActivityRingsIntent.Load)
    }

    fun onIntent(intent: ActivityRingsIntent) {
        when (intent) {
            ActivityRingsIntent.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val rings = getRings()
            _state.update { it.copy(isLoading = false, rings = rings) }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ActivityRingsViewModel(GetActivityRingsUseCase(ActivityRingsRepositoryImpl())) as T
    }
}
