package com.example.composelearning.ondevicespeech.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.ModelDownloadListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** Whether a given BCP-47 language pack is present for on-device recognition. */
enum class LanguagePackStatus { INSTALLED, DOWNLOADABLE, UNSUPPORTED, UNKNOWN }

/** Outcome of a [SpeechDownloadManager.downloadPack] request. */
sealed interface DownloadResult {
    /** Pack finished downloading and is ready (only reported on API 34+ with progress tracking). */
    data object Completed : DownloadResult

    /** Download was scheduled by the framework; completion can't be tracked on this API level. */
    data object Scheduled : DownloadResult

    /** Download failed with the platform [errorCode]. */
    data class Failed(val errorCode: Int) : DownloadResult
}

/**
 * Verifies and provisions on-device speech language packs.
 *
 * Wraps the callback-based platform APIs ([SpeechRecognizer.checkRecognitionSupport] and
 * [SpeechRecognizer.triggerModelDownload]) as suspending functions via
 * [suspendCancellableCoroutine]. The recognizer it owns is used purely for provisioning — actual
 * dictation lives in [OnDeviceSpeechRecognizerWrapper].
 *
 * Platform methods on [SpeechRecognizer] must be called from the main thread, so the suspend
 * functions here are expected to be invoked from a main-dispatcher coroutine (e.g. `viewModelScope`).
 * Their result callbacks fire on [executor]; resuming the continuation from that worker thread is
 * safe.
 */
class SpeechDownloadManager(context: Context) {

    private val appContext = context.applicationContext
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var recognizer: SpeechRecognizer? = null

    /** True if this device exposes an on-device recognition service at all. */
    val isOnDeviceAvailable: Boolean
        get() = SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)

    /** Lazily creates the on-device recognizer. Must run on the main thread. */
    private fun obtainRecognizer(): SpeechRecognizer =
        recognizer ?: SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
            .also { recognizer = it }

    /**
     * Asks the framework whether [localeTag]'s pack is installed, downloadable, or unsupported.
     */
    suspend fun checkSupport(localeTag: String): LanguagePackStatus =
        suspendCancellableCoroutine { continuation ->
            if (!isOnDeviceAvailable) {
                continuation.resume(LanguagePackStatus.UNSUPPORTED)
                return@suspendCancellableCoroutine
            }
            val recognizer = obtainRecognizer()
            recognizer.checkRecognitionSupport(
                recognizerIntent(localeTag),
                executor,
                object : RecognitionSupportCallback {
                    override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                        val status = when {
                            matches(recognitionSupport.installedOnDeviceLanguages, localeTag) ->
                                LanguagePackStatus.INSTALLED
                            matches(recognitionSupport.pendingOnDeviceLanguages, localeTag) ||
                                matches(recognitionSupport.supportedOnDeviceLanguages, localeTag) ->
                                LanguagePackStatus.DOWNLOADABLE
                            else -> LanguagePackStatus.UNSUPPORTED
                        }
                        if (continuation.isActive) continuation.resume(status)
                    }

                    override fun onError(error: Int) {
                        if (continuation.isActive) continuation.resume(LanguagePackStatus.UNKNOWN)
                    }
                },
            )
        }

    /**
     * Triggers a download of [localeTag]'s pack. On API 34+ progress is streamed to [onProgress]
     * and the function suspends until the framework reports success/failure. On API 33 there is no
     * progress listener, so the download is fire-and-forget and the result is [DownloadResult.Scheduled].
     */
    suspend fun downloadPack(
        localeTag: String,
        onProgress: (Int) -> Unit,
    ): DownloadResult {
        val recognizer = obtainRecognizer()
        val intent = recognizerIntent(localeTag)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 33: fire-and-forget — no listener overload available.
            recognizer.triggerModelDownload(intent)
            return DownloadResult.Scheduled
        }

        return suspendCancellableCoroutine { continuation ->
            recognizer.triggerModelDownload(
                intent,
                executor,
                object : ModelDownloadListener {
                    override fun onProgress(completedPercent: Int) = onProgress(completedPercent)

                    override fun onSuccess() {
                        if (continuation.isActive) continuation.resume(DownloadResult.Completed)
                    }

                    override fun onScheduled() {
                        // Queued by the framework; resolve so the UI stops blocking on progress.
                        if (continuation.isActive) continuation.resume(DownloadResult.Scheduled)
                    }

                    override fun onError(error: Int) {
                        if (continuation.isActive) continuation.resume(DownloadResult.Failed(error))
                    }
                },
            )
        }
    }

    /** Releases the provisioning recognizer and the worker executor. */
    fun close() {
        recognizer?.destroy()
        recognizer = null
        (executor as? ExecutorService)?.shutdown()
    }

    private fun recognizerIntent(localeTag: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

    /** Tolerant match: exact tag, or same primary language ignoring region/case (hi == hi-IN). */
    private fun matches(tags: List<String>, target: String): Boolean {
        val targetLang = Locale.forLanguageTag(target).language
        return tags.any { tag ->
            tag.equals(target, ignoreCase = true) ||
                Locale.forLanguageTag(tag).language.equals(targetLang, ignoreCase = true)
        }
    }
}
