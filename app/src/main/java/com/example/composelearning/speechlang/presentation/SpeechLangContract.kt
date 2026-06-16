package com.example.composelearning.speechlang.presentation

import androidx.compose.runtime.Immutable
import com.example.composelearning.speechlang.domain.model.DetectedLanguage

/** The five UI phases the screen moves through. */
enum class SpeechStatus { IDLE, LISTENING, PROCESSING, SUCCESS, ERROR }

/**
 * A spoken-input language the user can ask `SpeechRecognizer` to expect (via `EXTRA_LANGUAGE`).
 *
 * This matters because detection runs on *text*: forcing the recognizer to transcribe in the
 * correct script (e.g. Tamil → `சாப்பிட்டியா`, not romanized `"sapadiya"`) is what makes the
 * downstream language detection accurate.
 *
 * @property tag BCP-47 tag passed to the recognizer, or `null` for "Auto" (device default).
 */
@Immutable
data class InputLanguage(val label: String, val tag: String?)

/** Selectable spoken-input languages. `null` tag = follow the device default. */
val SupportedInputLanguages: List<InputLanguage> = listOf(
    InputLanguage("Auto (device)", null),
    InputLanguage("English", "en-IN"),
    InputLanguage("Hindi", "hi-IN"),
    InputLanguage("Gujarati", "gu-IN"),
    InputLanguage("Tamil", "ta-IN"),
    InputLanguage("Telugu", "te-IN"),
    InputLanguage("Marathi", "mr-IN"),
    InputLanguage("Kannada", "kn-IN"),
    InputLanguage("Malayalam", "ml-IN"),
    InputLanguage("Bengali", "bn-IN"),
)

/**
 * Single immutable UI state for the speech → language screen.
 *
 * A flat state object (rather than a sealed hierarchy) is used on purpose: the [transcript] and
 * [detectedLanguage] need to stay visible *across* status changes (the transcript card persists
 * while we move LISTENING → PROCESSING → SUCCESS), which a flat object expresses without
 * duplicating those fields in every variant.
 *
 * @property status           current phase, drives the mic button and banners.
 * @property transcript       latest recognized text (partial while listening, final afterwards).
 * @property detectedLanguage populated once MediaPipe finishes, else null.
 * @property detectorReady    false until the MediaPipe model has loaded.
 * @property inputLanguageTag BCP-47 tag the recognizer is told to expect, or null for Auto.
 * @property errorMessage     transient error for the snackbar; cleared via [SpeechLangIntent.ConsumeError].
 */
@Immutable
data class SpeechLangUiState(
    val status: SpeechStatus = SpeechStatus.IDLE,
    val transcript: String = "",
    val detectedLanguage: DetectedLanguage? = null,
    val detectorReady: Boolean = false,
    val inputLanguageTag: String? = null,
    val errorMessage: String? = null,
) {
    val isListening: Boolean get() = status == SpeechStatus.LISTENING
    val isProcessing: Boolean get() = status == SpeechStatus.PROCESSING
}

/** Unidirectional events flowing UI → ViewModel. */
sealed interface SpeechLangIntent {
    /** Tap the mic: start listening when idle, stop when already listening. */
    data object ToggleListening : SpeechLangIntent

    /** Dismiss the error after it's been shown (e.g. snackbar consumed). */
    data object ConsumeError : SpeechLangIntent

    /** Choose which language the recognizer should expect (null = device default). */
    data class SelectInputLanguage(val tag: String?) : SpeechLangIntent
}
