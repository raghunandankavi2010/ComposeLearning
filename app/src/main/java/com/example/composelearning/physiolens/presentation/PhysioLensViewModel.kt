package com.example.composelearning.physiolens.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.physiolens.domain.BiomechanicalEngine
import com.example.composelearning.physiolens.presentation.PhysioLensContract.Intent
import com.example.composelearning.physiolens.presentation.PhysioLensContract.PosePoint
import com.example.composelearning.physiolens.presentation.PhysioLensContract.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PhysioLensViewModel : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Start simulation mode by default for smooth demo experience
        startSimulationMode()
    }

    fun processIntent(intent: Intent) {
        when (intent) {
            is Intent.PermissionResult -> {
                _state.update { it.copy(isCameraPermissionGranted = intent.isGranted) }
                if (!intent.isGranted) {
                    startSimulationMode()
                } else {
                    stopSimulationMode()
                }
            }
            is Intent.ToggleSimulationMode -> {
                val newSimMode = !_state.value.isSimulationMode
                _state.update { it.copy(isSimulationMode = newSimMode) }
                if (newSimMode) {
                    startSimulationMode()
                } else {
                    stopSimulationMode()
                }
            }
            is Intent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.index) }
            }
            is Intent.ResetAnalysis -> {
                _state.update { it.copy(analysis = null, posePoints = emptyMap()) }
            }
        }
    }

    fun onFrameAnalyzed(poseMap: Map<Int, PosePoint>?, width: Int, height: Int) {
        if (_state.value.isSimulationMode) return // Ignore camera when simulating

        if (poseMap == null) {
            _state.update { it.copy(posePoints = emptyMap()) }
            return
        }

        val analysis = BiomechanicalEngine.calculateBiomechanics(poseMap)
        _state.update {
            it.copy(
                posePoints = poseMap,
                imageWidth = width,
                imageHeight = height,
                analysis = analysis
            )
        }
    }

    private fun startSimulationMode() {
        if (simulationJob?.isActive == true) return
        _state.update { it.copy(isSimulationMode = true) }
        simulationJob = viewModelScope.launch {
            var elapsed = 0L
            while (isActive) {
                delay(33L) // ~30 FPS
                elapsed += 33L
                val simulatedPoints = BiomechanicalEngine.generateSimulatedLandmarks(elapsed)
                val analysis = BiomechanicalEngine.calculateBiomechanics(simulatedPoints)
                _state.update {
                    it.copy(
                        posePoints = simulatedPoints,
                        analysis = analysis
                    )
                }
            }
        }
    }

    private fun stopSimulationMode() {
        simulationJob?.cancel()
        simulationJob = null
        _state.update { it.copy(isSimulationMode = false) }
    }

    override fun onCleared() {
        stopSimulationMode()
        super.onCleared()
    }
}
