package com.example.composelearning.arglasses.presentation

import kotlin.math.max

/**
 * Converts coordinates from the **source-image** space (the upright camera frame ML Kit
 * measured against) into the **on-screen viewport** space.
 *
 * It reproduces `PreviewView`'s `FILL_CENTER` behaviour — a center-crop that scales by the
 * larger of the two axis ratios so the preview fills the view — and applies front-camera
 * **horizontal mirroring** so the overlay lands on the same pixels the (already mirrored)
 * selfie preview shows.
 *
 * Pure value type: constructed per draw from the current sizes, no allocation beyond itself.
 */
class FaceMeshCoordinateMapper(
    sourceWidth: Float,
    sourceHeight: Float,
    private val viewWidth: Float,
    viewHeight: Float,
    private val mirror: Boolean,
) {
    /** Uniform fill-center scale factor (same on both axes). */
    val scaleFactor: Float = max(viewWidth / sourceWidth, viewHeight / sourceHeight)

    private val offsetX = (viewWidth - sourceWidth * scaleFactor) / 2f
    private val offsetY = (viewHeight - sourceHeight * scaleFactor) / 2f

    fun mapX(x: Float): Float {
        val viewX = x * scaleFactor + offsetX
        return if (mirror) viewWidth - viewX else viewX
    }

    fun mapY(y: Float): Float = y * scaleFactor + offsetY
}
