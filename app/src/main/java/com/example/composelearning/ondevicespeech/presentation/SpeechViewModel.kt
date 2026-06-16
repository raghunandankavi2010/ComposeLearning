package com.example.composelearning.ondevicespeech.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.ondevicespeech.data.DownloadResult
import com.example.composelearning.ondevicespeech.data.LanguagePackStatus
import com.example.composelearning.ondevicespeech.data.OnDeviceSpeechRecognizerWrapper
import com.example.composelearning.ondevicespeech.data.SpeechDownloadManager
import com.example.composelearning.speechlang.data.LanguageDetectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates the on-device speech pipeline:
 *  1. **Provision** — check the selected [IndicLocale]'s language pack and download it if missing
 *     ([SpeechDownloadManager]).
 *  2. **Listen** — capture speech on-device ([OnDeviceSpeechRecognizerWrapper]).
 *  3. **Detect** — push the final transcript through the existing MediaPipe
 *     [LanguageDetectionRepository] and surface the detected language.
 *
 * No Hilt (project convention) — see [Factory]. Recognizer callbacks and the platform provisioning
 * APIs run on the main thread (`viewModelScope` is main-dispatched); detection hops to a background
 * dispatcher inside the repository.
 */
class SpeechViewModel(
    appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeechUiState>(SpeechUiState.CheckingLanguagePacks)
    val uiState: StateFlow<SpeechUiState> = _uiState.asStateFlow()

    private val _selectedLocale = MutableStateFlow(IndicLocale.HINDI)
    val selectedLocale: StateFlow<IndicLocale> = _selectedLocale.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val downloadManager = SpeechDownloadManager(appContext)
    private val languageDetection = LanguageDetectionRepository(appContext)

    private val recognizer = OnDeviceSpeechRecognizerWrapper(
        context = appContext.applicationContext,
        onReady = { _uiState.value = SpeechUiState.Listening },
        onPartialResult = { partial -> _partialTranscript.value = partial },
        onFinalResult = ::onTranscript,
        onError = { message -> _uiState.value = SpeechUiState.Error(message) },
        onListeningEnded = ::onListeningEnded,
    )

    /** True only once the selected locale's pack is confirmed present. Gates the mic. */
    private var languageReady = false
    private var provisioningJob: Job? = null

    init {
        // Warm up the MediaPipe detector (fallback pipeline) and provision the default locale.
        viewModelScope.launch { languageDetection.initialize() }
        ensureLanguagePack(_selectedLocale.value)
    }

    fun onIntent(intent: SpeechIntent) {
        when (intent) {
            is SpeechIntent.SelectLocale -> selectLocale(intent.locale)
            SpeechIntent.ToggleListening -> toggleListening()
            SpeechIntent.RetryLanguagePack -> ensureLanguagePack(_selectedLocale.value)
        }
    }

    private fun selectLocale(locale: IndicLocale) {
        if (locale == _selectedLocale.value && languageReady) return
        recognizer.stopListening()
        _selectedLocale.value = locale
        ensureLanguagePack(locale)
    }

    /** Checks the pack, downloading it if needed, and lands on [SpeechUiState.ReadyToListen]. */
    private fun ensureLanguagePack(locale: IndicLocale) {
        provisioningJob?.cancel()
        languageReady = false
        provisioningJob = viewModelScope.launch {
            _uiState.value = SpeechUiState.CheckingLanguagePacks
            when (downloadManager.checkSupport(locale.tag)) {
                LanguagePackStatus.INSTALLED -> markReady()

                LanguagePackStatus.DOWNLOADABLE -> {
                    _uiState.value = SpeechUiState.DownloadingPack(locale.displayName, progress = null)
                    val result = downloadManager.downloadPack(locale.tag) { percent ->
                        _uiState.value = SpeechUiState.DownloadingPack(locale.displayName, percent)
                    }
                    when (result) {
                        DownloadResult.Completed, DownloadResult.Scheduled -> markReady()
                        is DownloadResult.Failed -> _uiState.value = SpeechUiState.Error(
                            "Couldn't download the ${locale.displayName} pack (code ${result.errorCode}).",
                        )
                    }
                }

                LanguagePackStatus.UNSUPPORTED -> _uiState.value = SpeechUiState.Error(
                    "${locale.displayName} isn't supported for on-device recognition on this device.",
                )

                LanguagePackStatus.UNKNOWN -> _uiState.value = SpeechUiState.Error(
                    "Couldn't verify the ${locale.displayName} language pack. Tap retry.",
                )
            }
        }
    }

    private fun markReady() {
        languageReady = true
        _uiState.value = SpeechUiState.ReadyToListen
    }

    private fun toggleListening() {
        if (_uiState.value is SpeechUiState.Listening) {
            recognizer.stopListening()
            return
        }
        if (!languageReady) return
        _partialTranscript.value = ""
        recognizer.startListening(_selectedLocale.value.tag)
    }

    /** Final transcript: run MediaPipe detection, then surface transcript + detected language. */
    private fun onTranscript(text: String) {
        if (text.isBlank()) {
            _uiState.value = SpeechUiState.Error("Didn't catch that — please try again")
            return
        }
        _uiState.value = SpeechUiState.Processing
        viewModelScope.launch {
            val detected = languageDetection.detect(text).getOrNull()
            _uiState.value = SpeechUiState.Success(transcribedText = text, detectedLanguage = detected)
        }
    }

    /** Mic closed with no result (e.g. user tapped stop): return to the ready state. */
    private fun onListeningEnded() {
        if (_uiState.value is SpeechUiState.Listening) {
            _uiState.value = if (languageReady) SpeechUiState.ReadyToListen else SpeechUiState.CheckingLanguagePacks
        }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.destroy()
        downloadManager.close()
        languageDetection.close()
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SpeechViewModel(appContext.applicationContext) as T
    }
}
