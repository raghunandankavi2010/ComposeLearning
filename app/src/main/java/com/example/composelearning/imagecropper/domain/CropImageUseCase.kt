package com.example.composelearning.imagecropper.domain

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.ui.unit.IntRect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Produces the final, real cropped bitmap.
 *
 * Two stages:
 *  1. **Region crop** — copy the pixels inside [region] (already expressed in *source*
 *     bitmap coordinates) into a tight bitmap. This is the lossless rectangle result.
 *  2. **Shape mask** — for [CropShape.Circle] / [CropShape.Star], paint the region crop
 *     through a [BitmapShader] clipped to an anti-aliased path, yielding clean edges with
 *     transparency outside the shape.
 *
 * All work runs on [dispatcher] (CPU-bound, default) so the main thread never decodes or
 * allocates large bitmaps. The function is side-effect free apart from allocating the
 * returned bitmap; the caller owns recycling it.
 */
class CropImageUseCase(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    suspend operator fun invoke(
        source: Bitmap,
        region: IntRect,
        shape: CropShape,
    ): Bitmap = withContext(dispatcher) {
        // Clamp the requested region into the source bounds defensively — the geometry
        // layer should already keep it valid, but a 1px rounding overshoot must never crash.
        val left = region.left.coerceIn(0, source.width - 1)
        val top = region.top.coerceIn(0, source.height - 1)
        val width = region.width.coerceIn(1, source.width - left)
        val height = region.height.coerceIn(1, source.height - top)

        val rectangleCrop = Bitmap.createBitmap(source, left, top, width, height)

        when (shape) {
            CropShape.Rectangle -> rectangleCrop
            CropShape.Circle -> rectangleCrop.maskedBy { canvas, paint ->
                val diameter = min(width, height).toFloat()
                canvas.drawCircle(width / 2f, height / 2f, diameter / 2f, paint)
            }.also { rectangleCrop.recycle() }

            CropShape.Star -> rectangleCrop.maskedBy { canvas, paint ->
                canvas.drawPath(starPath(width.toFloat(), height.toFloat()), paint)
            }.also { rectangleCrop.recycle() }
        }
    }

    /**
     * Returns a new transparent `ARGB_8888` bitmap of the same size, into which [this] is
     * painted only where [draw] rasterises a shape. Using a [BitmapShader] (rather than
     * `clipPath`) keeps the mask edges anti-aliased.
     */
    private inline fun Bitmap.maskedBy(draw: (Canvas, Paint) -> Unit): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(this@maskedBy, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        draw(canvas, paint)
        return output
    }

    private fun starPath(width: Float, height: Float, points: Int = 5, innerRatio: Float = 0.45f): Path {
        val cx = width / 2f
        val cy = height / 2f
        val outer = min(width, height) / 2f
        val inner = outer * innerRatio
        val step = Math.PI / points
        var angle = -Math.PI / 2 // first point straight up
        return Path().apply {
            moveTo(cx + (outer * cos(angle)).toFloat(), cy + (outer * sin(angle)).toFloat())
            for (i in 1 until points * 2) {
                angle += step
                val radius = if (i % 2 == 0) outer else inner
                lineTo(cx + (radius * cos(angle)).toFloat(), cy + (radius * sin(angle)).toFloat())
            }
            close()
        }
    }
}
