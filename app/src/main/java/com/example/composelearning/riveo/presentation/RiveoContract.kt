package com.example.composelearning.riveo.presentation

import com.example.composelearning.riveo.domain.model.Project

/**
 * MVI contract for the Riveo screen.
 *
 * Note: the curl/drag values are intentionally NOT part of this state. They update at
 * ~60fps while dragging and are pure view animation state, so they live locally in
 * [PageCurlCard] (an [androidx.compose.animation.core.Animatable]). The ViewModel owns
 * only the durable business state — the list of projects.
 */
data class RiveoState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
)

sealed interface RiveoIntent {
    data object LoadProjects : RiveoIntent
}
