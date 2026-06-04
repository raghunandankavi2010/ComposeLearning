package com.example.composelearning.cleartodo.domain.repository

import com.example.composelearning.cleartodo.domain.model.TaskItem

interface TaskRepository {
    suspend fun getTasks(): List<TaskItem>
}
