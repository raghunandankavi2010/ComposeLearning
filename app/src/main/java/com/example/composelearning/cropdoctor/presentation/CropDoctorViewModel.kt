package com.example.composelearning.cropdoctor.presentation

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.cropdoctor.data.PlantDiseaseClassifier
import com.example.composelearning.cropdoctor.domain.DiseaseKnowledge
import com.example.composelearning.cropdoctor.domain.model.Diagnosis
import com.example.composelearning.cropdoctor.domain.model.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CropDoctorUiState(
    /** The image currently selected (gallery or camera), shown as a preview. */
    val imageUri: Uri? = null,
    val isModelReady: Boolean = false,
    val isAnalyzing: Boolean = false,
    val diagnosis: Diagnosis? = null,
    /** Fatal model-load error (model missing from assets, etc.). */
    val modelError: String? = null,
    /** Per-image error (couldn't read the picked image). */
    val imageError: String? = null
)

/**
 * Drives the Crop Doctor screen: loads the on-device [PlantDiseaseClassifier] off the main thread,
 * decodes the picked/captured image, runs inference, and maps the result through
 * [DiseaseKnowledge] into farmer-facing advice. Everything is local — no network.
 */
class CropDoctorViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(CropDoctorUiState())
    val uiState: StateFlow<CropDoctorUiState> = _uiState.asStateFlow()

    private var classifier: PlantDiseaseClassifier? = null

    init {
        viewModelScope.launch {
            try {
                classifier = withContext(Dispatchers.Default) {
                    PlantDiseaseClassifier(getApplication())
                }
                _uiState.value = _uiState.value.copy(isModelReady = true)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    modelError = t.message ?: "Could not load the detection model."
                )
            }
        }
    }

    /** Diagnoses a user-supplied image (gallery pick or camera capture). */
    fun analyze(uri: Uri) {
        val model = classifier ?: return
        _uiState.value = _uiState.value.copy(
            imageUri = uri,
            isAnalyzing = true,
            diagnosis = null,
            imageError = null
        )
        viewModelScope.launch {
            try {
                val diagnosis = withContext(Dispatchers.Default) {
                    val bitmap = decodeBitmap(uri)
                    val raw = model.classify(bitmap, topK = 3)
                    val predictions = raw.map {
                        Prediction(
                            info = DiseaseKnowledge.lookup(it.label),
                            confidence = it.confidence,
                            rawLabel = it.label
                        )
                    }
                    val best = predictions.first()
                    Diagnosis(
                        best = best,
                        alternates = predictions.drop(1).filter { it.confidence >= ALT_MIN_CONFIDENCE },
                        isConfident = best.confidence >= CONFIDENCE_THRESHOLD
                    )
                }
                _uiState.value = _uiState.value.copy(isAnalyzing = false, diagnosis = diagnosis)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    imageError = t.message ?: "Could not read this image. Please try another photo."
                )
            }
        }
    }

    /** Clears the current photo/result so the farmer can scan another leaf. */
    fun reset() {
        _uiState.value = _uiState.value.copy(
            imageUri = null,
            isAnalyzing = false,
            diagnosis = null,
            imageError = null
        )
    }

    /**
     * Decodes a content/file Uri to a software ARGB_8888 bitmap (EXIF orientation applied),
     * downsampled at decode time so a multi-megapixel capture never fully inflates into memory.
     */
    private fun decodeBitmap(uri: Uri): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
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

    private fun calculateInSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= target && height / (sample * 2) >= target) {
            sample *= 2
        }
        return sample
    }

    override fun onCleared() {
        classifier?.close()
        classifier = null
    }

    private companion object {
        /** Min decoded dimension; comfortably above the model's 224px input. */
        const val DECODE_TARGET_PX = 256

        /**
         * Below this top-1 probability we don't trust the result and ask for a clearer photo,
         * rather than risk showing a farmer a wrong diagnosis.
         */
        const val CONFIDENCE_THRESHOLD = 0.55f

        /** Only show an alternate guess if it has at least this much probability. */
        const val ALT_MIN_CONFIDENCE = 0.08f
    }
}
