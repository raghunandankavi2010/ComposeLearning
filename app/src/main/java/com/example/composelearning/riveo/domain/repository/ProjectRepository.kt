package com.example.composelearning.riveo.domain.repository

import com.example.composelearning.riveo.domain.model.Project

/** Source of the Riveo project cards. */
interface ProjectRepository {
    suspend fun getProjects(): List<Project>
}
