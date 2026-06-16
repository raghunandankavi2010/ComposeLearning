package com.example.composelearning.speechlang.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Lifecycle-aware wrapper around Android's [SpeechRecognizer].
 *
 * The platform recognizer is stateful, must be created and called on the **main thread**, and
 * **must** be released with [destroy] or it leaks the bound recognition service. This class hides
 * that ceremony behind plain callbacks (set once at construction, in the style of the project's
 * other analyzer wrappers) and is driven entirely from the ViewModel.
 *
 * @param context          application context used to create the recognizer.
 * @param onReady          listening actually started and the mic is open.
 * @param onPartialResult  best-effort transcript while the user is still speaking.
 * @param onFinalResult    final transcript once speech ends (may be blank → treat as no match).
 * @param onError          human-readable error; listening has stopped.
 * @param onListeningEnded the mic closed (end of speech / stopped), regardless of result.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val onReady: () -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningEnded: () -> Unit,
) {

    private var recognizer: SpeechRecognizer? = null

    /** Whether on-device/handheld speech recognition is even available on this device. */
    val isAvailable: Boolean get() = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Starts (or restarts) a free-form dictation session. Lazily creates the recognizer on first
     * use. Must be called on the main thread.
     *
     * @param languageTag preferred input language (defaults to the device locale).
     */
    fun startListening(languageTag: String = Locale.getDefault().toLanguageTag()) {
        if (!isAvailable) {
            onError("Speech recognition is not available on this device")
            return
        }
        val recognizer = recognizer ?: createRecognizer().also { this.recognizer = it }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer.startListening(intent)
    }

    /** Asks the recognizer to stop and emit whatever it has. Safe to call when idle. */
    fun stopListening() {
        recognizer?.stopListening()
    }

    /** Releases the recognizer. Call from the owner's teardown (e.g. `ViewModel.onCleared`). */
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(context).apply { setRecognitionListener(listener) }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = onReady()

        override fun onPartialResults(partialResults: Bundle?) {
            firstResult(partialResults)?.takeIf { it.isNotBlank() }?.let(onPartialResult)
        }

        override fun onResults(results: Bundle?) {
            onFinalResult(firstResult(results).orEmpty())
            onListeningEnded()
        }

        override fun onError(error: Int) {
            onError(errorMessage(error))
            onListeningEnded()
        }

        override fun onEndOfSpeech() = onListeningEnded()

        // Unused callbacks — required by the interface.
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun firstResult(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Recognition client error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — please try again"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
        SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
        else -> "Speech recognition error ($error)"
    }
}
