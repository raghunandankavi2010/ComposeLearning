package com.example.composelearning.speechlang.domain.model

/**
 * A single language-detection result.
 *
 * Pure data — no Android or MediaPipe types — so the ViewModel's state machine is unit-testable on
 * a plain JVM.
 *
 * @property code        BCP-47 / ISO code, e.g. `"en"`, `"hi"`, `"ta"`.
 * @property displayName human-readable name resolved from [code], e.g. `"Tamil"`.
 * @property confidence  model probability in `[0, 1]` for this language.
 */
data class DetectedLanguage(
    val code: String,
    val displayName: String,
    val confidence: Float,
)
