package com.example.composelearning.geministt

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.sarvamstt.WavAudioRecorder
import com.google.firebase.Firebase
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class GeminiSttViewModel(application: Application) : AndroidViewModel(application) {

    private val recorder = WavAudioRecorder()
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    var isRecording by mutableStateOf(false)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    private val _transcript = MutableStateFlow("")
    val transcript = _transcript.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var audioFile: File? = null

    fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val file = File(getApplication<Application>().cacheDir, "recording.wav")
        audioFile = file
        recorder.start(file)
        isRecording = true
        _error.value = null
    }

    private fun stopRecording() {
        recorder.stop()
        isRecording = false
        processAudio()
    }

    private fun processAudio() {
        val file = audioFile ?: return
        if (!file.exists()) return

        isProcessing = true
        viewModelScope.launch {
            try {
                val bytes = file.readBytes()
                val response = model.generateContent(
                    content {
                        inlineData(bytes, "audio/wav")
                        text("Transcribe this audio. Return only the transcription.")
                    }
                )
                _transcript.value = response.text ?: "No transcription received"
            } catch (e: Exception) {
                _error.value = "Gemini Error: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }
}
