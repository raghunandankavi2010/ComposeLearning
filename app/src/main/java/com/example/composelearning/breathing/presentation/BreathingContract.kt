package com.example.composelearning.breathing.presentation

import com.example.composelearning.breathing.domain.model.BreathingSession

data class BreathingState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val session: BreathingSession? = null
)

sealed interface BreathingIntent {
    data object Load : BreathingIntent
    data object TogglePlay : BreathingIntent
}
