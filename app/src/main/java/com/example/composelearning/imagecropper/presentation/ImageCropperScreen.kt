package com.example.composelearning.imagecropper.presentation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.composelearning.imagecropper.domain.CropShape
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Stateless cropper UI. Receives the [CropperUiState] and forwards [CropperIntent]s; all
 * live gesture geometry is held by a remembered [CropController] so dragging never lifts
 * state up into recomposition.
 */
@Composable
fun ImageCropperScreen(
    state: CropperUiState,
    onIntent: (CropperIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val controller = remember { CropController() }

    // Configure touch tolerances once from density (px-space values the controller needs).
    LaunchedEffect(density) {
        controller.minFramePx = with(density) { 72.dp.toPx() }
        controller.handleRadiusPx = with(density) { 24.dp.toPx() }
    }

    // Keep the controller's notion of the subject in sync with the loaded image.
    LaunchedEffect(state.imageSize) { controller.onImageChanged(state.imageSize) }

    // Surface transient messages (save success / failures) as a toast, then clear.
    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onIntent(CropperIntent.ConsumeMessage)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { onIntent(CropperIntent.LoadFromUri(it)) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        CropperTopBar(
            onPickImage = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            onReset = controller::reset,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .onSizeChanged(controller::onContainerResized)
                .pointerInput(controller) { cropGestures(controller) },
            contentAlignment = Alignment.Center,
        ) {
            state.image?.let { image ->
                Image(
                    bitmap = image,
                    contentDescription = "Image being cropped",
                    contentScale = ContentScale.Fit,
                    // Reads of zoom/offset happen in the layer (draw) phase → no recomposition.
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayerTransform(controller),
                )
                CropOverlay(controller = controller, shape = state.shape, modifier = Modifier.fillMaxSize())
            }

            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        CropperControls(
            selectedShape = state.shape,
            isProcessing = state.isProcessing,
            onSelectShape = { onIntent(CropperIntent.SelectShape(it)) },
            onCrop = { controller.sourceCropRect()?.let { onIntent(CropperIntent.Crop(it)) } },
        )
    }

    state.result?.let { result ->
        CropResultDialog(
            imageBitmap = remember(result) { result.asImageBitmap() },
            onSave = { onIntent(CropperIntent.SaveResult) },
            onDismiss = { onIntent(CropperIntent.DismissResult) },
        )
    }
}

/* ----------------------------------------------------------------------------------------
 * Image transform (deferred-read graphicsLayer)
 * -------------------------------------------------------------------------------------- */

private fun Modifier.graphicsLayerTransform(controller: CropController): Modifier =
    this.graphicsLayer {
        scaleX = controller.zoom
        scaleY = controller.zoom
        translationX = controller.offset.x
        translationY = controller.offset.y
    }

/* ----------------------------------------------------------------------------------------
 * Gestures — one arbitrating loop for pinch-zoom, frame move, and corner resize
 * -------------------------------------------------------------------------------------- */

private suspend fun PointerInputScope.cropGestures(controller: CropController) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // Mode is decided by where the *first* finger lands; multi-touch always zooms.
        val mode = controller.hitTest(down.position)

        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break

            if (pressed.size >= 2) {
                val zoomChange = event.calculateZoom()
                val pan = event.calculatePan()
                val centroid = event.calculateCentroid()
                if (zoomChange != 1f || pan != Offset.Zero) {
                    controller.transform(centroid, pan, zoomChange)
                }
                pressed.forEach { it.consume() }
            } else {
                val change = pressed.first()
                val drag = change.positionChange()
                if (drag != Offset.Zero) {
                    when (mode) {
                        is DragMode.Resize -> controller.resizeFrame(mode.corner, drag)
                        DragMode.MoveFrame -> controller.moveFrame(drag)
                        DragMode.TransformImage -> controller.panImage(drag)
                    }
                    change.consume()
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------------------------
 * Overlay — scrim with a shaped hole, rule-of-thirds grid, frame outline, corner handles
 * -------------------------------------------------------------------------------------- */

@Composable
private fun CropOverlay(
    controller: CropController,
    shape: CropShape,
    modifier: Modifier = Modifier,
) {
    val scrim = Color.Black.copy(alpha = 0.55f)
    val frameColor = Color.White
    val gridColor = Color.White.copy(alpha = 0.5f)

    Canvas(modifier = modifier) {
        // cropRect is read here, in the draw phase → invalidates draw only, not composition.
        val rect = controller.cropRect
        if (rect.width <= 0f || rect.height <= 0f) return@Canvas

        val path = shapePath(rect, shape)

        // Scrim everywhere except the crop shape (punched via a Clear-blended layer).
        val layerBounds = Rect(0f, 0f, size.width, size.height)
        drawIntoCanvas { canvas ->
            canvas.saveLayer(layerBounds, Paint())
            drawRect(color = scrim)
            drawPath(path = path, color = Color.Black, blendMode = BlendMode.Clear)
            canvas.restore()
        }

        // Rule-of-thirds grid, confined to the rectangular bounds of the frame.
        val thirdW = rect.width / 3f
        val thirdH = rect.height / 3f
        for (i in 1..2) {
            val x = rect.left + thirdW * i
            drawLine(gridColor, Offset(x, rect.top), Offset(x, rect.bottom), strokeWidth = 1f)
            val y = rect.top + thirdH * i
            drawLine(gridColor, Offset(rect.left, y), Offset(rect.right, y), strokeWidth = 1f)
        }

        // Frame outline in the selected shape.
        drawPath(path = path, color = frameColor, style = Stroke(width = 2.dp.toPx()))

        // Corner handles (always rectangular, since resizing acts on the bounding box).
        val handle = 18.dp.toPx()
        val thickness = 3.dp.toPx()
        drawCornerHandles(rect, frameColor, handle, thickness)
    }
}

private fun DrawScope.drawCornerHandles(rect: Rect, color: Color, length: Float, thickness: Float) {
    data class L(val from: Offset, val to: Offset)

    val segments = listOf(
        // top-left
        L(rect.topLeft, rect.topLeft + Offset(length, 0f)),
        L(rect.topLeft, rect.topLeft + Offset(0f, length)),
        // top-right
        L(rect.topRight, rect.topRight + Offset(-length, 0f)),
        L(rect.topRight, rect.topRight + Offset(0f, length)),
        // bottom-left
        L(rect.bottomLeft, rect.bottomLeft + Offset(length, 0f)),
        L(rect.bottomLeft, rect.bottomLeft + Offset(0f, -length)),
        // bottom-right
        L(rect.bottomRight, rect.bottomRight + Offset(-length, 0f)),
        L(rect.bottomRight, rect.bottomRight + Offset(0f, -length)),
    )
    segments.forEach { drawLine(color, it.from, it.to, strokeWidth = thickness, cap = StrokeCap.Round) }
}

/** Builds the on-screen outline path matching [shape] within [rect]. */
private fun shapePath(rect: Rect, shape: CropShape): Path = Path().apply {
    when (shape) {
        CropShape.Rectangle -> addRect(rect)
        CropShape.Circle -> addOval(
            Rect(
                center = rect.center,
                radius = min(rect.width, rect.height) / 2f,
            ),
        )
        CropShape.Star -> addStar(rect)
    }
}

private fun Path.addStar(rect: Rect, points: Int = 5, innerRatio: Float = 0.45f) {
    val cx = rect.center.x
    val cy = rect.center.y
    val outer = min(rect.width, rect.height) / 2f
    val inner = outer * innerRatio
    val step = Math.PI / points
    var angle = -Math.PI / 2
    moveTo(cx + (outer * cos(angle)).toFloat(), cy + (outer * sin(angle)).toFloat())
    for (i in 1 until points * 2) {
        angle += step
        val radius = if (i % 2 == 0) outer else inner
        lineTo(cx + (radius * cos(angle)).toFloat(), cy + (radius * sin(angle)).toFloat())
    }
    close()
}

/* ----------------------------------------------------------------------------------------
 * Chrome — top bar, controls, result dialog
 * -------------------------------------------------------------------------------------- */

@Composable
private fun CropperTopBar(onPickImage: () -> Unit, onReset: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Image Cropper",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onReset) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset transform")
        }
        IconButton(onClick = onPickImage) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick a photo")
        }
    }
}

@Composable
private fun CropperControls(
    selectedShape: CropShape,
    isProcessing: Boolean,
    onSelectShape: (CropShape) -> Unit,
    onCrop: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CropShape.entries.forEach { shape ->
                FilterChip(
                    selected = shape == selectedShape,
                    onClick = { onSelectShape(shape) },
                    label = { Text(shape.name) },
                )
            }
        }
        Button(
            onClick = onCrop,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Crop")
            }
        }
    }
}

@Composable
private fun CropResultDialog(
    imageBitmap: ImageBitmap,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Cropped result", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .checkerboard(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Cropped result preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Close") }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save") }
            }
        }
    }
}

/** Light/grey checkerboard so transparency in circle/star crops is visible. */
private fun Modifier.checkerboard(cell: Float = 16f): Modifier = this.then(
    Modifier.background(Color(0xFFEFEFEF)).drawWithCheckerboard(cell),
)

private fun Modifier.drawWithCheckerboard(cell: Float): Modifier =
    drawBehind {
        val cols = (size.width / cell).toInt() + 1
        val rows = (size.height / cell).toInt() + 1
        for (row in 0..rows) {
            for (col in 0..cols) {
                if ((row + col) % 2 == 0) {
                    drawRect(
                        color = Color(0xFFD8D8D8),
                        topLeft = Offset(col * cell, row * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
