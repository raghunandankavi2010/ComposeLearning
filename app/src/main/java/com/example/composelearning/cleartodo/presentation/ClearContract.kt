package com.example.composelearning.cleartodo.presentation

import com.example.composelearning.cleartodo.domain.model.TaskItem

data class ClearState(
    val isLoading: Boolean = true,
    val tasks: List<TaskItem> = emptyList(),
)

sealed interface ClearIntent {
    data object Load : ClearIntent
    data class CreateTaskAt(val index: Int) : ClearIntent
}
