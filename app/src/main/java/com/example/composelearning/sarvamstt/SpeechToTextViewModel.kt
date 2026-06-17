package com.example.composelearning.sarvamstt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException

/** UI state machine for the speech-to-text language detection flow. */
sealed interface SpeechUiState {
    data object Idle : SpeechUiState
    data object Recording : SpeechUiState
    data object Uploading : SpeechUiState
    data class Success(
        val transcript: String,
        val languageCode: String,
        val confidence: Double?,
    ) : SpeechUiState
    data class Error(val message: String) : SpeechUiState
}

class SpeechToTextViewModel @JvmOverloads constructor(
    app: Application,
    private val api: SarvamSttApi = SarvamSttClient.create(),
    private val recorder: WavAudioRecorder = WavAudioRecorder(),
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow<SpeechUiState>(SpeechUiState.Idle)
    val uiState: StateFlow<SpeechUiState> = _uiState.asStateFlow()

    private var audioFile: File? = null

    /** Single toggle entry point for the record button. */
    fun onRecordToggle() {
        when (_uiState.value) {
            SpeechUiState.Recording -> stopAndTranscribe()
            SpeechUiState.Uploading -> Unit // ignore taps mid-upload
            else -> startRecording()
        }
    }

    private fun startRecording() {
        val file = File(getApplication<Application>().cacheDir, "sarvam_capture.wav")
        runCatching { recorder.start(file) }
            .onSuccess {
                audioFile = file
                _uiState.value = SpeechUiState.Recording
            }
            .onFailure { _uiState.value = SpeechUiState.Error(it.message ?: "Couldn't start recording") }
    }

    private fun stopAndTranscribe() {
        val file = recorder.stop()
        if (file == null || !file.exists() || file.length() <= 44L) {
            _uiState.value = SpeechUiState.Error("No audio captured. Try again.")
            return
        }
        _uiState.value = SpeechUiState.Uploading

        viewModelScope.launch {
            _uiState.value = runCatching { transcribe(file) }
                .getOrElse { SpeechUiState.Error(it.toUserMessage()) }
        }
    }

    private suspend fun transcribe(file: File): SpeechUiState = withContext(Dispatchers.IO) {
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = file.asRequestBody("audio/wav".toMediaType()),
        )
        val textType = "text/plain".toMediaType()
        val response = api.transcribe(
            file = filePart,
            model = SarvamSttApi.MODEL_SAARAS_V3.toRequestBody(textType),
            mode = SarvamSttApi.MODE_TRANSCRIBE.toRequestBody(textType),
        )

        val language = response.languageCode
        if (language.isNullOrBlank()) {
            SpeechUiState.Error("API returned no language. Speak a bit longer and retry.")
        } else {
            SpeechUiState.Success(
                transcript = response.transcript.orEmpty(),
                languageCode = language,
                confidence = response.languageProbability,
            )
        }
    }

    /** Maps network/API failures onto a user-safe message. */
    private fun Throwable.toUserMessage(): String = when (this) {
        is SocketTimeoutException -> "Request timed out. Check your connection and retry."
        is HttpException -> when (code()) {
            401, 403 -> "Authentication failed — check your Sarvam API key."
            429 -> "Rate limit reached. Try again shortly."
            in 500..599 -> "Sarvam service is unavailable. Try again later."
            else -> "Server error (HTTP ${code()})."
        }
        is IOException -> "No network connection."
        else -> message ?: "Something went wrong."
    }

    override fun onCleared() {
        if (recorder.recording) recorder.stop()
        audioFile?.delete()
    }
}
