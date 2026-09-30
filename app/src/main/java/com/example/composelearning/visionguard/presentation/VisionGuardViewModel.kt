package com.example.composelearning.visionguard.presentation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.visionguard.domain.SpatialSonarEngine
import com.example.composelearning.visionguard.presentation.VisionGuardContract.HazardLevel
import com.example.composelearning.visionguard.presentation.VisionGuardContract.Intent
import com.example.composelearning.visionguard.presentation.VisionGuardContract.SpatialTarget
import com.example.composelearning.visionguard.presentation.VisionGuardContract.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class VisionGuardViewModel : ViewModel(), TextToSpeech.OnInitListener {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    private var sweepAngle = 0f
    val currentSweepAngle: Float get() = sweepAngle

    private var radarJob: Job? = null
    private var lastSpokenTimeMs = 0L

    init {
        startRadarSimulation()
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
            is Intent.ToggleSimulationMode -> {
                val newSim = !_state.value.isSimulationMode
                _state.update { it.copy(isSimulationMode = newSim) }
            }
            is Intent.ToggleTts -> {
                _state.update { it.copy(isTtsEnabled = !it.isTtsEnabled) }
            }
            is Intent.SelectTarget -> {
                _state.update { it.copy(selectedTargetId = intent.targetId) }
            }
            is Intent.ClearHazards -> {
                _state.update { it.copy(targets = emptyList(), primaryHazard = null) }
            }
        }
    }

    private fun startRadarSimulation() {
        if (radarJob?.isActive == true) return
        radarJob = viewModelScope.launch {
            var elapsed = 0L
            while (isActive) {
                delay(40L) // 25 FPS
                elapsed += 40L
                sweepAngle = (sweepAngle + 4f) % 180f

                val targets = SpatialSonarEngine.generateSimulatedTargets(elapsed)
                val highestHazard = targets.filter { it.hazardLevel != HazardLevel.SAFE }
                    .minByOrNull { it.distanceMeters }

                _state.update {
                    it.copy(
                        targets = targets,
                        primaryHazard = highestHazard
                    )
                }

                checkAndSpeakAlert(highestHazard, elapsed)
            }
        }
    }

    private fun checkAndSpeakAlert(hazard: SpatialTarget?, nowMs: Long) {
        if (hazard == null || !_state.value.isTtsEnabled || !isTtsInitialized) return
        // Rate limit TTS to once every 4 seconds
        if (nowMs - lastSpokenTimeMs < 4000L) return

        lastSpokenTimeMs = nowMs
        val dirText = if (hazard.lateralOffsetMeters < -0.2f) "slight left" else if (hazard.lateralOffsetMeters > 0.2f) "slight right" else "directly ahead"
        val alertMessage = "Warning: ${hazard.label} ${String.format(Locale.US, "%.1f", hazard.distanceMeters)} meters $dirText."

        _state.update { it.copy(latestVoiceAlert = alertMessage) }
        tts?.speak(alertMessage, TextToSpeech.QUEUE_FLUSH, null, "HAZARD_ALERT")
    }

    override fun onCleared() {
        radarJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}
