package com.example.composelearning.cleartodo.domain.usecase

import com.example.composelearning.cleartodo.domain.model.TaskItem
import com.example.composelearning.cleartodo.domain.repository.TaskRepository

class GetTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): List<TaskItem> = repository.getTasks()
}
