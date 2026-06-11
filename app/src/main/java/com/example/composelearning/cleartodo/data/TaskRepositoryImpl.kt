package com.example.composelearning.cleartodo.data

import com.example.composelearning.cleartodo.domain.model.TaskItem
import com.example.composelearning.cleartodo.domain.repository.TaskRepository

class TaskRepositoryImpl : TaskRepository {
    override suspend fun getTasks(): List<TaskItem> = listOf(
        TaskItem("Pinch two rows apart to create"),
        TaskItem("Buy groceries"),
        TaskItem("Call the dentist"),
        TaskItem("Finish the Compose port"),
        TaskItem("Water the plants"),
        TaskItem("Read 20 pages"),
        TaskItem("Plan the weekend trip")
    )
}
