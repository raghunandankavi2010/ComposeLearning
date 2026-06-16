package com.example.composelearning.arglasses.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.composelearning.arglasses.domain.model.FaceTransform
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * Renders the chosen pair of real spectacles ([image]) over the tracked face.
 *
 * Performance: [transformProvider] is invoked **inside the Canvas draw lambda**, so reading
 * the ViewModel's snapshot-state transform happens in the draw phase — each camera frame
 * invalidates drawing only, never recomposition, keeping the UI thread free.
 *
 * @param image             the spectacles artwork (front-facing, transparent background).
 * @param colorFilter       optional re-colour for the artwork (the selected style's tint).
 * @param transformProvider supplies the latest [FaceTransform] (in source-image space).
 * @param mirror            true for the front camera (matches the mirrored preview).
 */
@Composable
fun GlassesOverlay(
    image: ImageBitmap,
    colorFilter: ColorFilter?,
    transformProvider: () -> FaceTransform?,
    mirror: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val transform = transformProvider() ?: return@Canvas

        val mapper = FaceMeshCoordinateMapper(
            sourceWidth = transform.sourceWidth.toFloat(),
            sourceHeight = transform.sourceHeight.toFloat(),
            viewWidth = size.width,
            viewHeight = size.height,
            mirror = mirror,
        )

        val center = Offset(mapper.mapX(transform.anchorX), mapper.mapY(transform.anchorY))
        val frameWidth = transform.faceWidthPx * mapper.scaleFactor
        // Mirroring flips the visual sense of roll and yaw.
        val rollDegrees = Math.toDegrees(transform.rollRadians.toDouble()).toFloat()
            .let { if (mirror) -it else it }
        val yaw = if (mirror) -transform.yawRadians else transform.yawRadians

        drawSpectacles(image, colorFilter, center, frameWidth, rollDegrees, yaw)
    }
}

/**
 * Draws [image] centered on [center], rotated by [rollDegrees] about that point and scaled
 * so its width matches [frameWidth]. [yaw] applies a subtle horizontal foreshortening so the
 * frame narrows as the head turns. Aspect ratio is preserved from the source artwork.
 */
private fun DrawScope.drawSpectacles(
    image: ImageBitmap,
    colorFilter: ColorFilter?,
    center: Offset,
    frameWidth: Float,
    rollDegrees: Float,
    yaw: Float,
) {
    if (frameWidth <= 0f || image.width == 0) return

    // Foreshorten width with yaw (clamped so it never collapses); keep the artwork's aspect.
    val destWidth = frameWidth * cos(yaw).coerceIn(0.55f, 1f)
    val destHeight = destWidth * image.height / image.width
    val topLeft = Offset(center.x - destWidth / 2f, center.y - destHeight / 2f)

    withTransform({ rotate(rollDegrees, pivot = center) }) {
        drawImage(
            image = image,
            dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
            dstSize = IntSize(destWidth.roundToInt(), destHeight.roundToInt()),
            colorFilter = colorFilter,
        )
    }
}
