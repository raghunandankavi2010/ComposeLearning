package com.example.composelearning.riveo.data

import com.example.composelearning.riveo.domain.model.Project
import com.example.composelearning.riveo.domain.repository.ProjectRepository

/**
 * In-memory sample data (no backend). Images come from seeded picsum.photos URLs so the
 * same photo is returned every time, matching the original Riveo demo's three projects.
 */
class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun getProjects(): List<Project> = listOf(
        Project(
            id = "zurich",
            title = "Zürich",
            size = "45MB",
            duration = "1:06m",
            imageUrl = "https://picsum.photos/seed/riveo-zurich/1080/640",
            accentColor = 0xFFBDA098,
        ),
        Project(
            id = "oslo",
            title = "Oslo",
            size = "1GB",
            duration = "5:02m",
            imageUrl = "https://picsum.photos/seed/riveo-oslo/1080/640",
            accentColor = 0xFF59659A,
        ),
        Project(
            id = "krakow",
            title = "Kraków",
            size = "500MB",
            duration = "11:04m",
            imageUrl = "https://picsum.photos/seed/riveo-krakow/1080/640",
            accentColor = 0xFFBAB9B0,
        ),
    )
}
