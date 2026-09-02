package com.example.composelearning.petparticles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap

/** The two pets the field can wear. */
enum class Pet(val label: String) { CAT("Cat"), DOG("Dog") }

/**
 * One entry of a pet's fixed palette. Every particle belongs to exactly one
 * tone, which decides its colour, its dot size and whether it gets a bloom pass
 * — so a tone is really "one `drawPoints` call".
 *
 * @param argb flat colour the artwork paints this region with.
 * @param sizeScale multiplier on the base dot size (whiskers thin, eyes fat).
 * @param glow draw an extra oversized, low-alpha pass underneath for bloom.
 * @param isEye dimmed while the pet blinks.
 */
data class FurTone(
    val argb: Int,
    val sizeScale: Float = 1f,
    val glow: Boolean = false,
    val isEye: Boolean = false
)

// ── Cat palette (index == particle draw order, back to front) ──────────────
private const val C_FUR = 0
private const val C_SHADOW = 1
private const val C_BELLY = 2
private const val C_PINK = 3
private const val C_WHISKER = 4
private const val C_EYE = 5
private const val C_PUPIL = 6

private val CAT_TONES = listOf(
    FurTone(0xFFE89A52.toInt()),                                  // ginger fur
    FurTone(0xFF9C5522.toInt()),                                  // stripes / shading
    FurTone(0xFFF6DFC0.toInt()),                                  // bib, muzzle, paws
    FurTone(0xFFF29AA2.toInt()),                                  // inner ear + nose
    FurTone(0xFFFFFFFF.toInt(), sizeScale = 0.62f),               // whiskers
    FurTone(0xFF7FE07A.toInt(), glow = true, isEye = true),       // iris
    FurTone(0xFF14181F.toInt(), sizeScale = 0.85f, isEye = true)  // slit pupil
)

// ── Dog palette ────────────────────────────────────────────────────────────
private const val D_FUR = 0
private const val D_SHADOW = 1
private const val D_BELLY = 2
private const val D_COLLAR = 3
private const val D_TAG = 4
private const val D_TONGUE = 5
private const val D_NOSE = 6
private const val D_EYE = 7
private const val D_GLINT = 8

private val DOG_TONES = listOf(
    FurTone(0xFFC08A4E.toInt()),                                  // tan fur
    FurTone(0xFF7E5027.toInt()),                                  // ears, hip patch
    FurTone(0xFFF3E2C4.toInt()),                                  // muzzle, bib, socks
    FurTone(0xFFD8453F.toInt()),                                  // collar
    FurTone(0xFFF2C14E.toInt(), glow = true),                     // name tag
    FurTone(0xFFEE7B8E.toInt()),                                  // tongue
    FurTone(0xFF241C16.toInt()),                                  // nose + mouth
    FurTone(0xFF2C3E56.toInt(), isEye = true),                    // eye
    FurTone(0xFFFFFFFF.toInt(), sizeScale = 0.9f, glow = true, isEye = true) // glint
)

/**
 * The pet "portraits". Everything is authored in a **unit square** (0..1, y
 * down) out of ordinary Canvas primitives — ovals, cubics, stroked curves — and
 * rasterised on demand into a small square bitmap. That bitmap is never shown:
 * [PetSampler] reads its pixels and throws it away, so it is only ever the
 * *source of truth for geometry and colour*.
 *
 * Consequence worth remembering: anything you can draw, you can particlise.
 * Swap these functions for a `PathParser` SVG, a decoded PNG or a text glyph
 * and the rest of the pipeline is unchanged.
 */
object PetArtwork {

    fun palette(pet: Pet): List<FurTone> = when (pet) {
        Pet.CAT -> CAT_TONES
        Pet.DOG -> DOG_TONES
    }

    /** Unit-space point the tail swings around, used for the idle wag. */
    fun tailPivot(pet: Pet): FloatArray = when (pet) {
        Pet.CAT -> floatArrayOf(0.66f, 0.88f)
        Pet.DOG -> floatArrayOf(0.74f, 0.89f)
    }

    /**
     * Rasterise [pet] into a [size]×[size] ARGB bitmap. When [tailOnly] is true
     * only the tail is drawn, in flat white — that second pass is the cheapest
     * way to *tag* which particles belong to the tail without needing a
     * dedicated palette entry that would look identical to the body fur.
     */
    fun rasterise(pet: Pet, size: Int, tailOnly: Boolean = false): Bitmap {
        val bmp = createBitmap(size, size)
        val canvas = Canvas(bmp)
        canvas.scale(size.toFloat(), size.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (pet) {
            Pet.CAT -> drawCat(canvas, paint, tailOnly)
            Pet.DOG -> drawDog(canvas, paint, tailOnly)
        }
        return bmp
    }
}

// ── Tiny drawing helpers (unit space) ──────────────────────────────────────

private val WHITE = 0xFFFFFFFF.toInt()

private fun Paint.fill(color: Int): Paint = apply {
    style = Paint.Style.FILL
    this.color = color
}

private fun Paint.stroke(color: Int, width: Float): Paint = apply {
    style = Paint.Style.STROKE
    this.color = color
    strokeWidth = width
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
}

private fun Canvas.ellipse(cx: Float, cy: Float, rx: Float, ry: Float, paint: Paint) =
    drawOval(cx - rx, cy - ry, cx + rx, cy + ry, paint)

private fun poly(vararg pts: Float): Path = Path().apply {
    moveTo(pts[0], pts[1])
    var i = 2
    while (i < pts.size) {
        lineTo(pts[i], pts[i + 1])
        i += 2
    }
    close()
}

private fun path(block: Path.() -> Unit): Path = Path().apply(block)

/**
 * Erases [shape] from the mask. Used when building the tail-only tag mask: the
 * tail root is painted over by the body in the real portrait, so those pixels
 * must not be tagged as tail — otherwise the wag would shear the flank.
 */
private fun Canvas.punchOut(shape: Path) {
    val eraser = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    drawPath(shape, eraser)
}

/** Mirror an x coordinate about the vertical centre line. */
private fun mx(x: Float) = 1f - x

// ── Cat ────────────────────────────────────────────────────────────────────

private fun drawCat(c: Canvas, p: Paint, tailOnly: Boolean) {
    val t = CAT_TONES

    val tail = path {
        moveTo(0.66f, 0.88f)
        cubicTo(0.90f, 0.94f, 0.97f, 0.74f, 0.88f, 0.56f)
    }
    // Sitting body: a symmetric teardrop, narrow at the shoulders.
    val body = path {
        moveTo(0.50f, 0.40f)
        cubicTo(0.28f, 0.44f, 0.20f, 0.66f, 0.19f, 0.86f)
        cubicTo(0.18f, 0.96f, 0.30f, 0.99f, 0.50f, 0.99f)
        cubicTo(0.70f, 0.99f, 0.82f, 0.96f, 0.81f, 0.86f)
        cubicTo(0.80f, 0.66f, 0.72f, 0.44f, 0.50f, 0.40f)
        close()
    }

    if (tailOnly) {
        c.drawPath(tail, p.stroke(WHITE, 0.085f))
        c.ellipse(0.88f, 0.56f, 0.048f, 0.048f, p.fill(WHITE))
        c.punchOut(body)
        return
    }

    // Tail first so the body overlaps its root.
    c.drawPath(tail, p.stroke(t[C_FUR].argb, 0.085f))
    c.ellipse(0.88f, 0.56f, 0.048f, 0.048f, p.fill(t[C_BELLY].argb))
    c.drawPath(body, p.fill(t[C_FUR].argb))

    // Tabby stripes: short dark strokes wrapping each flank.
    p.stroke(t[C_SHADOW].argb, 0.026f)
    for (k in 0..3) {
        val y = 0.58f + k * 0.085f
        val inset = 0.02f * k
        c.drawPath(path {
            moveTo(0.215f + inset, y)
            quadTo(0.30f + inset, y + 0.03f, 0.36f + inset, y + 0.005f)
        }, p)
        c.drawPath(path {
            moveTo(mx(0.215f + inset), y)
            quadTo(mx(0.30f + inset), y + 0.03f, mx(0.36f + inset), y + 0.005f)
        }, p)
    }

    // Chest bib and front paws.
    c.ellipse(0.50f, 0.815f, 0.150f, 0.155f, p.fill(t[C_BELLY].argb))
    c.ellipse(0.37f, 0.955f, 0.078f, 0.042f, p)
    c.ellipse(0.63f, 0.955f, 0.078f, 0.042f, p)
    p.stroke(t[C_SHADOW].argb, 0.010f)
    for (dx in floatArrayOf(-0.026f, 0f, 0.026f)) {
        c.drawLine(0.37f + dx, 0.935f, 0.37f + dx, 0.968f, p)
        c.drawLine(0.63f + dx, 0.935f, 0.63f + dx, 0.968f, p)
    }

    // Ears behind the head so the head silhouette stays clean.
    p.fill(t[C_FUR].argb)
    c.drawPath(poly(0.30f, 0.31f, 0.255f, 0.075f, 0.475f, 0.19f), p)
    c.drawPath(poly(mx(0.30f), 0.31f, mx(0.255f), 0.075f, mx(0.475f), 0.19f), p)
    p.fill(t[C_PINK].argb)
    c.drawPath(poly(0.315f, 0.275f, 0.288f, 0.125f, 0.435f, 0.205f), p)
    c.drawPath(poly(mx(0.315f), 0.275f, mx(0.288f), 0.125f, mx(0.435f), 0.205f), p)

    // Head.
    c.ellipse(0.50f, 0.285f, 0.225f, 0.198f, p.fill(t[C_FUR].argb))

    // Forehead "M" — the classic tabby marking, and a strong cat cue.
    p.stroke(t[C_SHADOW].argb, 0.020f)
    c.drawLine(0.445f, 0.125f, 0.435f, 0.185f, p)
    c.drawLine(0.500f, 0.115f, 0.500f, 0.180f, p)
    c.drawLine(0.555f, 0.125f, 0.565f, 0.185f, p)

    // Muzzle, nose, mouth.
    c.ellipse(0.50f, 0.358f, 0.118f, 0.070f, p.fill(t[C_BELLY].argb))
    c.drawPath(poly(0.468f, 0.322f, 0.532f, 0.322f, 0.50f, 0.360f), p.fill(t[C_PINK].argb))
    p.stroke(t[C_SHADOW].argb, 0.011f)
    c.drawPath(path {
        moveTo(0.50f, 0.360f)
        lineTo(0.50f, 0.378f)
        quadTo(0.50f, 0.402f, 0.462f, 0.398f)
    }, p)
    c.drawPath(path {
        moveTo(0.50f, 0.378f)
        quadTo(0.50f, 0.402f, 0.538f, 0.398f)
    }, p)

    // Whiskers — thin strokes give single-file rows of dots, which reads great.
    p.stroke(t[C_WHISKER].argb, 0.0085f)
    val whiskers = arrayOf(
        floatArrayOf(0.405f, 0.340f, 0.26f, 0.300f, 0.135f, 0.288f),
        floatArrayOf(0.405f, 0.358f, 0.26f, 0.352f, 0.118f, 0.360f),
        floatArrayOf(0.405f, 0.374f, 0.26f, 0.398f, 0.140f, 0.430f)
    )
    for (w in whiskers) {
        c.drawPath(path {
            moveTo(w[0], w[1]); quadTo(w[2], w[3], w[4], w[5])
        }, p)
        c.drawPath(path {
            moveTo(mx(w[0]), w[1]); quadTo(mx(w[2]), w[3], mx(w[4]), w[5])
        }, p)
    }

    // Eyes: almond iris + vertical slit pupil. Kept clear of the muzzle so the
    // green does not sit on top of the cream fur.
    p.fill(t[C_EYE].argb)
    c.ellipse(0.408f, 0.250f, 0.055f, 0.061f, p)
    c.ellipse(mx(0.408f), 0.250f, 0.055f, 0.061f, p)
    p.fill(t[C_PUPIL].argb)
    c.ellipse(0.408f, 0.250f, 0.017f, 0.049f, p)
    c.ellipse(mx(0.408f), 0.250f, 0.017f, 0.049f, p)
}

// ── Dog ────────────────────────────────────────────────────────────────────

private fun drawDog(c: Canvas, p: Paint, tailOnly: Boolean) {
    val t = DOG_TONES

    val tail = path {
        moveTo(0.74f, 0.89f)
        cubicTo(0.92f, 0.88f, 0.965f, 0.70f, 0.875f, 0.58f)
    }
    // Body — wider and lower than the cat's, so the two silhouettes read apart.
    val body = path {
        moveTo(0.50f, 0.42f)
        cubicTo(0.26f, 0.46f, 0.17f, 0.68f, 0.16f, 0.87f)
        cubicTo(0.15f, 0.97f, 0.30f, 0.995f, 0.50f, 0.995f)
        cubicTo(0.70f, 0.995f, 0.85f, 0.97f, 0.84f, 0.87f)
        cubicTo(0.83f, 0.68f, 0.74f, 0.46f, 0.50f, 0.42f)
        close()
    }

    if (tailOnly) {
        c.drawPath(tail, p.stroke(WHITE, 0.078f))
        c.ellipse(0.875f, 0.58f, 0.046f, 0.046f, p.fill(WHITE))
        c.punchOut(body)
        return
    }

    c.drawPath(tail, p.stroke(t[D_FUR].argb, 0.078f))
    c.ellipse(0.875f, 0.58f, 0.046f, 0.046f, p.fill(t[D_BELLY].argb))
    c.drawPath(body, p.fill(t[D_FUR].argb))

    // Hip patch — an off-centre blob is what makes a dog look like a dog.
    c.ellipse(0.275f, 0.735f, 0.098f, 0.115f, p.fill(t[D_SHADOW].argb))

    // Bib, front legs, socks.
    p.fill(t[D_BELLY].argb)
    c.ellipse(0.50f, 0.775f, 0.150f, 0.170f, p)
    c.drawRoundRect(0.352f, 0.795f, 0.442f, 0.985f, 0.045f, 0.045f, p)
    c.drawRoundRect(mx(0.442f), 0.795f, mx(0.352f), 0.985f, 0.045f, 0.045f, p)
    c.ellipse(0.397f, 0.965f, 0.062f, 0.038f, p)
    c.ellipse(mx(0.397f), 0.965f, 0.062f, 0.038f, p)
    p.stroke(t[D_SHADOW].argb, 0.010f)
    for (dx in floatArrayOf(-0.021f, 0.021f)) {
        c.drawLine(0.397f + dx, 0.945f, 0.397f + dx, 0.978f, p)
        c.drawLine(mx(0.397f) + dx, 0.945f, mx(0.397f) + dx, 0.978f, p)
    }

    // Floppy ears, drawn before the head so they tuck behind it.
    p.fill(t[D_SHADOW].argb)
    c.drawPath(path {
        moveTo(0.345f, 0.135f)
        cubicTo(0.165f, 0.155f, 0.125f, 0.420f, 0.225f, 0.505f)
        cubicTo(0.300f, 0.450f, 0.335f, 0.300f, 0.345f, 0.135f)
        close()
    }, p)
    c.drawPath(path {
        moveTo(mx(0.345f), 0.135f)
        cubicTo(mx(0.165f), 0.155f, mx(0.125f), 0.420f, mx(0.225f), 0.505f)
        cubicTo(mx(0.300f), 0.450f, mx(0.335f), 0.300f, mx(0.345f), 0.135f)
        close()
    }, p)

    // Head.
    c.ellipse(0.50f, 0.268f, 0.215f, 0.200f, p.fill(t[D_FUR].argb))

    // Eyebrow dots — cheap, and they give the dog an expression.
    p.fill(t[D_BELLY].argb)
    c.ellipse(0.418f, 0.170f, 0.036f, 0.019f, p)
    c.ellipse(mx(0.418f), 0.170f, 0.036f, 0.019f, p)

    // Muzzle, tongue, mouth, nose.
    c.ellipse(0.50f, 0.358f, 0.132f, 0.096f, p.fill(t[D_BELLY].argb))
    c.ellipse(0.50f, 0.415f, 0.038f, 0.030f, p.fill(t[D_TONGUE].argb))
    p.stroke(t[D_NOSE].argb, 0.013f)
    c.drawPath(path {
        moveTo(0.50f, 0.345f)
        lineTo(0.50f, 0.372f)
        quadTo(0.50f, 0.400f, 0.448f, 0.392f)
    }, p)
    c.drawPath(path {
        moveTo(0.50f, 0.372f)
        quadTo(0.50f, 0.400f, 0.552f, 0.392f)
    }, p)
    c.ellipse(0.50f, 0.318f, 0.047f, 0.034f, p.fill(t[D_NOSE].argb))

    // Eyes + specular glint.
    p.fill(t[D_EYE].argb)
    c.ellipse(0.412f, 0.232f, 0.047f, 0.050f, p)
    c.ellipse(mx(0.412f), 0.232f, 0.047f, 0.050f, p)
    // Both glints sit up-left of their pupil: one light source, two eyes.
    p.fill(t[D_GLINT].argb)
    c.ellipse(0.397f, 0.217f, 0.016f, 0.016f, p)
    c.ellipse(mx(0.427f), 0.217f, 0.016f, 0.016f, p)

    // Collar across the neck, plus a swinging tag.
    c.drawPath(path {
        moveTo(0.305f, 0.458f)
        quadTo(0.50f, 0.526f, 0.695f, 0.458f)
    }, p.stroke(t[D_COLLAR].argb, 0.048f))
    c.ellipse(0.50f, 0.545f, 0.036f, 0.036f, p.fill(t[D_TAG].argb))
}
