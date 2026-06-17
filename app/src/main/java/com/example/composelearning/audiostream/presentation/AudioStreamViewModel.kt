package com.example.composelearning.audiostream.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.audiostream.domain.StreamAudioUseCase
import com.example.composelearning.audiostream.domain.StreamingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Drives the streaming session. Holds a single [Job] for the active stream so that stopping
 * (or [onCleared]) cancels collection — which propagates into [StreamAudioUseCase]'s `finally`
 * to send "END", close the socket, and release `AudioRecord`.
 *
 * Depends only on the use case (which depends only on interfaces), so it's trivially unit-tested
 * with fakes — no Android dependencies here.
 */
class AudioStreamViewModel(
    private val streamAudio: StreamAudioUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioStreamUiState())
    val uiState: StateFlow<AudioStreamUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    /** Single intent entry point for the record button. */
    fun onToggleStreaming() {
        if (streamingJob?.isActive == true) stopStreaming() else startStreaming()
    }

    private fun startStreaming() {
        streamingJob = streamAudio()
            .onEach { state -> _uiState.update { it.copy(state = state) } }
            .catch { e -> _uiState.update { it.copy(state = StreamingState.Error(e.message ?: "Streaming failed")) } }
            .launchIn(viewModelScope)
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.update { it.copy(state = StreamingState.Stopped) }
    }

    override fun onCleared() {
        streamingJob?.cancel()
    }
}
