package com.example.composelearning.fastimages

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.abs

/**
 * A blurred preview of the photo that ships **inside the catalog JSON**.
 *
 * This is the placeholder rung above a flat dominant colour. The ladder, by
 * payload cost:
 *
 * | technique                      | bytes in JSON | extra request |
 * |--------------------------------|---------------|---------------|
 * | dominant colour                | ~4            | no            |
 * | **this / BlurHash / ThumbHash**| ~28-40        | no            |
 * | inline base64 micro-JPEG       | ~300-500      | no            |
 * | separate LQIP thumbnail URL    | ~1-2 KB       | **yes**       |
 *
 * The "no extra request" column is the whole argument. On a slow link a second
 * request per tile doubles the number of round trips before the user sees
 * anything, and round trips — not bytes — are what hurt on a high-latency
 * cellular connection. Anything that rides along in a response you were already
 * making is close to free.
 *
 * ### Relationship to BlurHash / ThumbHash
 *
 * Same idea, simpler maths. BlurHash stores a handful of 2D DCT coefficients
 * and packs them in base83; ThumbHash does something similar with a smarter
 * basis and better handling of aspect ratio and alpha. Both reconstruct a
 * smooth image by evaluating basis functions per output pixel.
 *
 * This implementation stores a literal [GRID_WIDTH] x [GRID_HEIGHT] grid of
 * RGB444 cells and lets the GPU's bilinear filter do the smoothing when the
 * tiny bitmap is scaled up. Visually it lands in much the same place for a
 * thumbnail, and it keeps the sample free of an extra dependency *and* free of
 * a hand-rolled DCT that could be subtly wrong. In production, use the real
 * BlurHash or ThumbHash library and run the encoder server-side at upload time.
 *
 * ### Honest caveat about this demo
 *
 * A real catalog computes the hash from the real photo. Picsum cannot tell us
 * anything about the image it is about to serve, so [synthesise] fabricates a
 * plausible hash from the dish's accent colour. The *pipeline* is real; the
 * resemblance between placeholder and photo is not.
 */
object MicroThumb {

    const val GRID_WIDTH = 4
    const val GRID_HEIGHT = 3

    private const val CELLS = GRID_WIDTH * GRID_HEIGHT

    /** 3 hex chars (RGB444) per cell — 36 chars for the whole grid. */
    private const val CHARS_PER_CELL = 3

    fun encode(cells: IntArray): String {
        require(cells.size == CELLS) { "expected $CELLS cells, got ${cells.size}" }
        return buildString(CELLS * CHARS_PER_CELL) {
            cells.forEach { color ->
                append(hexDigit(Color.red(color)))
                append(hexDigit(Color.green(color)))
                append(hexDigit(Color.blue(color)))
            }
        }
    }

    /**
     * Decodes to a [GRID_WIDTH] x [GRID_HEIGHT] bitmap — 12 pixels, 48 bytes.
     *
     * Drawn at tile size with bilinear filtering, that reads as a soft blur.
     * Deliberately *not* upscaled here: making the CPU produce a 320x240
     * gradient per tile would be the most expensive way to draw a blur.
     */
    fun decode(hash: String): ImageBitmap? {
        if (hash.length != CELLS * CHARS_PER_CELL) return null
        val pixels = IntArray(CELLS)
        for (index in 0 until CELLS) {
            val offset = index * CHARS_PER_CELL
            val r = hexValue(hash[offset]) ?: return null
            val g = hexValue(hash[offset + 1]) ?: return null
            val b = hexValue(hash[offset + 2]) ?: return null
            pixels[index] = Color.rgb(r * 17, g * 17, b * 17)
        }
        val bitmap = Bitmap.createBitmap(GRID_WIDTH, GRID_HEIGHT, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, GRID_WIDTH, 0, 0, GRID_WIDTH, GRID_HEIGHT)
        return bitmap.asImageBitmap()
    }

    /**
     * Stands in for the server-side encoder: derives a deterministic, gently
     * varying grid around [accentArgb] so the placeholder looks like a blurred
     * photograph rather than a flat rectangle.
     */
    fun synthesise(seed: String, accentArgb: Long): String {
        val base = accentArgb.toInt()
        var noise = seed.hashCode()
        val cells = IntArray(CELLS) { index ->
            noise = noise * 1_664_525 + 1_013_904_223
            val jitter = (abs(noise shr 16) % 96) - 40
            // Strong top-to-bottom luminance ramp. A real BlurHash of a food
            // photo is dominated by exactly this kind of low-frequency
            // structure, and without it the reconstruction reads as a flat
            // rectangle rather than a blurred image.
            val lift = 44 - (index / GRID_WIDTH) * 44
            Color.rgb(
                (Color.red(base) + jitter + lift).coerceIn(0, 255),
                (Color.green(base) + jitter + lift).coerceIn(0, 255),
                (Color.blue(base) + jitter + lift).coerceIn(0, 255)
            )
        }
        return encode(cells)
    }

    private fun hexDigit(channel: Int): Char {
        val value = (channel * 15 + 127) / 255
        return "0123456789abcdef"[value.coerceIn(0, 15)]
    }

    private fun hexValue(char: Char): Int? = when (char) {
        in '0'..'9' -> char - '0'
        in 'a'..'f' -> char - 'a' + 10
        in 'A'..'F' -> char - 'A' + 10
        else -> null
    }
}
