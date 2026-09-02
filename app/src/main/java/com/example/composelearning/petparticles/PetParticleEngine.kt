package com.example.composelearning.petparticles

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * A sine/cosine lookup table. At 6–8k particles we evaluate trig several times
 * per particle per frame; a 2048-entry table turns each of those into one
 * multiply, one truncation, one mask and one array read. The table is indexed
 * modulo its length via a bitmask, which is why the size must be a power of two
 * — and why negative angles wrap correctly for free (two's complement).
 */
private object Trig {
    private const val N = 2048
    private const val MASK = N - 1
    private val SIN = FloatArray(N) { kotlin.math.sin(it * 2.0 * PI / N).toFloat() }
    private val TO_INDEX = N / (2f * PI.toFloat())
    private const val QUARTER = N / 4

    fun sin(a: Float): Float = SIN[(a * TO_INDEX).toInt() and MASK]
    fun cos(a: Float): Float = SIN[((a * TO_INDEX).toInt() + QUARTER) and MASK]
}

/**
 * The simulation. Every particle attribute lives in its own flat [FloatArray]
 * indexed by particle id (a *structure of arrays*), so one update is a single
 * tight loop that walks memory forwards and allocates nothing. Positions are
 * streamed straight into [renderBuffer] as interleaved `[x0,y0,x1,y1,…]`, ready
 * for `Canvas.drawPoints`.
 *
 * Each particle is a **damped spring** anchored to its home pixel in the pet
 * portrait. Everything else is an extra force added on top of that spring
 * before integration:
 *
 * - **breathing** — the whole home field scales about the pet's base;
 * - **head bob** and **tail wag** — home positions offset / rotated by a
 *   per-particle weight, so only the right body parts move;
 * - **shimmer** — a per-particle phase offset makes the coat never quite still;
 * - **repel + swirl + advection** — the finger pushes fur aside, drags it along
 *   and curls it around, which is what makes petting feel physical;
 * - **purr ripple** — an expanding ring of outward force from a tap.
 *
 * Integration is semi-implicit (symplectic) Euler: velocity is updated first,
 * then position uses the *new* velocity. It costs the same as explicit Euler
 * and is dramatically more stable for springs.
 */
class PetParticleEngine(
    private val viewW: Float,
    private val viewH: Float,
    private val clouds: List<PetCloud>,
    maskSize: Int,
    seed: Long = System.nanoTime()
) {
    private val rng = Random(seed)

    val count: Int = clouds.first().count

    // ── Fit: the artwork's unit square mapped onto the canvas ──────────────
    /** Side length in px of the square the pet is drawn into. */
    val fit: Float = fitFor(viewW, viewH)
    val originX: Float = (viewW - fit) / 2f
    val originY: Float = viewH * 0.50f - fit * 0.50f

    /** Distance between neighbouring particles; the dot size is derived from it. */
    val spacing: Float = fit / maskSize

    // ── Per-particle state (structure of arrays) ───────────────────────────
    private val px = FloatArray(count)
    private val py = FloatArray(count)
    private val vx = FloatArray(count)
    private val vy = FloatArray(count)
    private val hx = FloatArray(count)   // home / rest position, in px
    private val hy = FloatArray(count)
    private val wag = FloatArray(count)  // 0..1 tail-wag weight
    private val bob = FloatArray(count)  // 0..1 head-bob weight
    private val phase = FloatArray(count)

    /** Interleaved positions handed straight to `drawPoints`. */
    val renderBuffer = FloatArray(count * 2)

    var petIndex: Int = 0
        private set

    val cloud: PetCloud get() = clouds[petIndex]
    fun cloudAt(index: Int): PetCloud = clouds[index]
    val bucketStart: IntArray get() = clouds[petIndex].bucketStart
    val palette: List<FurTone> get() = clouds[petIndex].palette

    /** 1 = eyes wide open, 0 = fully shut. Applied to the eye tones' alpha. */
    var eyeOpen: Float = 1f
        private set

    // ── Tunables ───────────────────────────────────────────────────────────
    /**
     * Spring constant, in 1/s². With ω = sqrt(k) = 18.4 rad/s the 2 % settling
     * time is 4 / (zeta·ω) ≈ 0.30 s, and zeta = 0.72 leaves ~4 % overshoot — a
     * hint of bounce on the way home rather than a dead stop.
     */
    private val stiffness = 340f
    private val dampingRatio = 0.72f
    private val damping = 2f * dampingRatio * sqrt(stiffness)

    private val touchRadius = fit * 0.30f
    private val touchRadiusSq = touchRadius * touchRadius
    private val pushStrength = 26_000f
    private val swirlStrength = 14_000f
    private val advection = 9f
    private val maxSpeed = fit * 4.5f
    private val maxSpeedSq = maxSpeed * maxSpeed

    private val breathAmount = 0.014f
    private val bobAmount = fit * 0.007f
    private val wagAmount = 0.26f          // radians at full weight
    private val shimmer = fit * 0.0035f

    // ── Time-varying state ─────────────────────────────────────────────────
    private var time = 0f
    private var blinkCountdown = 2.5f
    private var blinkPhase = 0f
    private val blinkDuration = 0.17f

    private var rippleActive = false
    private var rippleX = 0f
    private var rippleY = 0f
    private var rippleR = 0f
    private val rippleSpeed: Float = fit * 1.9f
    private val rippleMax: Float = fit * 1.15f
    private val rippleBand: Float = fit * 0.10f
    private val rippleStrength = 30_000f

    init {
        applyCloud(0)
        // Start scattered so the very first assembly is visible.
        for (i in 0 until count) {
            val a = rng.nextFloat() * 2f * PI.toFloat()
            val r = fit * (0.6f + rng.nextFloat() * 0.9f)
            px[i] = viewW / 2f + Trig.cos(a) * r
            py[i] = viewH / 2f + Trig.sin(a) * r
        }
        writeRenderBuffer()
    }

    /** Switch pets; particle *i* simply re-targets to pet B's *i*-th sample. */
    fun setPet(index: Int) {
        if (index == petIndex || index !in clouds.indices) return
        applyCloud(index)
        burst(0.55f)
    }

    /** Scatter the coat and let the springs pull it back — a shake-off. */
    fun shake() = burst(1f)

    /** A ring of outward force that sweeps through the body from [x], [y]. */
    fun purr(x: Float, y: Float) {
        rippleX = x
        rippleY = y
        rippleR = 0f
        rippleActive = true
    }

    private fun applyCloud(index: Int) {
        petIndex = index
        val c = clouds[index]
        val tpx = c.tailPivotX
        val tpy = c.tailPivotY
        for (i in 0 until count) {
            val ux = c.ux[i]
            val uy = c.uy[i]
            hx[i] = originX + ux * fit
            hy[i] = originY + uy * fit

            // Tail wag: only tagged particles rotate, and the further from the
            // pivot the more they swing — that is what a real tail does.
            wag[i] = if (c.tail[i].toInt() == 0) {
                0f
            } else {
                val dx = ux - tpx
                val dy = uy - tpy
                (sqrt(dx * dx + dy * dy) / 0.32f).coerceIn(0f, 1f)
            }
            // Head bob: a smooth ramp so the neck doesn't shear off the body.
            bob[i] = smoothstep(0.50f, 0.30f, uy)
            phase[i] = rng.nextFloat() * 2f * PI.toFloat()
        }
    }

    private fun burst(scale: Float) {
        for (i in 0 until count) {
            val a = rng.nextFloat() * 2f * PI.toFloat()
            val s = fit * (0.5f + rng.nextFloat() * 1.3f) * scale
            vx[i] += Trig.cos(a) * s
            vy[i] += Trig.sin(a) * s
        }
    }

    /**
     * Advance the simulation by [dt] seconds.
     *
     * [dt] must already be clamped by the caller. A damped spring stepped with
     * semi-implicit Euler is stable while `dt < (-c + sqrt(c² + 4k)) / k`,
     * which for k = 340 and c = 26.6 is ≈ 0.056 s. The caller clamps to
     * 0.033 s, so a dropped frame becomes slow motion instead of an explosion.
     * (The often-quoted `dt < 2/sqrt(k)` is the *undamped* limit — damping
     * makes the real bound tighter, not looser.)
     */
    fun update(
        dt: Float,
        touchX: Float,
        touchY: Float,
        touchVx: Float,
        touchVy: Float,
        touching: Boolean
    ) {
        if (dt <= 0f) return
        time += dt

        // ── Per-frame constants, hoisted out of the particle loop ──────────
        val breath = 1f + breathAmount * Trig.sin(time * 1.65f)
        val bobNow = Trig.sin(time * 1.15f) * bobAmount
        val wagNow = Trig.sin(time * 2.7f) * wagAmount
        val basePivotX = originX + fit * 0.5f
        val basePivotY = originY + fit          // breathe about the seated base
        val tailPivotX = originX + cloud.tailPivotX * fit
        val tailPivotY = originY + cloud.tailPivotY * fit

        val touchSpeed = sqrt(touchVx * touchVx + touchVy * touchVy)
        val swirlNow = swirlStrength * (touchSpeed / (fit * 3f)).coerceIn(0f, 1f)

        updateBlink(dt)
        if (rippleActive) {
            rippleR += rippleSpeed * dt
            if (rippleR > rippleMax) rippleActive = false
        }
        val rippleFade = if (rippleActive) 1f - rippleR / rippleMax else 0f
        val rippleForce = rippleStrength * rippleFade
        val invBand = 1f / rippleBand

        for (i in 0 until count) {
            // ── Where should this particle want to be, right now? ──────────
            var tx = hx[i]
            var ty = hy[i]

            // Breathing: uniform scale about the base.
            tx = basePivotX + (tx - basePivotX) * breath
            ty = basePivotY + (ty - basePivotY) * breath

            val bw = bob[i]
            if (bw > 0f) ty += bobNow * bw

            val ww = wag[i]
            if (ww > 0f) {
                // Rotation about the tail pivot: the standard 2×2 rotation
                // matrix, with the angle scaled per particle.
                val a = wagNow * ww
                val s = Trig.sin(a)
                val c = Trig.cos(a)
                val rx = tx - tailPivotX
                val ry = ty - tailPivotY
                tx = tailPivotX + rx * c - ry * s
                ty = tailPivotY + rx * s + ry * c
            }

            // Shimmer: a Lissajous wobble with a per-particle phase, so the
            // coat never freezes into a static bitmap.
            val ph = phase[i] + time * 1.9f
            tx += Trig.sin(ph) * shimmer
            ty += Trig.cos(ph * 0.87f) * shimmer

            // ── Forces ─────────────────────────────────────────────────────
            // Hooke's law toward home, minus viscous damping.
            var ax = (tx - px[i]) * stiffness - vx[i] * damping
            var ay = (ty - py[i]) * stiffness - vy[i] * damping

            if (touching) {
                val dx = px[i] - touchX
                val dy = py[i] - touchY
                val d2 = dx * dx + dy * dy
                // Compare squared distances: no sqrt for particles we reject.
                if (d2 < touchRadiusSq && d2 > 1e-4f) {
                    val d = sqrt(d2)
                    val nx = dx / d
                    val ny = dy / d
                    // Quadratic falloff: zero value *and* zero slope at the rim,
                    // so particles don't pop as the finger sweeps over them.
                    val f = 1f - d / touchRadius
                    val ff = f * f
                    ax += nx * pushStrength * ff
                    ay += ny * pushStrength * ff
                    // Perpendicular component: curls the fur around the finger.
                    ax += -ny * swirlNow * ff
                    ay += nx * swirlNow * ff
                    // Advection: fur gets carried along with the stroke.
                    ax += touchVx * advection * ff
                    ay += touchVy * advection * ff
                }
            }

            if (rippleActive) {
                val dx = px[i] - rippleX
                val dy = py[i] - rippleY
                val d = sqrt(dx * dx + dy * dy)
                if (d > 1e-4f) {
                    // Triangular band around the expanding radius — the cheap
                    // stand-in for a Gaussian shell.
                    val band = 1f - abs(d - rippleR) * invBand
                    if (band > 0f) {
                        val f = band * rippleForce
                        ax += dx / d * f
                        ay += dy / d * f
                    }
                }
            }

            // ── Integrate (semi-implicit Euler) ────────────────────────────
            var nvx = vx[i] + ax * dt
            var nvy = vy[i] + ay * dt
            val sp2 = nvx * nvx + nvy * nvy
            if (sp2 > maxSpeedSq) {
                val k = maxSpeed / sqrt(sp2)
                nvx *= k
                nvy *= k
            }
            vx[i] = nvx
            vy[i] = nvy
            val x = px[i] + nvx * dt
            val y = py[i] + nvy * dt
            px[i] = x
            py[i] = y

            val o = i shl 1
            renderBuffer[o] = x
            renderBuffer[o + 1] = y
        }
    }

    private fun updateBlink(dt: Float) {
        if (blinkPhase > 0f) {
            blinkPhase -= dt
            // Triangular pulse: 1 → 0 → 1 across the blink.
            val u = (blinkPhase / blinkDuration).coerceIn(0f, 1f)
            eyeOpen = abs(2f * u - 1f)
            if (blinkPhase <= 0f) eyeOpen = 1f
        } else {
            blinkCountdown -= dt
            if (blinkCountdown <= 0f) {
                blinkPhase = blinkDuration
                blinkCountdown = 2.6f + rng.nextFloat() * 3.4f
            }
        }
    }

    private fun writeRenderBuffer() {
        for (i in 0 until count) {
            val o = i shl 1
            renderBuffer[o] = px[i]
            renderBuffer[o + 1] = py[i]
        }
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    companion object {
        /** Side of the square the artwork is fitted into on a w×h canvas. */
        fun fitFor(width: Float, height: Float): Float =
            minOf(width * 0.94f, height * 0.68f)

        /**
         * Pick a mask resolution from the available space: we want roughly
         * [targetSpacing] pixels between neighbouring particles regardless of
         * screen density, clamped so a tablet doesn't ask for 40k particles.
         */
        fun maskSizeFor(fitPx: Float, targetSpacing: Float = 6.2f): Int =
            (fitPx / targetSpacing).toInt().coerceIn(90, 190)
    }
}
