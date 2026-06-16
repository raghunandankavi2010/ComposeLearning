package com.example.composelearning.ondevicespeech.presentation

import androidx.compose.runtime.Immutable
import com.example.composelearning.speechlang.domain.model.DetectedLanguage

/**
 * The six Indian languages this on-device speech layer targets. [tag] is the BCP-47 locale fed to
 * the recognizer and used to check/provision its language pack.
 */
enum class IndicLocale(val tag: String, val displayName: String) {
    HINDI("hi-IN", "Hindi"),
    TAMIL("ta-IN", "Tamil"),
    TELUGU("te-IN", "Telugu"),
    MARATHI("mr-IN", "Marathi"),
    GUJARATI("gu-IN", "Gujarati"),
    KANNADA("kn-IN", "Kannada"),
}

/**
 * The screen's phase machine, exposed as `StateFlow<SpeechUiState>`.
 *
 * Provisioning (checking/downloading the language pack) precedes dictation, so those phases are
 * first-class states rather than flags.
 */
sealed interface SpeechUiState {
    /** Verifying whether the selected locale's pack is installed. */
    @Immutable
    data object CheckingLanguagePacks : SpeechUiState

    /** Pack is missing and downloading. [progress] is 0–100, or null when it can't be tracked. */
    @Immutable
    data class DownloadingPack(val locale: String, val progress: Int?) : SpeechUiState

    /** Pack is present; the mic is ready. */
    @Immutable
    data object ReadyToListen : SpeechUiState

    /** Actively recording. */
    @Immutable
    data object Listening : SpeechUiState

    /** Speech ended; running the transcript through MediaPipe language detection. */
    @Immutable
    data object Processing : SpeechUiState

    /** Final transcript plus the detected language (null if detection failed). */
    @Immutable
    data class Success(
        val transcribedText: String,
        val detectedLanguage: DetectedLanguage?,
    ) : SpeechUiState

    /** Unrecoverable error for this attempt. */
    @Immutable
    data class Error(val message: String) : SpeechUiState
}

/** Unidirectional events flowing UI → ViewModel. */
sealed interface SpeechIntent {
    /** Choose a different target language (re-checks/provisions its pack). */
    data class SelectLocale(val locale: IndicLocale) : SpeechIntent

    /** Tap the mic: start when ready, stop when listening. */
    data object ToggleListening : SpeechIntent

    /** Retry provisioning the current locale after an error. */
    data object RetryLanguagePack : SpeechIntent
}
