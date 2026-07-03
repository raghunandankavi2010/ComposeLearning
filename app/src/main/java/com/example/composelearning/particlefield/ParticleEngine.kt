package com.example.composelearning.particlefield

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import kotlin.math.sqrt
import kotlin.random.Random
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor

/**
 * The lifecycle phase of the whole field. [ASSEMBLE] steers every particle onto
 * its target (arrive behaviour); [ESCAPE] throws them outward off-screen, after
 * which the field respawns and assembles the next shape.
 */
private enum class Phase { ASSEMBLE, ESCAPE }

/**
 * A steering-behaviour particle field built as a **structure of arrays**: every
 * attribute (position, velocity, target) is a flat [FloatArray] indexed by
 * particle id, so an update pass is a tight cache-friendly loop with no per-object
 * allocation. Positions are streamed into [renderBuffer] as interleaved
 * `[x0,y0,x1,y1,…]` ready for a single `Canvas.drawPoints` (the raw-points) call.
 *
 * Behaviours, blended each frame:
 * - **arrive** — accelerate toward the target, easing off inside [slowRadius];
 * - **flee** — push away from the pointer within [fleeRadius];
 * - **escape** — during [Phase.ESCAPE], steer outward from centre and leave.
 *
 * Shapes are pre-sampled once into [shapeTargets]; morphing is then just swapping
 * which target array the arrive step reads, so particle *i* slides from its slot
 * in shape A to its slot in shape B — a smooth cross-shape morph.
 */
class ParticleEngine(
    private val width: Int,
    private val height: Int,
    val count: Int,
    private val seed: Long = System.nanoTime()
) {
    private val rng = Random(seed)

    private val px = FloatArray(count)
    private val py = FloatArray(count)
    private val vx = FloatArray(count)
    private val vy = FloatArray(count)
    private val tx = FloatArray(count)
    private val ty = FloatArray(count)

    /** Interleaved `[x0,y0,x1,y1,…]` buffer handed straight to `drawPoints`. */
    val renderBuffer = FloatArray(count * 2)

    private var shapeTargets: List<Pair<FloatArray, FloatArray>> = emptyList()
    var shapeIndex = 0
        private set

    private var phase = Phase.ASSEMBLE
    private var escapeElapsed = 0f

    /** True while particles are flying off-screen; used to pause auto-morphing. */
    val isEscaping: Boolean get() = phase == Phase.ESCAPE

    // ── Tunables (px / px-per-second) ──────────────────────────────────────
    private val maxSpeed = 1500f
    private val slowRadius = 70f
    private val arriveResponse = 7f      // 1/sec — larger = snappier arrive
    private val fleeRadius = 150f
    private val fleeStrength = 2600f
    private val escapeSpeed = 2100f
    private val escapeResponse = 5f
    private val escapeDuration = 1.15f
    private val drag = 0.90f             // per-frame velocity retention baseline

    private val cx = width / 2f
    private val cy = height / 2f

    init {
        // Scatter randomly so the very first shape visibly assembles.
        for (i in 0 until count) {
            px[i] = rng.nextFloat() * width
            py[i] = rng.nextFloat() * height
            vx[i] = (rng.nextFloat() - 0.5f) * 40f
            vy[i] = (rng.nextFloat() - 0.5f) * 40f
        }
        writeRenderBuffer()
    }

    /** Install the pre-sampled shapes and lock onto the first one. */
    fun setShapes(shapes: List<Pair<FloatArray, FloatArray>>) {
        if (shapes.isEmpty()) return
        shapeTargets = shapes
        shapeIndex = 0
        copyTargetsFrom(0)
    }

    /** Morph to the next shape in the ring (retargets the arrive step). */
    fun nextShape() {
        if (shapeTargets.isEmpty()) return
        shapeIndex = (shapeIndex + 1) % shapeTargets.size
        copyTargetsFrom(shapeIndex)
        phase = Phase.ASSEMBLE
    }

    /** Blow the field apart; it re-assembles the next shape when the burst ends. */
    fun triggerEscape() {
        if (shapeTargets.isEmpty()) return
        phase = Phase.ESCAPE
        escapeElapsed = 0f
    }

    private fun copyTargetsFrom(index: Int) {
        val (sx, sy) = shapeTargets[index]
        System.arraycopy(sx, 0, tx, 0, count)
        System.arraycopy(sy, 0, ty, 0, count)
    }

    private fun respawnForReentry() {
        // Reappear from just outside a random edge so they stream back in.
        for (i in 0 until count) {
            when (rng.nextInt(4)) {
                0 -> { px[i] = rng.nextFloat() * width; py[i] = -20f }
                1 -> { px[i] = rng.nextFloat() * width; py[i] = height + 20f }
                2 -> { px[i] = -20f; py[i] = rng.nextFloat() * height }
                else -> { px[i] = width + 20f; py[i] = rng.nextFloat() * height }
            }
            vx[i] = 0f
            vy[i] = 0f
        }
    }

    /**
     * Advance one step. [pointerX]/[pointerY] < 0 means no active touch.
     * Returns true once, on the frame an escape burst completes, so the caller
     * can advance to the next shape.
     */
    fun update(dt: Float, pointerX: Float, pointerY: Float): Boolean {
        if (dt <= 0f) return false
        // dt-normalised drag so behaviour is frame-rate independent.
        val frameDrag = Math.pow(drag.toDouble(), (dt * 60f).toDouble()).toFloat()
        val hasPointer = pointerX >= 0f

        if (phase == Phase.ESCAPE) {
            for (i in 0 until count) {
                var dx = px[i] - cx
                var dy = py[i] - cy
                val d = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                dx /= d; dy /= d
                val desVx = dx * escapeSpeed
                val desVy = dy * escapeSpeed
                vx[i] += (desVx - vx[i]) * escapeResponse * dt
                vy[i] += (desVy - vy[i]) * escapeResponse * dt
                px[i] += vx[i] * dt
                py[i] += vy[i] * dt
            }
            writeRenderBuffer()
            escapeElapsed += dt
            if (escapeElapsed >= escapeDuration) {
                phase = Phase.ASSEMBLE
                respawnForReentry()
                return true
            }
            return false
        }

        // ── ASSEMBLE: arrive + flee, all in one SoA sweep ──────────────────
        for (i in 0 until count) {
            var ax = 0f
            var ay = 0f

            // Arrive toward target, ramping speed down inside slowRadius.
            val dx = tx[i] - px[i]
            val dy = ty[i] - py[i]
            val d = sqrt(dx * dx + dy * dy)
            if (d > 0.001f) {
                val desiredSpeed = if (d < slowRadius) maxSpeed * (d / slowRadius) else maxSpeed
                val desVx = dx / d * desiredSpeed
                val desVy = dy / d * desiredSpeed
                ax += (desVx - vx[i]) * arriveResponse
                ay += (desVy - vy[i]) * arriveResponse
            }

            // Flee: repel from the pointer, strongest at the centre of its bubble.
            if (hasPointer) {
                val fdx = px[i] - pointerX
                val fdy = py[i] - pointerY
                val fd = sqrt(fdx * fdx + fdy * fdy)
                if (fd < fleeRadius && fd > 0.001f) {
                    val f = (1f - fd / fleeRadius) * fleeStrength
                    ax += fdx / fd * f
                    ay += fdy / fd * f
                }
            }

            vx[i] = (vx[i] + ax * dt) * frameDrag
            vy[i] = (vy[i] + ay * dt) * frameDrag
            px[i] += vx[i] * dt
            py[i] += vy[i] * dt
        }
        writeRenderBuffer()
        return false
    }

    private fun writeRenderBuffer() {
        var k = 0
        for (i in 0 until count) {
            renderBuffer[k++] = px[i]
            renderBuffer[k++] = py[i]
        }
    }

    companion object {
        /**
         * Rasterise [text] centred in a [width]×[height] mask and sample its opaque
         * pixels into [count] target slots. Fewer opaque pixels than particles are
         * wrapped modulo (with jitter) so every particle still gets a home; more
         * pixels are decimated by picking every k-th sample.
         */
        fun sampleText(
            text: String,
            width: Int,
            height: Int,
            count: Int,
            rng: Random
        ): Pair<FloatArray, FloatArray> {
            val tx = FloatArray(count)
            val ty = FloatArray(count)
            if (width <= 0 || height <= 0) return tx to ty

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bmp)
            val paint = Paint().apply {
                isAntiAlias = true
                color = AndroidColor.WHITE
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 200f
            }
            // Scale the glyph(s) to ~78% of the smaller screen dimension.
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val target = minOf(width * 0.78f, height * 0.5f)
            val scale = target / maxOf(bounds.width(), bounds.height()).coerceAtLeast(1)
            paint.textSize = 200f * scale
            paint.getTextBounds(text, 0, text.length, bounds)
            canvas.drawText(text, width / 2f, height / 2f - bounds.exactCenterY(), paint)

            val pixels = IntArray(width * height)
            bmp.getPixels(pixels, 0, width, 0, 0, width, height)
            bmp.recycle()

            // Collect opaque samples on a grid step tuned to the canvas size.
            val step = (minOf(width, height) / 220).coerceIn(2, 6)
            val candidates = ArrayList<Float>() // interleaved x,y
            var y = 0
            while (y < height) {
                var x = 0
                while (x < width) {
                    if ((pixels[y * width + x] ushr 24 and 0xFF) > 128) {
                        candidates.add(x.toFloat())
                        candidates.add(y.toFloat())
                    }
                    x += step
                }
                y += step
            }

            val n = candidates.size / 2
            val jitter = step.toFloat()
            if (n == 0) {
                // Blank glyph (e.g. a space): collapse everyone to centre.
                for (i in 0 until count) { tx[i] = width / 2f; ty[i] = height / 2f }
                return tx to ty
            }
            for (i in 0 until count) {
                // Stride through candidates so decimation spreads over the shape.
                val idx = (i.toLong() * n / count).toInt() % n
                tx[i] = candidates[idx * 2] + (rng.nextFloat() - 0.5f) * jitter
                ty[i] = candidates[idx * 2 + 1] + (rng.nextFloat() - 0.5f) * jitter
            }
            return tx to ty
        }
    }
}
