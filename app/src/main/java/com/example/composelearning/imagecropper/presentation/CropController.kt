package com.example.composelearning.imagecropper.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Which part of the editor a single-finger drag manipulates. */
sealed interface DragMode {
    data class Resize(val corner: Corner) : DragMode
    data object MoveFrame : DragMode
    data object TransformImage : DragMode
}

enum class Corner { TopLeft, TopRight, BottomLeft, BottomRight }

/**
 * Owns the *live* editor geometry — the image transform (zoom + pan) and the crop frame —
 * as Compose snapshot state, plus all the coordinate math that ties them to the source
 * bitmap.
 *
 * ### Why a `@Stable` holder instead of ViewModel state
 * These values change every animation frame while a finger is down. Reading them inside a
 * `graphicsLayer { }` block and inside a `Canvas { }` draw lambda means a gesture only
 * invalidates the **draw** phase — never recomposition. Hoisting them into the ViewModel's
 * `StateFlow` would instead recompose the whole screen 60×/second.
 *
 * ### Invariant
 * The crop frame is always fully contained by the displayed image. Frame edits clamp the
 * rect into the image rect; transform edits clamp the pan (and floor the zoom at
 * [minZoom]) so the image keeps covering the frame. Therefore [sourceCropRect] always maps
 * to a fully-valid region of real pixels.
 */
@Stable
class CropController {

    var containerSize by mutableStateOf(IntSize.Zero)
        private set
    var imageSize by mutableStateOf(IntSize.Zero)
        private set

    var zoom by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var cropRect by mutableStateOf(Rect.Zero)
        private set

    /** Touch tolerances, configured by the screen from the current [androidx.compose.ui.unit.Density]. */
    var minFramePx: Float = 160f
    var handleRadiusPx: Float = 64f

    private val maxZoom = 8f

    // region — bounds / lifecycle ------------------------------------------------------

    fun onContainerResized(size: IntSize) {
        if (size != containerSize) {
            containerSize = size
            initFrameIfReady()
        }
    }

    fun onImageChanged(size: IntSize) {
        if (size != imageSize) {
            imageSize = size
            initFrameIfReady()
        }
    }

    private fun initFrameIfReady() {
        if (containerSize == IntSize.Zero || imageSize == IntSize.Zero) return
        reset()
    }

    /** Recenters the image at fit-scale and places a centered crop frame at 80% of it. */
    fun reset() {
        val fitted = fittedSize()
        if (fitted == Size.Zero) return
        zoom = 1f
        offset = Offset.Zero
        val w = fitted.width * 0.8f
        val h = fitted.height * 0.8f
        val left = (containerSize.width - w) / 2f
        val top = (containerSize.height - h) / 2f
        cropRect = Rect(left, top, left + w, top + h)
    }

    // endregion

    // region — derived geometry --------------------------------------------------------

    /** The bitmap fitted into the container (`ContentScale.Fit`) at zoom = 1. */
    private fun fittedSize(): Size {
        val c = containerSize
        val i = imageSize
        if (c.width == 0 || c.height == 0 || i.width == 0 || i.height == 0) return Size.Zero
        val scale = min(c.width.toFloat() / i.width, c.height.toFloat() / i.height)
        return Size(i.width * scale, i.height * scale)
    }

    /** The image's on-screen rectangle for the current [zoom] / [offset]. */
    fun imageRect(): Rect {
        val fitted = fittedSize()
        if (fitted == Size.Zero) return Rect.Zero
        val dispW = fitted.width * zoom
        val dispH = fitted.height * zoom
        val left = (containerSize.width - dispW) / 2f + offset.x
        val top = (containerSize.height - dispH) / 2f + offset.y
        return Rect(left, top, left + dispW, top + dispH)
    }

    /** Smallest zoom at which the image still fully covers the crop frame (never below fit). */
    private fun minZoom(): Float {
        val fitted = fittedSize()
        if (fitted.width == 0f || fitted.height == 0f) return 1f
        val byWidth = cropRect.width / fitted.width
        val byHeight = cropRect.height / fitted.height
        return max(1f, max(byWidth, byHeight)).coerceAtMost(maxZoom)
    }

    // endregion

    // region — gesture handlers --------------------------------------------------------

    /** Pinch-zoom + two-finger pan, zooming about [centroid]. */
    fun transform(centroid: Offset, pan: Offset, zoomChange: Float) {
        val old = imageRect()
        if (old.width <= 0f || old.height <= 0f) return

        // Fraction of the centroid within the current image rect — held constant across zoom.
        val fx = (centroid.x - old.left) / old.width
        val fy = (centroid.y - old.top) / old.height

        val newZoom = (zoom * zoomChange).coerceIn(minZoom(), maxZoom)
        val fitted = fittedSize()
        val dispW = fitted.width * newZoom
        val dispH = fitted.height * newZoom
        val baseLeft = (containerSize.width - dispW) / 2f
        val baseTop = (containerSize.height - dispH) / 2f

        val wantedLeft = (centroid.x + pan.x) - fx * dispW
        val wantedTop = (centroid.y + pan.y) - fy * dispH

        zoom = newZoom
        offset = clampOffset(
            candidate = Offset(wantedLeft - baseLeft, wantedTop - baseTop),
            dispW = dispW,
            dispH = dispH,
            baseLeft = baseLeft,
            baseTop = baseTop,
        )
    }

    /** Single-finger pan of the image behind a fixed crop frame. */
    fun panImage(drag: Offset) {
        val rect = imageRect()
        if (rect.width <= 0f) return
        val baseLeft = (containerSize.width - rect.width) / 2f
        val baseTop = (containerSize.height - rect.height) / 2f
        offset = clampOffset(offset + drag, rect.width, rect.height, baseLeft, baseTop)
    }

    /** Translate the crop frame, clamped to stay within the image. */
    fun moveFrame(drag: Offset) {
        val img = imageRect()
        val width = cropRect.width
        val height = cropRect.height
        val left = (cropRect.left + drag.x).coerceIn(img.left, img.right - width)
        val top = (cropRect.top + drag.y).coerceIn(img.top, img.bottom - height)
        cropRect = Rect(left, top, left + width, top + height)
    }

    /** Drag one [corner], honouring [minFramePx] and the image bounds. */
    fun resizeFrame(corner: Corner, drag: Offset) {
        val img = imageRect()
        var left = cropRect.left
        var top = cropRect.top
        var right = cropRect.right
        var bottom = cropRect.bottom

        when (corner) {
            Corner.TopLeft -> { left += drag.x; top += drag.y }
            Corner.TopRight -> { right += drag.x; top += drag.y }
            Corner.BottomLeft -> { left += drag.x; bottom += drag.y }
            Corner.BottomRight -> { right += drag.x; bottom += drag.y }
        }

        left = left.coerceIn(img.left, right - minFramePx)
        right = right.coerceIn(left + minFramePx, img.right)
        top = top.coerceIn(img.top, bottom - minFramePx)
        bottom = bottom.coerceIn(top + minFramePx, img.bottom)

        cropRect = Rect(left, top, right, bottom)
        // Shrinking the image is now disallowed below the frame: re-floor the zoom.
        if (zoom < minZoom()) zoom = minZoom()
    }

    /** Classifies where a press landed so the gesture loop can route the drag. */
    fun hitTest(position: Offset): DragMode {
        val corners = listOf(
            Corner.TopLeft to Offset(cropRect.left, cropRect.top),
            Corner.TopRight to Offset(cropRect.right, cropRect.top),
            Corner.BottomLeft to Offset(cropRect.left, cropRect.bottom),
            Corner.BottomRight to Offset(cropRect.right, cropRect.bottom),
        )
        corners.firstOrNull { (_, point) -> (position - point).getDistance() <= handleRadiusPx }
            ?.let { (corner, _) -> return DragMode.Resize(corner) }
        return if (cropRect.contains(position)) DragMode.MoveFrame else DragMode.TransformImage
    }

    // endregion

    /**
     * Maps the on-screen crop frame back into source-bitmap pixel coordinates. Returns
     * `null` until the editor is laid out. The result is always within `[0, imageSize]`.
     */
    fun sourceCropRect(): IntRect? {
        val img = imageRect()
        if (img.width <= 0f || img.height <= 0f) return null
        val scaleX = imageSize.width / img.width
        val scaleY = imageSize.height / img.height

        val left = ((cropRect.left - img.left) * scaleX).roundToInt().coerceIn(0, imageSize.width)
        val top = ((cropRect.top - img.top) * scaleY).roundToInt().coerceIn(0, imageSize.height)
        val right = ((cropRect.right - img.left) * scaleX).roundToInt().coerceIn(0, imageSize.width)
        val bottom = ((cropRect.bottom - img.top) * scaleY).roundToInt().coerceIn(0, imageSize.height)

        if (right <= left || bottom <= top) return null
        return IntRect(left, top, right, bottom)
    }

    private fun clampOffset(
        candidate: Offset,
        dispW: Float,
        dispH: Float,
        baseLeft: Float,
        baseTop: Float,
    ): Offset {
        // imageLeft = baseLeft + offset.x must keep the crop frame covered:
        //   imageLeft <= cropRect.left  AND  imageLeft + dispW >= cropRect.right
        val minX = cropRect.right - dispW - baseLeft
        val maxX = cropRect.left - baseLeft
        val minY = cropRect.bottom - dispH - baseTop
        val maxY = cropRect.top - baseTop
        return Offset(
            x = candidate.x.coerceIn(min(minX, maxX), max(minX, maxX)),
            y = candidate.y.coerceIn(min(minY, maxY), max(minY, maxY)),
        )
    }
}
