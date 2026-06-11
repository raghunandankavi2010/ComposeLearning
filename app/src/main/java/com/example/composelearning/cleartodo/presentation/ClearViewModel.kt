package com.example.composelearning.cleartodo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.cleartodo.data.TaskRepositoryImpl
import com.example.composelearning.cleartodo.domain.model.TaskItem
import com.example.composelearning.cleartodo.domain.usecase.GetTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClearViewModel(
    private val getTasks: GetTasksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ClearState())
    val state: StateFlow<ClearState> = _state.asStateFlow()

    init {
        onIntent(ClearIntent.Load)
    }

    fun onIntent(intent: ClearIntent) {
        when (intent) {
            ClearIntent.Load -> viewModelScope.launch {
                val tasks = getTasks()
                _state.update { it.copy(isLoading = false, tasks = tasks) }
            }

            is ClearIntent.CreateTaskAt -> _state.update { s ->
                val index = intent.index.coerceIn(0, s.tasks.size)
                val updated = s.tasks.toMutableList().apply { add(index, TaskItem("New Task")) }
                s.copy(tasks = updated)
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ClearViewModel(GetTasksUseCase(TaskRepositoryImpl())) as T
    }
}
