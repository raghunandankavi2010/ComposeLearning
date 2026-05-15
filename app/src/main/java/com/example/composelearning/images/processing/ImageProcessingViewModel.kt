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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.sqrt

// =================================================================================================
// ViewModel owns: the decoded source bitmap, the currently selected filter, all live sliders, and
// the optional CPU-processed output. The GPU preview is computed inside the composable on every
// frame from the shader uniforms — the VM never touches the GPU path.
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

enum class ProcessingMode { Gpu, Cpu }

data class ImageProcessingUiState(
    val sourceUri: Uri? = null,
    val sourceBitmap: Bitmap? = null,
    val cpuOutput: Bitmap? = null,
    val isLoadingSource: Boolean = false,
    val isProcessingCpu: Boolean = false,
    val lastCpuDurationMs: Long? = null,
)

class ImageProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val _ui = MutableStateFlow(ImageProcessingUiState())
    val ui: StateFlow<ImageProcessingUiState> = _ui.asStateFlow()

    private val _filter = MutableStateFlow(ImageFilter.Original)
    val filter: StateFlow<ImageFilter> = _filter.asStateFlow()

    private val _params = MutableStateFlow(FilterParams())
    val params: StateFlow<FilterParams> = _params.asStateFlow()

    private val _mode = MutableStateFlow(ProcessingMode.Gpu)
    val mode: StateFlow<ProcessingMode> = _mode.asStateFlow()

    // The CPU pipeline rebuilds the whole output bitmap; cancel any in-flight job whenever inputs
    // change so the user always sees the result of their LATEST gesture, not a stale one.
    private var cpuJob: Job? = null

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
        _ui.update { it.copy(sourceUri = uri, isLoadingSource = true, cpuOutput = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = runCatching { decodeBitmap(uri) }.getOrNull()
            _ui.update { it.copy(sourceBitmap = bmp, isLoadingSource = false) }
            if (bmp != null && _mode.value == ProcessingMode.Cpu) processCpu()
        }
    }

    fun onFilterChanged(filter: ImageFilter) {
        if (_filter.value == filter) return
        _filter.value = filter
        // Each preset ships a "look" — saturation pop, vignette, contrast — so the strip feels like
        // a real Instagram-style filter row instead of a math menu. Users can then tweak the
        // sliders on top of the preset.
        _params.value = defaultParamsFor(filter)
        if (_mode.value == ProcessingMode.Cpu) processCpu()
    }

    fun onParamsChanged(transform: (FilterParams) -> FilterParams) {
        _params.update(transform)
        if (_mode.value == ProcessingMode.Cpu) processCpu()
    }

    fun onModeChanged(mode: ProcessingMode) {
        if (_mode.value == mode) return
        _mode.value = mode
        when (mode) {
            ProcessingMode.Gpu -> {
                cpuJob?.cancel()
                _ui.update { it.copy(cpuOutput = null, isProcessingCpu = false) }
            }
            ProcessingMode.Cpu -> processCpu()
        }
    }

    private fun processCpu() {
        val src = _ui.value.sourceBitmap ?: return
        cpuJob?.cancel()
        _ui.update { it.copy(isProcessingCpu = true) }
        cpuJob = viewModelScope.launch {
            val start = System.nanoTime()
            val out = withContext(Dispatchers.Default) {
                applyFilterCpu(src, _filter.value, _params.value)
            }
            val elapsed = (System.nanoTime() - start) / 1_000_000L
            _ui.update {
                it.copy(cpuOutput = out, isProcessingCpu = false, lastCpuDurationMs = elapsed)
            }
        }
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
        // Force ARGB_8888 for the pixel array path — HARDWARE bitmaps cannot be read with
        // getPixels(), and ImageDecoder may return HARDWARE on newer Android.
        return if (raw.config == Bitmap.Config.ARGB_8888) raw
        else raw.copy(Bitmap.Config.ARGB_8888, false).also { raw.recycle() }
    }

    // Decode a bundled drawable resource using a 2-pass BitmapFactory approach: pass 1 reads only
    // the header to size the source, pass 2 decodes with a power-of-two inSampleSize that brings
    // the long edge under `longEdgeTarget`. This is the canonical low-memory pattern for resources.
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

// =================================================================================================
// CPU pipeline. Runs exactly the same math as the AGSL shader, but in Kotlin, on Dispatchers.Default
// with the bitmap chunked across cores. We pull pixels into one IntArray (one contiguous copy off
// the heap) then split the row range across N coroutines — each one writes into its own slice of
// the same array so we never touch shared state during the hot loop.
// =================================================================================================

suspend fun applyFilterCpu(
    src: Bitmap,
    filter: ImageFilter,
    params: FilterParams,
): Bitmap = coroutineScope {
    val spec = filterSpec(filter)
    val width = src.width
    val height = src.height
    val pixels = IntArray(width * height)
    src.getPixels(pixels, 0, width, 0, 0, width, height)

    val centerX = width * 0.5f
    val centerY = height * 0.5f
    val halfDiag = hypot(centerX, centerY)

    val cores = Runtime.getRuntime().availableProcessors().coerceIn(2, 8)
    val rowsPerJob = (height + cores - 1) / cores

    processChunked(
        rowsPerJob = rowsPerJob,
        cores = cores,
        height = height,
        scope = this,
    ) { rowStart, rowEnd ->
        processStrip(
            pixels = pixels,
            rowStart = rowStart,
            rowEnd = rowEnd,
            width = width,
            spec = spec,
            params = params,
            centerX = centerX,
            centerY = centerY,
            halfDiag = halfDiag,
        )
    }

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    out.setPixels(pixels, 0, width, 0, 0, width, height)
    out
}

private suspend inline fun processChunked(
    rowsPerJob: Int,
    cores: Int,
    height: Int,
    scope: CoroutineScope,
    crossinline block: suspend (Int, Int) -> Unit,
) {
    val jobs = ArrayList<Job>(cores)
    var rowStart = 0
    while (rowStart < height) {
        val rowEnd = (rowStart + rowsPerJob).coerceAtMost(height)
        val s = rowStart
        val e = rowEnd
        jobs += scope.launch(Dispatchers.Default) { block(s, e) }
        rowStart = rowEnd
    }
    jobs.forEach { it.join() }
}

private fun processStrip(
    pixels: IntArray,
    rowStart: Int,
    rowEnd: Int,
    width: Int,
    spec: FilterSpec,
    params: FilterParams,
    centerX: Float,
    centerY: Float,
    halfDiag: Float,
) {
    // Hoist everything we read every pixel out of the inner loop. The JIT does some of this for
    // us, but pulling array elements into locals is the kind of thing that swings 2-3x on a hot
    // path like this. `local fields are friends.`
    val m0 = spec.mat[0]; val m1 = spec.mat[1]; val m2 = spec.mat[2]
    val m4 = spec.mat[4]; val m5 = spec.mat[5]; val m6 = spec.mat[6]
    val m8 = spec.mat[8]; val m9 = spec.mat[9]; val m10 = spec.mat[10]
    val o0 = spec.offset[0]; val o1 = spec.offset[1]; val o2 = spec.offset[2]
    val hasLut = spec.hasLut
    val lutR = spec.lutR; val lutG = spec.lutG; val lutB = spec.lutB
    val intensity = params.intensity
    val brightness = params.brightness
    val contrast = params.contrast
    val saturation = params.saturation
    val vignette = params.vignette
    val vignetteEnabled = vignette > 0.001f

    for (y in rowStart until rowEnd) {
        val rowOffset = y * width
        val dy = y - centerY
        for (x in 0 until width) {
            val i = rowOffset + x
            val orig = pixels[i]
            val a = orig.ushr(24) and 0xff
            val srcR = ((orig shr 16) and 0xff) * (1f / 255f)
            val srcG = ((orig shr 8) and 0xff) * (1f / 255f)
            val srcB = (orig and 0xff) * (1f / 255f)

            // 1) color matrix.
            var r = (m0 * srcR + m1 * srcG + m2 * srcB + o0).coerceIn(0f, 1f)
            var g = (m4 * srcR + m5 * srcG + m6 * srcB + o1).coerceIn(0f, 1f)
            var b = (m8 * srcR + m9 * srcG + m10 * srcB + o2).coerceIn(0f, 1f)

            // 2) LUT (per-channel tone curve).
            if (hasLut) {
                r = lutR!![(r * 255f).toInt()] * (1f / 255f)
                g = lutG!![(g * 255f).toInt()] * (1f / 255f)
                b = lutB!![(b * 255f).toInt()] * (1f / 255f)
            }

            // 3) brightness / contrast.
            r = (r - 0.5f) * contrast + 0.5f + brightness
            g = (g - 0.5f) * contrast + 0.5f + brightness
            b = (b - 0.5f) * contrast + 0.5f + brightness

            // 4) saturation around luma.
            val luma = 0.299f * r + 0.587f * g + 0.114f * b
            r = luma + (r - luma) * saturation
            g = luma + (g - luma) * saturation
            b = luma + (b - luma) * saturation

            r = r.coerceIn(0f, 1f); g = g.coerceIn(0f, 1f); b = b.coerceIn(0f, 1f)

            // 5) vignette.
            if (vignetteEnabled) {
                val dx = x - centerX
                val dist = sqrt(dx * dx + dy * dy) / halfDiag
                val v = 1f - smoothstep(0.35f, 0.85f, dist) * vignette
                r *= v; g *= v; b *= v
            }

            // 6) intensity blend.
            r = srcR + (r - srcR) * intensity
            g = srcG + (g - srcG) * intensity
            b = srcB + (b - srcB) * intensity

            val rr = (r.coerceIn(0f, 1f) * 255f).toInt()
            val gg = (g.coerceIn(0f, 1f) * 255f).toInt()
            val bb = (b.coerceIn(0f, 1f) * 255f).toInt()
            pixels[i] = (a shl 24) or (rr shl 16) or (gg shl 8) or bb
        }
    }
}

private inline fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
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
}