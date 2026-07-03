package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap

@Composable
fun DottedTextRoute(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        DottedText(
            text = "Compose",
            modifier = Modifier.fillMaxSize(),
            dotColor = Color.White
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
fun DottedText(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White,
    dotRadiusPx: Float = 1f,      // diameter 6px ...
    gridStep: Int = 2            // ... vs 5px spacing → dots overlap, no visible gaps
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val points = remember(canvasSize, text) {
        if (canvasSize == IntSize.Zero) emptyList()
        else sampleTextToDots(text, canvasSize.width, canvasSize.height, gridStep)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
    ) {
        if (points.isNotEmpty()) {
            drawPoints(
                points = points,
                pointMode = PointMode.Points,
                color = dotColor,
                strokeWidth = dotRadiusPx * 2,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun sampleTextToDots(
    text: String,
    width: Int,
    height: Int,
    gridStep: Int,
    jitter: Float = gridStep * 1.0f   // scatters points so the silhouette edge isn't a rigid staircase
): List<Offset> {
    if (width <= 0 || height <= 0) return emptyList()

    val bmp = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bmp)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = false                 // binary alpha: 0 or 255, no fuzzy edge pixels
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textSize = width / 6f
    }

    val bounds = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, bounds)
    if (bounds.width() > 0) paint.textSize *= (width * 0.8f) / bounds.width()
    paint.getTextBounds(text, 0, text.length, bounds)

    val baselineY = height / 2f - bounds.exactCenterY()
    canvas.drawText(text, width / 2f, baselineY, paint)

    val pixels = IntArray(width * height)
    bmp.getPixels(pixels, 0, width, 0, 0, width, height)
    bmp.recycle()

    val rng = kotlin.random.Random(0)
    val result = mutableListOf<Offset>()
    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val alpha = pixels[y * width + x] ushr 24 and 0xFF
            if (alpha > 200) {
                val jx = x + (rng.nextFloat() - 0.5f) * jitter
                val jy = y + (rng.nextFloat() - 0.5f) * jitter
                result += Offset(jx, jy)
            }
            x += gridStep
        }
        y += gridStep
    }
    return result
}
