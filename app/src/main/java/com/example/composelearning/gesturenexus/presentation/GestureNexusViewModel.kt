package com.example.composelearning.gesturenexus.presentation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.gesturenexus.data.GestureNexusAnalyzer
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.DetectedGesture
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.HandLandmarkPoint
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.HandMode
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.Intent
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.State
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.StrokePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class GestureNexusViewModel : ViewModel(), TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var simulationJob: Job? = null

    private val currentStrokePoints = mutableListOf<Offset>()

    init {
        startGestureSimulation()
    }

    fun initTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsInitialized = true
        }
    }

    fun processIntent(intent: Intent) {
        when (intent) {
            is Intent.PermissionResult -> {
                _state.update { it.copy(isCameraPermissionGranted = intent.isGranted) }
            }
            is Intent.SelectMode -> {
                _state.update { it.copy(handMode = intent.mode) }
            }
            is Intent.SelectColor -> {
                _state.update { it.copy(activeColor = intent.color) }
            }
            is Intent.ClearCanvas -> {
                currentStrokePoints.clear()
                _state.update { it.copy(drawnStrokes = emptyList(), transcribedText = "") }
            }
            is Intent.SpeakTranscribedText -> {
                speakCurrentText()
            }
            is Intent.ToggleSimulationMode -> {
                _state.update { it.copy(isSimulationMode = !it.isSimulationMode) }
            }
        }
    }

    private fun startGestureSimulation() {
        if (simulationJob?.isActive == true) return
        simulationJob = viewModelScope.launch {
            var elapsed = 0L
            var simulatedCanvasWidth = 1080f
            var simulatedCanvasHeight = 1920f

            while (isActive) {
                delay(33L) // ~30 FPS
                elapsed += 33L

                val landmarks = GestureNexusAnalyzer.generateSimulatedHandLandmarks(elapsed)
                val indexTip = landmarks[GestureNexusAnalyzer.INDEX_FINGER_TIP]

                if (indexTip != null && _state.value.handMode == HandMode.AIR_CANVAS_DRAWING) {
                    val pt = Offset(indexTip.x * simulatedCanvasWidth, indexTip.y * simulatedCanvasHeight)
                    currentStrokePoints.add(pt)

                    if (currentStrokePoints.size > 180) {
                        currentStrokePoints.removeAt(0)
                    }

                    val updatedStroke = StrokePath(
                        points = currentStrokePoints.toList(),
                        color = _state.value.activeColor
                    )
                    _state.update {
                        it.copy(
                            handLandmarks = landmarks,
                            currentGesture = DetectedGesture.PINCH_DRAW,
                            drawnStrokes = listOf(updatedStroke)
                        )
                    }
                } else {
                    // ASL Mode simulation
                    val aslGesture = when ((elapsed / 2500L % 3).toInt()) {
                        0 -> DetectedGesture.ASL_LETTER_A
                        1 -> DetectedGesture.ASL_LETTER_B
                        else -> DetectedGesture.ASL_LETTER_C
                    }
                    val letter = aslGesture.label.takeLast(3).replace("'", "").trim()

                    _state.update {
                        it.copy(
                            handLandmarks = landmarks,
                            currentGesture = aslGesture,
                            transcribedText = if (!it.transcribedText.endsWith(letter)) it.transcribedText + letter else it.transcribedText
                        )
                    }
                }
            }
        }
    }

    private fun speakCurrentText() {
        val text = _state.value.transcribedText
        if (text.isNotBlank() && isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GESTURE_TRANSCRIPTION")
        }
    }

    override fun onCleared() {
        simulationJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
