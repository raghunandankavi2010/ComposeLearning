package com.example.composelearning.animcompose

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ImagesBitmapsScreen() {
    var selectedDemo by remember { mutableIntStateOf(0) }
    val demos = listOf<Pair<String, @Composable () -> Unit>>(
        "Simple Image" to @Composable { SimpleImageDemo() },
        "Transformed Image" to @Composable { TransformedImageDemo() },
        "Interactive Image" to @Composable { InteractiveImageDemo() },
        "Clipped Image" to @Composable { ClippedImageDemo() },
        "Photo Frame" to @Composable { PhotoFrameDemo() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(demos.size) { index ->
                FilterChip(
                    onClick = { selectedDemo = index },
                    label = { Text(demos[index].first) },
                    selected = selectedDemo == index
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            demos[selectedDemo].second()
        }
    }
}

@Composable
fun SimpleImageDemo() {
    // Create a simple colored bitmap as placeholder
    val imageBitmap: ImageBitmap = remember {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.BLUE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.YELLOW
        paint.textSize = 40f
        canvas.drawText("LOGO", 30f, 120f, paint)

        bitmap.asImageBitmap()
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        drawImage(
            image = imageBitmap,
            topLeft = Offset(100f, 100f)
        )
    }
}

@Composable
fun TransformedImageDemo() {
    val imageBitmap: ImageBitmap = remember {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 40f
        canvas.drawText("LION", 20f, 120f, paint)

        bitmap.asImageBitmap()
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        withTransform({
            scale(0.5f, 0.5f, pivot = center)
            rotate(45f, pivot = center)
        }) {
            drawImage(
                image = imageBitmap,
                topLeft = Offset(
                    center.x - imageBitmap.width.toFloat() / 2f,
                    center.y - imageBitmap.height.toFloat() / 2f
                )
            )
        }
    }
}

@Composable
fun InteractiveImageDemo() {
    val imageBitmap: ImageBitmap = remember {
        val width = 200
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.GREEN
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 40f
        canvas.drawText("PHOTO", 20f, 120f, paint)

        bitmap.asImageBitmap()
    }

    var offset by remember { mutableStateOf(Offset(100f, 100f)) }
    var scale by remember { mutableFloatStateOf(1f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offset += pan
                    scale = (scale * zoom).coerceIn(0.5f, 2f)
                }
            }
    ) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {
            drawImage(
                image = imageBitmap,
                topLeft = Offset.Zero
            )
        }
    }
}

@Composable
fun ClippedImageDemo() {
    val imageBitmap: ImageBitmap = remember {
        val width = 300
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.CYAN
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        for (i in 0..10) {
            paint.color = if (i % 2 == 0) android.graphics.Color.MAGENTA else android.graphics.Color.YELLOW
            canvas.drawRect(i * 30f, 0f, (i + 1) * 30f, height.toFloat(), paint)
        }

        bitmap.asImageBitmap()
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.Black)
    ) {
        clipPath(
            path = Path().apply {
                addOval(Rect(center.x - 150f, center.y - 150f, center.x + 150f, center.y + 150f))
            }
        ) {
            drawImage(
                image = imageBitmap,
                topLeft = Offset(
                    center.x - imageBitmap.width.toFloat() / 2f,
                    center.y - imageBitmap.height.toFloat() / 2f
                )
            )
        }
    }
}

@Composable
fun PhotoFrameDemo() {
    val imageBitmap: ImageBitmap = remember {
        val width = 300
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.rgb(147, 112, 219)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        paint.color = android.graphics.Color.WHITE
        paint.textSize = 50f
        canvas.drawText("PHOTO", 50f, 150f, paint)

        bitmap.asImageBitmap()
    }

    var offset by remember { mutableStateOf(Offset(100f, 100f)) }
    var scale by remember { mutableFloatStateOf(1f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offset += pan
                    scale = (scale * zoom).coerceIn(0.5f, 2f)
                }
            }
    ) {
        clipPath(
            path = Path().apply {
                addOval(Rect(center.x - 150f, center.y - 150f, center.x + 150f, center.y + 150f))
            }
        ) {
            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale, pivot = center)
            }) {
                drawImage(
                    image = imageBitmap,
                    topLeft = Offset.Zero
                )
            }
        }

        // Draw decorative frame
        drawCircle(
            color = Color.Gray,
            radius = 155f,
            center = center,
            style = Stroke(width = 10.dp.toPx())
        )
    }
}
