package com.example.composelearning.audiostream.presentation

import com.example.composelearning.audiostream.domain.StreamingState

/** Immutable UI state for the audio-streaming screen (unidirectional data flow). */
data class AudioStreamUiState(
    val state: StreamingState = StreamingState.Idle,
) {
    val isActive: Boolean
        get() = state is StreamingState.Connecting || state is StreamingState.Streaming

    val bytesSent: Long
        get() = (state as? StreamingState.Streaming)?.bytesSent ?: 0L
}
