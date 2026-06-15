package com.example.composelearning.imagecropper.presentation

import android.content.Context
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.composelearning.imagecropper.data.ImageRepositoryImpl
import com.example.composelearning.imagecropper.domain.CropImageUseCase
import com.example.composelearning.imagecropper.domain.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the image cropper. Reduces [CropperIntent]s into [CropperUiState].
 * Owns the heavy/durable concerns only — image decode, the bitmap crop, and persistence —
 * while transient gesture geometry stays in [CropController]. No Hilt (project convention);
 * see [Factory].
 */
class CropperViewModel(
    private val repository: ImageRepository,
    private val cropImage: CropImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropperUiState())
    val uiState: StateFlow<CropperUiState> = _uiState.asStateFlow()

    /**
     * Guards the one-shot initial load. The UI fires [CropperIntent.LoadDefault] from a
     * `LaunchedEffect` when the screen first appears; this flag makes that idempotent so a
     * configuration change (which re-runs the effect while the ViewModel survives) can't
     * reload the sample over a photo the user already picked.
     *
     * Deliberately *not* in an `init` block — constructing the ViewModel has no side
     * effects, so it can be unit-tested without a coroutine/dispatcher harness.
     */
    private var initialized = false

    fun onIntent(intent: CropperIntent) {
        when (intent) {
            CropperIntent.LoadDefault -> if (!initialized) {
                initialized = true
                loadImage { repository.loadDefault() }
            }

            is CropperIntent.LoadFromUri -> {
                initialized = true
                loadImage { repository.load(intent.uri) }
            }
            is CropperIntent.SelectShape -> _uiState.update { it.copy(shape = intent.shape) }
            is CropperIntent.Crop -> performCrop(intent)
            CropperIntent.SaveResult -> saveResult()
            CropperIntent.DismissResult -> _uiState.update { state ->
                state.result?.recycle()
                state.copy(result = null)
            }
            CropperIntent.ConsumeMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun loadImage(decode: suspend () -> android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { decode() }
                .onSuccess { bitmap ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            image = bitmap.asImageBitmap(),
                            imageSize = IntSize(bitmap.width, bitmap.height),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, message = "Couldn't load image") }
                }
        }
    }

    private fun performCrop(intent: CropperIntent.Crop) {
        val state = _uiState.value
        val source = state.image?.asAndroidBitmap() ?: return
        if (state.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            runCatching { cropImage(source, intent.region, state.shape) }
                .onSuccess { cropped ->
                    _uiState.update {
                        it.result?.recycle()
                        it.copy(isProcessing = false, result = cropped)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isProcessing = false, message = "Crop failed") }
                }
        }
    }

    private fun saveResult() {
        val bitmap = _uiState.value.result ?: return
        viewModelScope.launch {
            val uri = repository.save(bitmap, "crop_${System.currentTimeMillis()}")
            _uiState.update {
                it.copy(message = if (uri != null) "Saved to Photos" else "Couldn't save image")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.result?.recycle()
    }

    /** Manual factory wiring data → domain → presentation (no DI framework). */
    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = ImageRepositoryImpl(appContext)
            return CropperViewModel(repository, CropImageUseCase()) as T
        }
    }
}
