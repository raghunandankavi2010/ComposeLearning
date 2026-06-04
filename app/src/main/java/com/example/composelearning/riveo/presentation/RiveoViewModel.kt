package com.example.composelearning.riveo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.riveo.data.ProjectRepositoryImpl
import com.example.composelearning.riveo.domain.usecase.GetProjectsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel: holds [RiveoState] in a [StateFlow] and reduces [RiveoIntent]s.
 * No Hilt (per project conventions) — see [Factory] for manual construction.
 */
class RiveoViewModel(
    private val getProjects: GetProjectsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RiveoState())
    val state: StateFlow<RiveoState> = _state.asStateFlow()

    init {
        onIntent(RiveoIntent.LoadProjects)
    }

    fun onIntent(intent: RiveoIntent) {
        when (intent) {
            RiveoIntent.LoadProjects -> loadProjects()
        }
    }

    private fun loadProjects() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val projects = getProjects()
            _state.update { it.copy(isLoading = false, projects = projects) }
        }
    }

    /** Manual factory — wires the data → domain → presentation graph. */
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val useCase = GetProjectsUseCase(ProjectRepositoryImpl())
            return RiveoViewModel(useCase) as T
        }
    }
}
