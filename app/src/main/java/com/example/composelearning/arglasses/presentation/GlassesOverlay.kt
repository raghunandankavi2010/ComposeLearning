package com.example.composelearning.arglasses.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.composelearning.arglasses.domain.model.FaceTransform
import kotlin.math.cos

/**
 * Renders the virtual spectacles over the tracked face.
 *
 * Performance: [transformProvider] is invoked **inside the Canvas draw lambda**, so reading
 * the ViewModel's snapshot-state transform happens in the draw phase — each camera frame
 * invalidates drawing only, never recomposition, keeping the UI thread free.
 *
 * @param transformProvider supplies the latest [FaceTransform] (in source-image space).
 * @param mirror            true for the front camera (matches the mirrored preview).
 */
@Composable
fun GlassesOverlay(
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

        drawSpectacles(center, frameWidth, rollDegrees, yaw)
    }
}

/**
 * Draws a stylised pair of glasses centered on [center], rotated by [rollDegrees] about
 * that point, scaled to [frameWidth]. [yaw] applies a subtle horizontal foreshortening so
 * the frame narrows as the head turns.
 */
private fun DrawScope.drawSpectacles(
    center: Offset,
    frameWidth: Float,
    rollDegrees: Float,
    yaw: Float,
) {
    if (frameWidth <= 0f) return

    val frameColor = Color(0xFF1A1A1A)
    val lensTint = Color(0x331E88E5)
    val highlight = Color(0xFF4FC3F7)

    // Foreshorten width with yaw (clamped so it never collapses).
    val effectiveWidth = frameWidth * cos(yaw).coerceIn(0.55f, 1f)
    val lensHeight = effectiveWidth * 0.32f
    val lensWidth = effectiveWidth * 0.40f
    val stroke = (effectiveWidth * 0.035f).coerceAtLeast(2f)
    val corner = CornerRadius(lensHeight * 0.45f, lensHeight * 0.45f)

    val leftLensCenterX = center.x - effectiveWidth / 2f + lensWidth / 2f
    val rightLensCenterX = center.x + effectiveWidth / 2f - lensWidth / 2f

    withTransform({ rotate(rollDegrees, pivot = center) }) {
        listOf(leftLensCenterX, rightLensCenterX).forEach { lensCenterX ->
            val topLeft = Offset(lensCenterX - lensWidth / 2f, center.y - lensHeight / 2f)
            val lensSize = Size(lensWidth, lensHeight)
            // Tinted lens fill + frame outline.
            drawRoundRect(color = lensTint, topLeft = topLeft, size = lensSize, cornerRadius = corner)
            drawRoundRect(
                color = frameColor,
                topLeft = topLeft,
                size = lensSize,
                cornerRadius = corner,
                style = Stroke(width = stroke),
            )
            // Subtle glare line across the upper-left of each lens.
            drawLine(
                color = highlight.copy(alpha = 0.5f),
                start = Offset(topLeft.x + lensWidth * 0.18f, topLeft.y + lensHeight * 0.30f),
                end = Offset(topLeft.x + lensWidth * 0.42f, topLeft.y + lensHeight * 0.18f),
                strokeWidth = stroke * 0.6f,
            )
        }

        // Bridge between the inner lens edges.
        drawLine(
            color = frameColor,
            start = Offset(leftLensCenterX + lensWidth / 2f, center.y - lensHeight * 0.15f),
            end = Offset(rightLensCenterX - lensWidth / 2f, center.y - lensHeight * 0.15f),
            strokeWidth = stroke,
        )

        // Temple arms reaching from the outer lens edges toward the ears.
        val armLength = effectiveWidth * 0.16f
        drawLine(
            color = frameColor,
            start = Offset(leftLensCenterX - lensWidth / 2f, center.y - lensHeight * 0.2f),
            end = Offset(leftLensCenterX - lensWidth / 2f - armLength, center.y - lensHeight * 0.35f),
            strokeWidth = stroke,
        )
        drawLine(
            color = frameColor,
            start = Offset(rightLensCenterX + lensWidth / 2f, center.y - lensHeight * 0.2f),
            end = Offset(rightLensCenterX + lensWidth / 2f + armLength, center.y - lensHeight * 0.35f),
            strokeWidth = stroke,
        )
    }
}
