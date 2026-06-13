package com.example.composelearning.disintegration

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Drives a "disintegration" (Thanos-snap / dissolve) animation for an arbitrary
 * composable. Create one with [rememberDisintegrationState], hand it to
 * [Disintegration], and call [trigger] to play.
 *
 * The whole animation is driven by a single [progress] float in `0f..1f`. Every
 * spatial behaviour (which part of the image is still solid, which pixels have
 * turned to dust, how far that dust has drifted) is derived analytically from
 * that one value, so the per-frame work is just "advance one float and redraw".
 *
 * @param durationMillis total play time of the dissolve.
 * @param band width of the moving wavefront, in `progress` units. A column does
 *   not dissolve instantly — it fades over a window of this width as the front
 *   sweeps past it. Larger = softer, more gradual edge.
 * @param maxParticles upper bound on flying particles. The particle cell size is
 *   auto-derived from the captured bitmap so the count stays under this, keeping
 *   the draw loop cheap regardless of image resolution.
 * @param particleCell explicit particle cell size in px; `0f` means auto.
 * @param stripWidthPx width of each redrawn image slice in px. Thinner = smoother
 *   wavefront, more `drawImage` calls.
 * @param driftFactor how far dust travels, as a fraction of the largest image side.
 */
@Stable
class DisintegrationState(
    val durationMillis: Int = 1500,
    val band: Float = 0.40f,
    val maxParticles: Int = 4500,
    val particleCell: Float = 0f,
    val stripWidthPx: Float = 3f,
    val driftFactor: Float = 0.18f
) {
    /** True from [trigger] until [reset]; while true the dissolve is rendered. */
    var triggered by mutableStateOf(false)
        private set

    /** Animation clock in `0f..1f`. Read in the draw phase to derive every frame. */
    var progress by mutableFloatStateOf(0f)
        internal set

    internal var source: ImageBitmap? by mutableStateOf(null)
    internal var particles: ParticleField? by mutableStateOf(null)
    internal var strips: List<Strip> by mutableStateOf(emptyList())

    /** Start the dissolve. No-op if already triggered. */
    fun trigger() {
        if (!triggered) {
            progress = 0f
            triggered = true
        }
    }

    /** Restore the original content and discard the captured particle data. */
    fun reset() {
        triggered = false
        progress = 0f
        particles = null
        strips = emptyList()
        source = null
    }

    internal fun finish() {
        progress = 1f
    }
}

@Composable
fun rememberDisintegrationState(
    durationMillis: Int = 1500,
    band: Float = 0.40f,
    maxParticles: Int = 4500,
    particleCell: Float = 0f,
    stripWidthPx: Float = 3f,
    driftFactor: Float = 0.18f
): DisintegrationState = remember(durationMillis, band, maxParticles, particleCell, stripWidthPx, driftFactor) {
    DisintegrationState(
        durationMillis = durationMillis,
        band = band,
        maxParticles = maxParticles,
        particleCell = particleCell,
        stripWidthPx = stripWidthPx,
        driftFactor = driftFactor
    )
}

/**
 * Wraps [content] and, when [state] is triggered, dissolves it into drifting
 * pixels. Works with any composable — an image, text, or a whole layout — because
 * it never inspects what it wraps: it snapshots the rendered pixels via a
 * [androidx.compose.ui.graphics.layer.GraphicsLayer] and animates that snapshot.
 *
 * Pipeline:
 *  1. `drawWithContent` records [content] into a GraphicsLayer every frame.
 *  2. On trigger, the layer is turned into an [ImageBitmap]; [buildEffect]
 *     reads its pixels **off the main thread** to produce strips + particles.
 *  3. A `withFrameMillis` loop advances [DisintegrationState.progress]; the draw
 *     phase renders strips (the eroding image) and particles (the dust).
 */
@Composable
fun Disintegration(
    state: DisintegrationState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(state.triggered) {
        if (!state.triggered) return@LaunchedEffect

        // The layer has already recorded at least one frame of `content` by now,
        // so we can read its pixels back as a bitmap.
        val bitmap = graphicsLayer.toImageBitmap()
        state.source = bitmap

        // Heavy work — reading every sampled pixel — runs off the main thread.
        val (field, strips) = withContext(Dispatchers.Default) {
            buildEffect(
                bitmap = bitmap,
                maxParticles = state.maxParticles,
                requestedCell = state.particleCell,
                stripWidthPx = state.stripWidthPx,
                driftFactor = state.driftFactor
            )
        }
        state.particles = field
        state.strips = strips

        // Drive progress off the frame clock for smooth 60–120 FPS playback.
        val startTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val p = ((now - startTime).toFloat() / state.durationMillis).coerceIn(0f, 1f)
            state.progress = p
            if (p >= 1f) break
        }
        state.finish()
    }

    Box(
        modifier = modifier.drawWithContent {
            // Record the live content into the layer (keeps the snapshot fresh
            // until the moment we trigger).
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            val field = state.particles
            val strips = state.strips
            val src = state.source

            if (!state.triggered || field == null || src == null || strips.isEmpty()) {
                // Idle / not yet captured: just draw the recorded content.
                drawLayer(graphicsLayer)
            } else {
                // The eroding image, then the dust on top of it.
                drawStrips(src, strips, state.progress, state.band)
                drawParticles(field, state.progress, state.band)
            }
        }
    ) { content() }
}

// ── Wavefront math ──────────────────────────────────────────────────────────
//
// A single horizontal front sweeps left → right. For a normalized x-position
// `seed` in 0..1, the local dissolve fraction is:
//
//     f = ((progress * (1 + band) - seed) / band).coerceIn(0, 1)
//
//   • f == 0  → still fully solid (the front hasn't arrived)
//   • 0 < f<1 → dissolving (inside the moving band)
//   • f == 1  → fully gone
//
// At progress 0 every f is 0 (all solid); at progress 1 every f is 1 (all gone).
private fun dissolveFraction(seed: Float, progress: Float, band: Float): Float {
    val raw = (progress * (1f + band) - seed) / band
    return raw.coerceIn(0f, 1f)
}

/** Draws the still-coherent image as vertical slices, each faded by the front. */
private fun DrawScope.drawStrips(
    bitmap: ImageBitmap,
    strips: List<Strip>,
    progress: Float,
    band: Float
) {
    val h = bitmap.height
    for (strip in strips) {
        val f = dissolveFraction(strip.seed, progress, band)
        val alpha = 1f - f
        if (alpha <= 0.001f) continue
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(strip.x, 0),
            srcSize = IntSize(strip.width, h),
            dstOffset = IntOffset(strip.x, 0),
            dstSize = IntSize(strip.width, h),
            alpha = alpha
        )
    }
}

/** Draws the detached pixels: they accelerate up-and-out and puff away. */
private fun DrawScope.drawParticles(
    field: ParticleField,
    progress: Float,
    band: Float
) {
    val cell = field.cell
    val size = Size(cell, cell)
    for (i in 0 until field.count) {
        val f = dissolveFraction(field.seed[i], progress, band)
        if (f <= 0f || f >= 1f) continue

        // Accelerating travel; opacity is a bell that peaks mid-flight (0 at the
        // edges) so dust appears from the image and dissolves into nothing.
        val travel = f * f
        val alpha = (f * (1f - f) * 4f).coerceIn(0f, 1f)
        val x = field.startX[i] + field.driftX[i] * travel
        val y = field.startY[i] + field.driftY[i] * travel

        drawRect(
            color = Color(field.color[i]),
            topLeft = Offset(x, y),
            size = size,
            alpha = alpha
        )
    }
}

/** A vertical slice of the source bitmap; [seed] is its normalized centre x. */
internal class Strip(
    val x: Int,
    val width: Int,
    val seed: Float
)

/**
 * Particle data laid out in parallel primitive arrays (not a `List<Particle>`)
 * to avoid per-object allocation and keep the hot draw loop cache-friendly.
 */
internal class ParticleField(
    val count: Int,
    val cell: Float,
    val seed: FloatArray,
    val startX: FloatArray,
    val startY: FloatArray,
    val driftX: FloatArray,
    val driftY: FloatArray,
    val color: IntArray
)

/**
 * Reads the captured bitmap once and produces the strips + particles. Runs on
 * [Dispatchers.Default]; `toPixelMap()` and the per-cell sampling are the
 * expensive steps that must stay off the main thread.
 */
private fun buildEffect(
    bitmap: ImageBitmap,
    maxParticles: Int,
    requestedCell: Float,
    stripWidthPx: Float,
    driftFactor: Float
): Pair<ParticleField, List<Strip>> {
    val w = bitmap.width
    val h = bitmap.height
    val pixels = bitmap.toPixelMap()

    // Choose a cell size so the grid never exceeds maxParticles.
    val autoCell = sqrt(w.toFloat() * h / maxParticles).coerceAtLeast(1f)
    val cell = (if (requestedCell > 0f) requestedCell else autoCell).coerceAtLeast(2f)
    val cols = floor(w / cell).toInt().coerceAtLeast(1)
    val rows = floor(h / cell).toInt().coerceAtLeast(1)

    val driftDist = max(w, h) * driftFactor
    val rnd = Random(0x5EED)

    val capacity = cols * rows
    val seed = FloatArray(capacity)
    val startX = FloatArray(capacity)
    val startY = FloatArray(capacity)
    val driftX = FloatArray(capacity)
    val driftY = FloatArray(capacity)
    val color = IntArray(capacity)

    var n = 0
    for (cy in 0 until rows) {
        val sy = cy * cell
        val py = (sy + cell * 0.5f).toInt().coerceIn(0, h - 1)
        for (cx in 0 until cols) {
            val sx = cx * cell
            val px = (sx + cell * 0.5f).toInt().coerceIn(0, w - 1)
            val c = pixels[px, py]
            if (c.alpha < 0.05f) continue // skip transparent pixels (text, gaps)

            seed[n] = (sx + cell * 0.5f) / w
            startX[n] = sx
            startY[n] = sy
            // Dust trails the front (rightward) and floats up, with randomness.
            driftX[n] = (0.25f + rnd.nextFloat() * 0.9f) * driftDist
            driftY[n] = -(0.15f + rnd.nextFloat() * 0.7f) * driftDist
            color[n] = c.toArgb()
            n++
        }
    }

    val field = ParticleField(
        count = n,
        cell = cell,
        seed = seed.copyOf(n),
        startX = startX.copyOf(n),
        startY = startY.copyOf(n),
        driftX = driftX.copyOf(n),
        driftY = driftY.copyOf(n),
        color = color.copyOf(n)
    )

    // Strips: contiguous vertical slices covering the full width.
    val stripW = stripWidthPx.toInt().coerceAtLeast(2)
    val strips = ArrayList<Strip>(w / stripW + 1)
    var x = 0
    while (x < w) {
        val sw = minOf(stripW, w - x)
        strips.add(Strip(x = x, width = sw, seed = (x + sw * 0.5f) / w))
        x += sw
    }

    return field to strips
}
