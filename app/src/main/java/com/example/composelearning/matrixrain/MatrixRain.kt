package com.example.composelearning.matrixrain

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlinx.coroutines.isActive
import kotlin.math.pow
import kotlin.random.Random
import android.graphics.Color as AndroidColor

private const val MATRIX_CHARS =
    "0123456789" +
        "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン" +
        "ｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ" +
        "=+*<>¦｜╌:."

/**
 * One vertical stream of glyphs.
 *
 * [head] is the fractional row index of the leading (brightest) glyph. The tail
 * follows behind it for [trailLength] rows. [brightness] dims whole columns so
 * some streams read as "further away", giving the wall of rain some depth.
 */
private class RainColumn(
    var head: Float,
    var speed: Float,
    var trailLength: Int,
    val brightness: Float,
    val glyphs: CharArray
)

private fun newColumn(rng: Random): RainColumn = RainColumn(
    // Stagger spawns well above the top so columns don't all fall in lockstep.
    head = rng.nextFloat() * -60f,
    speed = rng.nextFloat() * 14f + 7f,
    trailLength = rng.nextInt(10, 28),
    brightness = rng.nextFloat() * 0.6f + 0.4f,
    glyphs = CharArray(96) { MATRIX_CHARS.random(rng) }
)

/**
 * Classic "Matrix digital rain": columns of glyphs streaming downward, each led
 * by a glowing near-white head that fades through phosphor green into black.
 *
 * Improvements over a naive version: a blurred glow on the head glyph, an
 * exponential (not linear) trail fade, per-column depth dimming, glyph flicker,
 * and a fade-in as each stream re-spawns.
 */
@Composable
fun MatrixRainBackground(
    modifier: Modifier = Modifier,
    fontSize: Dp = 18.dp,
    glyphColor: Color = Color(0xFF00FF41),
    backgroundColor: Color = Color.Black
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val animationsEnabled = LocalAnimationsEnabled.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val columns = remember(canvasSize, fontSizePx) {
        if (canvasSize.width == 0 || fontSizePx == 0f) {
            emptyList()
        } else {
            val rng = Random(System.nanoTime())
            val columnCount = (canvasSize.width / fontSizePx).toInt() + 1
            List(columnCount) { newColumn(rng) }
        }
    }

    var frame by remember { mutableLongStateOf(0L) }

    LaunchedEffect(columns, fontSizePx, animationsEnabled) {
        if (columns.isEmpty() || !animationsEnabled) return@LaunchedEffect
        val rng = Random(System.nanoTime())
        var lastNanos = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            // Clamp dt so a dropped frame / resume doesn't teleport every stream.
            val deltaSeconds = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
            lastNanos = now
            val rowCount = (canvasSize.height / fontSizePx).toInt() + 10

            columns.forEach { column ->
                column.head += column.speed * deltaSeconds
                // Random glyph mutation gives the shimmering, "decoding" look.
                if (rng.nextFloat() < 0.18f) {
                    column.glyphs[rng.nextInt(column.glyphs.size)] = MATRIX_CHARS.random(rng)
                }
                // Once the whole trail has fallen off the bottom, respawn it.
                if (column.head - column.trailLength > rowCount) {
                    column.head = rng.nextFloat() * -20f
                    column.speed = rng.nextFloat() * 14f + 7f
                    column.trailLength = rng.nextInt(10, 28)
                }
            }
            frame++
        }
    }

    val headPaint = remember(fontSizePx) {
        Paint().apply {
            isAntiAlias = true
            textSize = fontSizePx
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
    }
    // Separate paint carries a blur mask so the head glyph blooms like phosphor.
    val glowPaint = remember(fontSizePx) {
        Paint().apply {
            isAntiAlias = true
            textSize = fontSizePx
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            maskFilter = BlurMaskFilter(fontSizePx * 0.5f, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val trailPaint = remember(fontSizePx) {
        Paint().apply {
            isAntiAlias = true
            textSize = fontSizePx
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
    }

    val glyphRgb = remember(glyphColor) {
        Triple(
            (glyphColor.red * 255).toInt(),
            (glyphColor.green * 255).toInt(),
            (glyphColor.blue * 255).toInt()
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onSizeChanged { canvasSize = it }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        frame // reading this state redraws this scope every frame

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            columns.forEachIndexed { colIndex, column ->
                val x = colIndex * fontSizePx + fontSizePx / 2f
                // A freshly respawned head fades in over its first few rows.
                val spawnFade = (column.head / 4f).coerceIn(0f, 1f)

                for (i in 0 until column.trailLength) {
                    val rowFloat = column.head - i
                    if (rowFloat < 0f) continue
                    val row = rowFloat.toInt()
                    val y = row * fontSizePx + fontSizePx
                    val glyph = column.glyphs[(row + colIndex) % column.glyphs.size]

                    // Exponential fade reads as a longer, more natural tail than linear.
                    val fraction = (1f - i.toFloat() / column.trailLength).pow(1.4f)
                    val dim = column.brightness * spawnFade

                    if (i == 0) {
                        // Leading glyph: glow bloom, then a near-white core on top.
                        val headAlpha = (255 * dim).toInt().coerceIn(0, 255)
                        glowPaint.color = AndroidColor.argb(
                            (160 * dim).toInt().coerceIn(0, 255),
                            glyphRgb.first, glyphRgb.second, glyphRgb.third
                        )
                        nativeCanvas.drawText(glyph.toString(), x, y, glowPaint)
                        headPaint.color = AndroidColor.argb(headAlpha, 210, 255, 220)
                        nativeCanvas.drawText(glyph.toString(), x, y, headPaint)
                    } else {
                        val alpha = (fraction * 255 * dim).toInt().coerceIn(0, 255)
                        trailPaint.color = AndroidColor.argb(
                            alpha,
                            (glyphRgb.first * fraction).toInt().coerceIn(0, 255),
                            (glyphRgb.second * fraction + 30).toInt().coerceIn(0, 255),
                            (glyphRgb.third * fraction).toInt().coerceIn(0, 255)
                        )
                        nativeCanvas.drawText(glyph.toString(), x, y, trailPaint)
                    }
                }
            }
        }
    }
}

/** Full-screen route: the rain plus a title overlay and a back button. */
@Composable
fun MatrixRainScreen(onBack: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize()) {
        MatrixRainBackground()

        Text(
            text = "Wake up, Neo…",
            color = Color(0xFF00FF41),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .systemBarsPadding()
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
                tint = Color(0xFF00FF41)
            )
        }
    }
}
