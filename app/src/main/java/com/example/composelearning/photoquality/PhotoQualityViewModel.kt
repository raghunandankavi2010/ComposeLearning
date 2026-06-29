package com.example.composelearning.photoquality

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One bundled sample image plus its (lazily computed) classification result. */
data class SampleImage(
    @param:DrawableRes val resId: Int,
    val caption: String,
    val result: QualityResult? = null,
    val isAnalyzing: Boolean = false
)

/** A user-supplied image (gallery pick or camera capture) and its result. */
data class UserCapture(
    val uri: Uri,
    val result: QualityResult? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null
)

data class PhotoQualityUiState(
    val samples: List<SampleImage>,
    val userCapture: UserCapture? = null,
    val modelReady: Boolean = false,
    val error: String? = null
)

/**
 * Loads [PhotoQualityClassifier] off the main thread and scores both the bundled samples and any
 * user-supplied image (gallery / camera) through it. The classifier is closed in [onCleared].
 */
class PhotoQualityViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(
        PhotoQualityUiState(
            samples = listOf(
                SampleImage(R.drawable.sample_clear, "Dog"),
                SampleImage(R.drawable.sample_flower, "Flower"),
                SampleImage(R.drawable.sample_beach, "Beach"),
                SampleImage(R.drawable.sample_unclear, "Blurred dog"),
                SampleImage(R.drawable.sample_pixelated, "Pixelated"),
                SampleImage(R.drawable.sample_blur_scene, "Blurred scene"),
                SampleImage(R.drawable.sample_photo, "Camera moved")
            )
        )
    )
    val uiState: StateFlow<PhotoQualityUiState> = _uiState.asStateFlow()

    private var classifier: PhotoQualityClassifier? = null

    init {
        viewModelScope.launch {
            try {
                classifier = withContext(Dispatchers.Default) {
                    PhotoQualityClassifier(getApplication())
                }
                _uiState.value = _uiState.value.copy(modelReady = true)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(error = t.message ?: "Failed to load model")
            }
        }
    }

    /** Classifies the bundled sample at [index]; safe to call repeatedly. */
    fun analyze(index: Int) {
        val model = classifier ?: return
        val current = _uiState.value
        if (index !in current.samples.indices || current.samples[index].isAnalyzing) return

        updateSample(index) { it.copy(isAnalyzing = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bitmap = BitmapFactory.decodeResource(
                    getApplication<Application>().resources,
                    current.samples[index].resId,
                    opts
                )
                model.classify(bitmap)
            }
            updateSample(index) { it.copy(result = result, isAnalyzing = false) }
        }
    }

    /** Runs every bundled sample through the model in one go. */
    fun analyzeAll() {
        _uiState.value.samples.indices.forEach { analyze(it) }
    }

    /** Scores a user-supplied image (gallery pick or camera capture). */
    fun analyzeUri(uri: Uri) {
        val model = classifier ?: return
        _uiState.value = _uiState.value.copy(
            userCapture = UserCapture(uri = uri, isAnalyzing = true)
        )
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    model.classify(decodeBitmap(uri))
                }
                _uiState.value = _uiState.value.copy(
                    userCapture = UserCapture(uri = uri, result = result)
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    userCapture = UserCapture(uri = uri, error = t.message ?: "Could not read image")
                )
            }
        }
    }

    /**
     * Decodes a content/file Uri to a software ARGB_8888 bitmap (EXIF orientation applied),
     * downsampled at decode time so a multi-megapixel capture never fully inflates into memory —
     * the classifier only needs [DECODE_TARGET_PX], well above its 224px input.
     */
    private fun decodeBitmap(uri: Uri): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                // Software allocator so the classifier can read pixels via getPixels().
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
                val sample = calculateInSampleSize(info.size.width, info.size.height, DECODE_TARGET_PX)
                if (sample > 1) decoder.setTargetSampleSize(sample)
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, DECODE_TARGET_PX)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opts) }
                ?: error("Could not decode image")
        }
    }

    /**
     * Largest power-of-two sample size that keeps *both* decoded dimensions at or above [target],
     * so the result stays above the classifier's 224px input without upscaling.
     */
    private fun calculateInSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= target && height / (sample * 2) >= target) {
            sample *= 2
        }
        return sample
    }

    private inline fun updateSample(index: Int, transform: (SampleImage) -> SampleImage) {
        _uiState.value = _uiState.value.copy(
            samples = _uiState.value.samples.mapIndexed { i, s -> if (i == index) transform(s) else s }
        )
    }

    override fun onCleared() {
        classifier?.close()
        classifier = null
    }

    private companion object {
        /** Min decoded dimension for user images; comfortably above the model's 224px input. */
        const val DECODE_TARGET_PX = 256
    }
}
