package com.example.composelearning.speechlang.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.speechlang.data.LanguageDetectionRepository
import com.example.composelearning.speechlang.data.SpeechRecognizerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the speech → language-detection screen. No Hilt (project convention) — see
 * [Factory].
 *
 * Responsibilities:
 *  - owns the [SpeechRecognizerManager] lifecycle (destroyed in [onCleared] so it never leaks),
 *  - initializes the MediaPipe [LanguageDetectionRepository] asynchronously off the main thread,
 *  - runs the IDLE → LISTENING → PROCESSING → SUCCESS / ERROR state machine over a single
 *    [StateFlow].
 *
 * The recognizer's callbacks arrive on the main thread; detection is dispatched to a background
 * dispatcher inside the repository.
 */
class SpeechLangViewModel(
    appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SpeechLangUiState())
    val state: StateFlow<SpeechLangUiState> = _state.asStateFlow()

    private val languageDetection = LanguageDetectionRepository(appContext)

    private val speechRecognizer = SpeechRecognizerManager(
        context = appContext.applicationContext,
        onReady = { _state.update { it.copy(status = SpeechStatus.LISTENING) } },
        onPartialResult = { partial -> _state.update { it.copy(transcript = partial) } },
        onFinalResult = ::onTranscript,
        onError = { message ->
            _state.update { it.copy(status = SpeechStatus.ERROR, errorMessage = message) }
        },
        onListeningEnded = ::onListeningEnded,
    )

    init {
        // Warm up the model off the main thread; surface a clear error if the asset is missing.
        viewModelScope.launch {
            languageDetection.initialize()
                .onSuccess { _state.update { it.copy(detectorReady = true) } }
                .onFailure { cause ->
                    _state.update {
                        it.copy(
                            status = SpeechStatus.ERROR,
                            errorMessage = cause.message ?: "Failed to load the language model",
                        )
                    }
                }
        }
    }

    fun onIntent(intent: SpeechLangIntent) {
        when (intent) {
            SpeechLangIntent.ToggleListening -> toggleListening()
            SpeechLangIntent.ConsumeError -> _state.update { it.copy(errorMessage = null) }
            is SpeechLangIntent.SelectInputLanguage ->
                _state.update { it.copy(inputLanguageTag = intent.tag) }
        }
    }

    private fun toggleListening() {
        if (_state.value.isListening) {
            speechRecognizer.stopListening()
            return
        }
        val languageTag = _state.value.inputLanguageTag
        _state.update {
            it.copy(
                status = SpeechStatus.LISTENING,
                transcript = "",
                detectedLanguage = null,
                errorMessage = null,
            )
        }
        // null tag → let the manager fall back to the device default locale.
        if (languageTag != null) {
            speechRecognizer.startListening(languageTag)
        } else {
            speechRecognizer.startListening()
        }
    }

    /** Final transcript received: move to PROCESSING and kick off detection (or error on blank). */
    private fun onTranscript(text: String) {
        if (text.isBlank()) {
            _state.update {
                it.copy(status = SpeechStatus.ERROR, errorMessage = "Didn't catch that — please try again")
            }
            return
        }
        _state.update { it.copy(transcript = text, status = SpeechStatus.PROCESSING) }
        viewModelScope.launch {
            languageDetection.detect(text)
                .onSuccess { language ->
                    _state.update { it.copy(status = SpeechStatus.SUCCESS, detectedLanguage = language) }
                }
                .onFailure { cause ->
                    _state.update {
                        it.copy(
                            status = SpeechStatus.ERROR,
                            errorMessage = cause.message ?: "Language detection failed",
                        )
                    }
                }
        }
    }

    /** The mic closed. If we were still in LISTENING with no result, fall back to IDLE. */
    private fun onListeningEnded() {
        _state.update { if (it.status == SpeechStatus.LISTENING) it.copy(status = SpeechStatus.IDLE) else it }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        languageDetection.close()
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SpeechLangViewModel(appContext.applicationContext) as T
    }
}
