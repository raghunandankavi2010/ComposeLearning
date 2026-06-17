package com.example.composelearning.sarvamlid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable UI state. The screen renders purely from this. */
data class LanguageDetectionUiState(
    val input: String = "",
    val isDetecting: Boolean = false,
    val result: DetectionResult? = null,
)

class LanguageDetectionViewModel(
    private val repository: SarvamLidRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageDetectionUiState())
    val uiState: StateFlow<LanguageDetectionUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        // Clear a stale result as soon as the user edits, so the badge never lies.
        _uiState.update { it.copy(input = text, result = null) }
    }

    fun detect() {
        val text = _uiState.value.input
        if (_uiState.value.isDetecting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDetecting = true, result = null) }
            val result = repository.detect(text)
            _uiState.update { it.copy(isDetecting = false, result = result) }
        }
    }

    /** Manual factory — this project uses no Hilt. */
    class Factory(private val apiKey: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = apiKey.takeIf { it.isNotBlank() }?.let { SarvamLidService(it) }
            val repository = SarvamLidRepository(service)
            return LanguageDetectionViewModel(repository) as T
        }
    }
}
