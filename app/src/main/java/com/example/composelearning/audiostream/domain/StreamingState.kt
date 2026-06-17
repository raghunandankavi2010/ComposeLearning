package com.example.composelearning.audiostream.domain

/** Single source of truth for the streaming session, emitted by [StreamAudioUseCase]. */
sealed interface StreamingState {
    data object Idle : StreamingState
    data object Connecting : StreamingState
    data class Streaming(val bytesSent: Long = 0L) : StreamingState
    data object Stopped : StreamingState
    data class Error(val message: String) : StreamingState
}
