package com.example.composelearning.images.processing

import android.graphics.Bitmap
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// =================================================================================================
// Image-processing screen. One photo, a strip of TikTok / Instagram-style filter presets, and live
// sliders for intensity / brightness / contrast / saturation / vignette. The pipeline is AGSL on
// the GPU as a RenderEffect — runs at display refresh rate with zero per-frame allocation.
// =================================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageProcessingScreen(
    onBack: () -> Unit = {},
    viewModel: ImageProcessingViewModel = viewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    // Held as State, NOT destructured. The GPU preview reads it inside the graphicsLayer block so
    // slider drags invalidate the layer only — the screen composable itself does not recompose
    // at 60Hz.
    val paramsState = viewModel.params.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onImageSelected(uri) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Image Processing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledIconButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick image")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            PreviewArea(
                bitmap = ui.sourceBitmap,
                isLoadingSource = ui.isLoadingSource,
                filter = filter,
                paramsState = paramsState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            FilterStrip(
                current = filter,
                onSelect = viewModel::onFilterChanged,
            )

            ParamSliders(
                paramsState = paramsState,
                onChange = viewModel::onParamsChanged,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun PreviewArea(
    bitmap: Bitmap?,
    isLoadingSource: Boolean,
    filter: ImageFilter,
    paramsState: State<FilterParams>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoadingSource -> CircularProgressIndicator()
            bitmap == null -> Text("Pick an image to filter")
            else -> GpuPreview(
                bitmap = bitmap,
                filter = filter,
                paramsState = paramsState,
            )
        }
    }
}

@Composable
private fun GpuPreview(
    bitmap: Bitmap,
    filter: ImageFilter,
    paramsState: State<FilterParams>,
) {
    val shader = remember { RuntimeShader(FILTER_SHADER) }
    val spec = remember(filter) { filterSpec(filter) }
    // Rebuild the LUT bitmap only when the filter changes. The shader needs a sampler bound every
    // frame even when the filter has no LUT — we bind an identity ramp in that case and gate the
    // lookup with `useLut`.
    val lutBitmap = remember(filter) {
        if (spec.hasLut) buildLutBitmap(spec.lutR!!, spec.lutG!!, spec.lutB!!)
        else identityLutBitmap()
    }
    val lutShader = remember(lutBitmap) { lutBitmap.toClampShader() }
    val image = remember(bitmap) { bitmap.asImageBitmap() }

    Image(
        bitmap = image,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                clip = true
                // Reading paramsState.value here scopes invalidation to the layer (placement),
                // not composition — slider drags do not retrigger the calling composable.
                val p = paramsState.value
                shader.setInputShader("lut", lutShader)
                shader.setFloatUniform(
                    "matRow0", spec.mat[0], spec.mat[1], spec.mat[2], spec.mat[3],
                )
                shader.setFloatUniform(
                    "matRow1", spec.mat[4], spec.mat[5], spec.mat[6], spec.mat[7],
                )
                shader.setFloatUniform(
                    "matRow2", spec.mat[8], spec.mat[9], spec.mat[10], spec.mat[11],
                )
                shader.setFloatUniform(
                    "matOffset", spec.offset[0], spec.offset[1], spec.offset[2],
                )
                shader.setFloatUniform("useLut", if (spec.hasLut) 1f else 0f)
                shader.setFloatUniform("intensity", p.intensity)
                shader.setFloatUniform("brightness", p.brightness)
                shader.setFloatUniform("contrast", p.contrast)
                shader.setFloatUniform("saturation", p.saturation)
                shader.setFloatUniform("vignette", p.vignette)
                shader.setFloatUniform("resolution", size.width, size.height)
                // Convolution stage uniforms. The kernel rows default to (0,0,0) when unused —
                // the shader gates them with `useKernel` so the eight neighbour samples are
                // skipped entirely on the no-kernel fast path.
                val k = spec.kernel
                shader.setFloatUniform("useKernel", if (spec.hasKernel) 1f else 0f)
                shader.setFloatUniform("kRow0",
                    k?.get(0) ?: 0f, k?.get(1) ?: 0f, k?.get(2) ?: 0f)
                shader.setFloatUniform("kRow1",
                    k?.get(3) ?: 0f, k?.get(4) ?: 0f, k?.get(5) ?: 0f)
                shader.setFloatUniform("kRow2",
                    k?.get(6) ?: 0f, k?.get(7) ?: 0f, k?.get(8) ?: 0f)
                shader.setFloatUniform("kernelOffset", spec.kernelOffset)
                shader.setFloatUniform("pixelateBlock", spec.pixelateBlock)
                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, "content")
                    .asComposeRenderEffect()
            },
    )
}

@Composable
private fun FilterStrip(
    current: ImageFilter,
    onSelect: (ImageFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(ImageFilter.entries) { f ->
            FilterThumb(
                filter = f,
                selected = f == current,
                onClick = { onSelect(f) },
            )
        }
    }
}

@Composable
private fun FilterThumb(
    filter: ImageFilter,
    selected: Boolean,
    onClick: () -> Unit,
) {
    // Swatch is generated once per filter for the lifetime of the composition — the bitmap is tiny
    // (64x64) so memory cost is negligible compared to retaining the same instance.
    val swatch = remember(filter) {
        val pixels = thumbnailGradient(filter)
        val bmp = Bitmap.createBitmap(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, THUMBNAIL_SIZE, 0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        bmp.asImageBitmap()
    }
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
                .clickable(onClick = onClick),
        ) {
            Image(
                bitmap = swatch,
                contentDescription = filter.label,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = filter.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ParamSliders(
    paramsState: State<FilterParams>,
    onChange: ((FilterParams) -> FilterParams) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = paramsState.value
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ParamRow(
            label = "Strength",
            value = p.intensity,
            range = 0f..1f,
            display = "%.2f".format(p.intensity),
        ) { v -> onChange { it.copy(intensity = v) } }
        ParamRow(
            label = "Brightness",
            value = p.brightness,
            range = -0.4f..0.4f,
            display = "%+.2f".format(p.brightness),
        ) { v -> onChange { it.copy(brightness = v) } }
        ParamRow(
            label = "Contrast",
            value = p.contrast,
            range = 0.5f..1.8f,
            display = "%.2f".format(p.contrast),
        ) { v -> onChange { it.copy(contrast = v) } }
        ParamRow(
            label = "Saturation",
            value = p.saturation,
            range = 0f..2f,
            display = "%.2f".format(p.saturation),
        ) { v -> onChange { it.copy(saturation = v) } }
        ParamRow(
            label = "Vignette",
            value = p.vignette,
            range = 0f..1f,
            display = "%.2f".format(p.vignette),
        ) { v -> onChange { it.copy(vignette = v) } }
    }
}

@Composable
private fun ParamRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    display: String,
    onChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            modifier = Modifier.width(86.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            display,
            modifier = Modifier.width(54.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}