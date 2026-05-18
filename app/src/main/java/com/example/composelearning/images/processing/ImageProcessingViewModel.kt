package com.example.composelearning.images.processing

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =================================================================================================
// ViewModel owns: the decoded source bitmap, the currently selected filter, and all live sliders.
// The preview is computed inside the composable on every frame from the AGSL shader uniforms — the
// VM never touches the GPU path.
//
// State separation matters here: the bitmap is heavy (multi-MB) so it lives behind its own
// StateFlow; the params are lightweight floats that change ~60Hz under finger drag and live in one
// combined `Params` flow. Recomposition scopes can subscribe to one without dragging the other.
// =================================================================================================

data class FilterParams(
    val intensity: Float = 1f,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val vignette: Float = 0f,
)

data class ImageProcessingUiState(
    val sourceUri: Uri? = null,
    val sourceBitmap: Bitmap? = null,
    val isLoadingSource: Boolean = false,
)

class ImageProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val _ui = MutableStateFlow(ImageProcessingUiState())
    val ui: StateFlow<ImageProcessingUiState> = _ui.asStateFlow()

    private val _filter = MutableStateFlow(ImageFilter.Original)
    val filter: StateFlow<ImageFilter> = _filter.asStateFlow()

    private val _params = MutableStateFlow(FilterParams())
    val params: StateFlow<FilterParams> = _params.asStateFlow()

    init {
        // Seed with the bundled sample bitmap so the screen has something to filter the moment
        // the user opens it. Decode off the main thread.
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = runCatching { decodeResourceScaled(R.drawable.tomato, longEdgeTarget = 1200) }
                .getOrNull() ?: return@launch
            // If the user already picked something in the meantime, drop the sample.
            _ui.update { current -> if (current.sourceBitmap == null) current.copy(sourceBitmap = bmp) else current }
        }
    }

    fun onImageSelected(uri: Uri) {
        _ui.update { it.copy(sourceUri = uri, isLoadingSource = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = runCatching { decodeBitmap(uri) }.getOrNull()
            _ui.update { it.copy(sourceBitmap = bmp, isLoadingSource = false) }
        }
    }

    fun onFilterChanged(filter: ImageFilter) {
        if (_filter.value == filter) return
        _filter.value = filter
        // Each preset ships a "look" — saturation pop, vignette, contrast — so the strip feels like
        // a real Instagram-style filter row instead of a math menu. Users can then tweak the
        // sliders on top of the preset.
        _params.value = defaultParamsFor(filter)
    }

    fun onParamsChanged(transform: (FilterParams) -> FilterParams) {
        _params.update(transform)
    }

    // Downsample on decode so we don't burn 30MB of RAM on a 12MP phone photo. Long-edge cap of
    // 1600px is enough for a full-bleed phone preview while keeping per-pixel work cheap on CPU.
    private fun decodeBitmap(uri: Uri): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        val target = 1600
        val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.isMutableRequired = false
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val w = info.size.width
                val h = info.size.height
                val longEdge = maxOf(w, h)
                if (longEdge > target) {
                    val scale = target.toFloat() / longEdge
                    decoder.setTargetSize((w * scale).toInt(), (h * scale).toInt())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(resolver, uri)
        }
        return if (raw.config == Bitmap.Config.ARGB_8888) raw
        else raw.copy(Bitmap.Config.ARGB_8888, false).also { raw.recycle() }
    }

    private fun decodeResourceScaled(@DrawableRes resId: Int, longEdgeTarget: Int): Bitmap {
        val res = getApplication<Application>().resources
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, resId, bounds)
        var sample = 1
        var longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > longEdgeTarget) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeResource(res, resId, opts)
    }
}

fun defaultParamsFor(filter: ImageFilter): FilterParams = when (filter) {
    ImageFilter.Original -> FilterParams()
    ImageFilter.Greyscale -> FilterParams(saturation = 0f)
    ImageFilter.Sepia -> FilterParams(saturation = 0.9f, contrast = 1.05f)
    ImageFilter.Invert -> FilterParams()
    ImageFilter.Vivid -> FilterParams(saturation = 1.45f, contrast = 1.15f, brightness = 0.02f)
    ImageFilter.Cool -> FilterParams(saturation = 1.05f)
    ImageFilter.Warm -> FilterParams(saturation = 1.05f, brightness = 0.02f)
    ImageFilter.Vintage -> FilterParams(saturation = 0.85f, contrast = 1.08f, vignette = 0.4f)
    ImageFilter.Cinematic -> FilterParams(saturation = 0.9f, contrast = 1.12f, vignette = 0.5f)
    ImageFilter.Polaroid -> FilterParams(saturation = 0.85f, contrast = 0.95f, vignette = 0.6f)
    ImageFilter.Noir -> FilterParams(saturation = 0f, contrast = 1.3f, vignette = 0.7f)
    ImageFilter.Fade -> FilterParams(saturation = 0.9f, contrast = 0.85f)
    ImageFilter.Cyberpunk -> FilterParams(saturation = 1.3f, contrast = 1.2f, vignette = 0.4f)
    ImageFilter.Posterize -> FilterParams(saturation = 1.1f, contrast = 1.1f)
    ImageFilter.Sharpen -> FilterParams(intensity = 0.6f)
    ImageFilter.Emboss -> FilterParams(saturation = 0f, contrast = 1.1f)
    ImageFilter.Edge -> FilterParams(saturation = 0.4f, contrast = 1.2f)
    ImageFilter.Pixelate -> FilterParams()
}