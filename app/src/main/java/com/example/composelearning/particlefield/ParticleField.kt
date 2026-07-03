package com.example.composelearning.particlefield

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random
import android.graphics.Color as AndroidColor

/**
 * Steering-behaviour particle field — a Compose take on the Flutter
 * `animated_particles` package. Particles are sampled from the pixels of each
 * [shapes] string, then **arrive** into the shape, **morph** to the next shape
 * (tap or auto every few seconds), **flee** from your finger (drag), and
 * **escape** off-screen (long-press) before re-assembling. All rendered with a
 * single `Canvas.drawPoints` over [ParticleEngine]'s structure-of-arrays.
 *
 * @param shapes text/glyphs to cycle through; each is sampled to a point cloud.
 * @param particleColor colour of every point (one paint → one raw-points call).
 * @param autoMorphSeconds seconds between automatic morphs; <= 0 disables it.
 */
@Composable
fun ParticleFieldBackground(
    modifier: Modifier = Modifier,
    shapes: List<String> = listOf("Compose", "Steering", "Particles", "★", "Morph"),
    particleColor: Color = Color(0xFF7DF9FF),
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(Color(0xFF0B1026), Color(0xFF05070F))
    ),
    autoMorphSeconds: Float = 4.5f,
    pointSize: Float = 3.4f
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var pointer by remember { mutableStateOf<Offset?>(null) }

    val engine = remember(canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) {
            null
        } else {
            val area = canvasSize.width.toLong() * canvasSize.height.toLong()
            val count = (area / 2600L).toInt().coerceIn(1500, 5000)
            ParticleEngine(canvasSize.width, canvasSize.height, count)
        }
    }

    // Pre-sample every shape once (off the main thread) into the engine.
    LaunchedEffect(engine, shapes) {
        val e = engine ?: return@LaunchedEffect
        val w = canvasSize.width
        val h = canvasSize.height
        val sampled = withContext(Dispatchers.Default) {
            val rng = Random(System.nanoTime())
            shapes.map { ParticleEngine.sampleText(it, w, h, e.count, rng) }
        }
        e.setShapes(sampled)
    }

    var frame by remember { mutableLongStateOf(0L) }

    LaunchedEffect(engine, animationsEnabled) {
        val e = engine ?: return@LaunchedEffect
        if (!animationsEnabled) return@LaunchedEffect
        var lastNanos = withFrameNanos { it }
        var sinceMorph = 0f
        while (isActive) {
            val now = withFrameNanos { it }
            val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
            lastNanos = now

            val p = pointer
            val escaped = e.update(dt, p?.x ?: -1f, p?.y ?: -1f)
            if (escaped) {
                e.nextShape()
                sinceMorph = 0f
            } else if (autoMorphSeconds > 0f && !e.isEscaping) {
                sinceMorph += dt
                if (sinceMorph >= autoMorphSeconds) {
                    e.nextShape()
                    sinceMorph = 0f
                }
            }
            frame++
        }
    }

    val paint = remember(particleColor, pointSize) {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = pointSize
            color = AndroidColor.argb(
                255,
                (particleColor.red * 255).toInt(),
                (particleColor.green * 255).toInt(),
                (particleColor.blue * 255).toInt()
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .onSizeChanged { canvasSize = it }
            // Tap = morph to next shape; long-press = escape burst.
            .pointerInput(engine) {
                detectTapGestures(
                    onTap = { engine?.nextShape() },
                    onLongPress = { engine?.triggerEscape() }
                )
            }
            // Track a pressed pointer so particles can flee from it.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        pointer = if (change != null && change.pressed) change.position else null
                    }
                }
            }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        frame // reading this state redraws every frame

        val e = engine ?: return@Canvas
        drawIntoCanvas { canvas ->
            // The single batched raw-points draw call for the whole field.
            canvas.nativeCanvas.drawPoints(e.renderBuffer, paint)
        }
    }
}

/** Full-screen route: the field plus title, hint and a back button. */
@Composable
fun ParticleFieldScreen(onBack: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize()) {
        ParticleFieldBackground()

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tap → morph   •   Drag → flee   •   Long-press → escape",
                color = Color(0xFF7DF9FF),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }

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
                tint = Color(0xFF7DF9FF)
            )
        }
    }
}
