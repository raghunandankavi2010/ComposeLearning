package com.example.composelearning.speechlang.data

import android.content.Context
import com.example.composelearning.speechlang.domain.model.DetectedLanguage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.languagedetector.LanguageDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Wraps the MediaPipe **Tasks Text** `LanguageDetector` behind a coroutine-friendly API.
 *
 * Model creation and inference are both blocking native calls, so they run off the main thread
 * ([Dispatchers.Default]) and are surfaced as suspending functions returning [Result] — the
 * ViewModel decides how to map success/failure into UI state. The detector is created once
 * ([initialize]) and reused; [close] releases its native handle.
 */
class LanguageDetectionRepository(context: Context) {

    private val appContext = context.applicationContext

    @Volatile
    private var detector: LanguageDetector? = null

    /**
     * Loads the model from assets. Idempotent: a second call is a no-op once initialized.
     * Returns [Result.failure] (rather than throwing) if the model asset is missing.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.Default) {
        if (detector != null) return@withContext Result.success(Unit)
        runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_PATH)
                .build()
            val options = LanguageDetector.LanguageDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .build()
            detector = LanguageDetector.createFromOptions(appContext, options)
        }.recoverCatching { cause ->
            throw MissingModelException(cause)
        }
    }

    /**
     * Detects the dominant language of [text]. Returns the highest-probability prediction, or a
     * failure if the detector isn't ready, the text is blank, or nothing was detected.
     */
    suspend fun detect(text: String): Result<DetectedLanguage> = withContext(Dispatchers.Default) {
        runCatching {
            val detector = detector ?: error("Language detector is not initialized")
            require(text.isNotBlank()) { "Nothing to analyse" }

            val prediction = detector.detect(text).languagesAndScores()
                .maxByOrNull { it.probability() }
                ?: error("Couldn't determine the language")

            val code = prediction.languageCode()
            DetectedLanguage(
                code = code,
                displayName = Locale.forLanguageTag(code)
                    .getDisplayLanguage(Locale.ENGLISH)
                    .ifBlank { code },
                confidence = prediction.probability(),
            )
        }
    }

    /** Releases native resources. Call from the owner's teardown. */
    fun close() {
        detector?.close()
        detector = null
    }

    /** Thrown when the model asset can't be loaded, so the UI can show actionable instructions. */
    class MissingModelException(cause: Throwable) : Exception(
        "Couldn't load $MODEL_ASSET_PATH. Add it under app/src/main/assets/ (see SPEECHLANG.md).",
        cause,
    )

    private companion object {
        const val MODEL_ASSET_PATH = "language_detector.tflite"
    }
}
