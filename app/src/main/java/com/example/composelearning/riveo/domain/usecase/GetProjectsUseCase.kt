package com.example.composelearning.riveo.domain.usecase

import com.example.composelearning.riveo.domain.model.Project
import com.example.composelearning.riveo.domain.repository.ProjectRepository

/** Fetches the list of project cards to display. */
class GetProjectsUseCase(
    private val repository: ProjectRepository,
) {
    suspend operator fun invoke(): List<Project> = repository.getProjects()
}
