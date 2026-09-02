package com.example.composelearning.petparticles

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.util.LocalAnimationsEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Follows the finger without ever touching snapshot state. Pointer events
 * arrive far more often than frames, and every `mutableStateOf` write would
 * schedule work on the Compose runtime; the simulation only ever reads these
 * fields once per frame, so plain vars are both correct and free.
 */
private class PointerTracker {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var active = false
    private var lastMs = 0L

    fun onMove(nx: Float, ny: Float, timeMs: Long) {
        if (active && lastMs != 0L && timeMs > lastMs) {
            val dt = ((timeMs - lastMs) / 1000f).coerceIn(0.004f, 0.05f)
            // One-pole low-pass on the instantaneous velocity; raw finger
            // deltas are far too spiky to drive a force directly.
            vx += ((nx - x) / dt - vx) * 0.35f
            vy += ((ny - y) / dt - vy) * 0.35f
        }
        x = nx
        y = ny
        lastMs = timeMs
        active = true
    }

    fun release() {
        active = false
        vx = 0f
        vy = 0f
        lastMs = 0L
    }

    /** Bleed off velocity when the finger is held still (no events arrive). */
    fun decay(dt: Float) {
        val k = Math.pow(0.86, (dt * 60f).toDouble()).toFloat()
        vx *= k
        vy *= k
    }
}

/** Immutable readout for the stats line, kept out of the hot loop. */
private data class FieldStats(val particles: Int, val frameMs: Float)

/** Everything the draw phase needs, built once per engine. */
private class PetPaints(
    val fill: List<Paint>,
    val glow: List<Paint?>,
    val eyeTone: BooleanArray
)

private fun buildPaints(palette: List<FurTone>, dotSize: Float): PetPaints {
    val fill = palette.map { tone ->
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = dotSize * tone.sizeScale
            color = tone.argb
        }
    }
    val glow = palette.map { tone ->
        if (!tone.glow) null else Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = dotSize * tone.sizeScale * 3.4f
            color = tone.argb
            alpha = 46
        }
    }
    return PetPaints(fill, glow, BooleanArray(palette.size) { palette[it].isEye })
}

/**
 * A cat and a dog made of a few thousand dots that behave like fur: they idle,
 * breathe, blink and wag, scatter under your finger and spring back home.
 *
 * The whole screen is **one** `Canvas` node. Nothing recomposes per frame — the
 * animation loop only bumps an integer that is read inside the draw lambda, so
 * Compose re-runs the draw phase and skips composition and layout entirely.
 */
@Composable
fun PetParticlesScreen(onBack: () -> Unit = {}) {
    val animationsEnabled = LocalAnimationsEnabled.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var engine by remember { mutableStateOf<PetParticleEngine?>(null) }
    var stats by remember { mutableStateOf(FieldStats(0, 0f)) }
    var selectedPet by remember { mutableIntStateOf(0) }
    val pointer = remember { PointerTracker() }

    // Rasterising and sampling both pets costs a few ms — do it off the main
    // thread, then hand the finished clouds to a fresh engine.
    LaunchedEffect(canvasSize) {
        if (canvasSize.width == 0 || canvasSize.height == 0) return@LaunchedEffect
        val w = canvasSize.width.toFloat()
        val h = canvasSize.height.toFloat()
        val maskSize = PetParticleEngine.maskSizeFor(PetParticleEngine.fitFor(w, h))
        val clouds = withContext(Dispatchers.Default) {
            PetSampler.sampleAll(
                pets = Pet.entries,
                maskSize = maskSize,
                maxCount = 9000,
                rng = Random(1234)
            )
        }
        engine = PetParticleEngine(w, h, clouds, maskSize)
        selectedPet = 0
    }

    // The one piece of state the frame loop writes. It is read *only* inside
    // the draw lambda below, which is what keeps the invalidation draw-only.
    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(engine, animationsEnabled) {
        val e = engine ?: return@LaunchedEffect
        if (!animationsEnabled) return@LaunchedEffect
        var last = withFrameNanos { it }
        var accumMs = 0f
        var accumFrames = 0
        while (isActive) {
            val now = withFrameNanos { it }
            // Clamp: a dropped frame becomes slow motion, never an explosion.
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.033f)
            last = now

            val t0 = System.nanoTime()
            pointer.decay(dt)
            e.update(dt, pointer.x, pointer.y, pointer.vx, pointer.vy, pointer.active)
            accumMs += (System.nanoTime() - t0) / 1_000_000f
            accumFrames++
            if (accumFrames >= 30) {
                stats = FieldStats(e.count, accumMs / accumFrames)
                accumMs = 0f
                accumFrames = 0
            }
            frame++
        }
    }

    val paints = remember(engine) {
        val e = engine ?: return@remember emptyList<PetPaints>()
        val dot = e.spacing * 1.15f
        Pet.entries.mapIndexed { i, _ -> buildPaints(e.cloudAt(i).palette, dot) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141024), Color(0xFF0A0912), Color(0xFF05050A))
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(engine) {
                    detectTapGestures(
                        onTap = { engine?.purr(it.x, it.y) },
                        onLongPress = { engine?.shake() }
                    )
                }
                .pointerInput(engine) {
                    awaitPointerEventScope {
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull()
                            if (change != null && change.pressed) {
                                pointer.onMove(
                                    change.position.x,
                                    change.position.y,
                                    change.uptimeMillis
                                )
                            } else {
                                pointer.release()
                            }
                        }
                    }
                }
        ) {
            @Suppress("UNUSED_EXPRESSION")
            frame // draw-phase read: this is what schedules the next frame's draw

            val e = engine ?: return@Canvas
            val sets = paints
            if (sets.isEmpty()) return@Canvas

            // A soft contact shadow so the pet is standing on something.
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x33000000), Color.Transparent),
                    center = Offset(e.originX + e.fit * 0.5f, e.originY + e.fit * 0.985f),
                    radius = e.fit * 0.42f
                ),
                topLeft = Offset(e.originX + e.fit * 0.08f, e.originY + e.fit * 0.93f),
                size = Size(e.fit * 0.84f, e.fit * 0.11f)
            )

            val set = sets[e.petIndex]
            val starts = e.bucketStart
            val eyeAlpha = (e.eyeOpen * 255f).toInt().coerceIn(0, 255)

            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                for (b in set.fill.indices) {
                    val from = starts[b]
                    val n = starts[b + 1] - from
                    if (n <= 0) continue
                    val offset = from shl 1
                    val values = n shl 1

                    set.glow[b]?.let { glowPaint ->
                        if (set.eyeTone[b]) glowPaint.alpha = eyeAlpha * 46 / 255
                        native.drawPoints(e.renderBuffer, offset, values, glowPaint)
                    }
                    val paint = set.fill[b]
                    if (set.eyeTone[b]) paint.alpha = eyeAlpha
                    native.drawPoints(e.renderBuffer, offset, values, paint)
                }
            }
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
                tint = Color(0xFFE8DFF5)
            )
        }

        StatsBadge(
            stats = { stats },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(12.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Drag to ruffle the fur  •  Tap to purr  •  Long-press to shake",
                color = Color(0x99FFFFFF),
                fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pet.entries.forEachIndexed { index, pet ->
                    PetPill(
                        label = pet.label,
                        selected = selectedPet == index,
                        onClick = {
                            selectedPet = index
                            engine?.setPet(index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * The stats read is deferred behind a lambda, so the twice-a-second update
 * recomposes this badge alone instead of the whole screen.
 */
@Composable
private fun StatsBadge(stats: () -> FieldStats, modifier: Modifier = Modifier) {
    val value = stats()
    if (value.particles == 0) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color(0x22FFFFFF)
    ) {
        Text(
            text = "${value.particles} dots  •  ${"%.2f".format(value.frameMs)} ms sim",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PetPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFFE89A52) else Color(0x1FFFFFFF),
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 9.dp),
            color = if (selected) Color(0xFF1A1206) else Color(0xCCFFFFFF),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}
