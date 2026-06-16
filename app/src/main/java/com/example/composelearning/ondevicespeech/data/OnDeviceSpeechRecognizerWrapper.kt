package com.example.composelearning.ondevicespeech.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Lifecycle-aware wrapper around an **on-device** [SpeechRecognizer]
 * ([SpeechRecognizer.createOnDeviceSpeechRecognizer]).
 *
 * The platform recognizer is stateful, must be created and called on the **main thread**, and
 * **must** be released with [destroy] or it leaks the bound recognition service. Callbacks are set
 * once at construction; the owner ([com.example.composelearning.ondevicespeech.presentation.SpeechViewModel])
 * drives it.
 *
 * @param context          application context used to create the recognizer.
 * @param onReady          listening actually started and the mic is open.
 * @param onPartialResult  best-effort transcript while the user is still speaking.
 * @param onFinalResult    final transcript once speech ends (may be blank → treat as no match).
 * @param onError          human-readable error; listening has stopped.
 * @param onListeningEnded the mic closed (end of speech / stopped), regardless of result.
 */
class OnDeviceSpeechRecognizerWrapper(
    private val context: Context,
    private val onReady: () -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningEnded: () -> Unit,
) {

    private var recognizer: SpeechRecognizer? = null

    /** Whether on-device recognition is available on this device. */
    val isAvailable: Boolean
        get() = SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    /**
     * Starts an on-device, free-form dictation session in [localeTag]. Lazily creates the recognizer
     * on first use. Must be called on the main thread.
     */
    fun startListening(localeTag: String) {
        if (!isAvailable) {
            onError("On-device speech recognition isn't available on this device")
            return
        }
        val recognizer = recognizer ?: createRecognizer().also { this.recognizer = it }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
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
        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            .apply { setRecognitionListener(listener) }

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
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "This language pack isn't installed yet"
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Language pack is still downloading"
        else -> "Speech recognition error ($error)"
    }
}
