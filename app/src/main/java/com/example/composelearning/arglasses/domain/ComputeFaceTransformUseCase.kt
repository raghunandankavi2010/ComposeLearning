package com.example.composelearning.arglasses.domain

import com.example.composelearning.arglasses.domain.model.FaceAnchors
import com.example.composelearning.arglasses.domain.model.FaceTransform
import com.example.composelearning.arglasses.domain.model.Vec3
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Translates raw Face Mesh contours into a [FaceTransform] (scale, roll/yaw/pitch, and a
 * center anchor) for placing the glasses.
 *
 * The anchoring follows strict, non-guessed rules:
 *  - **Center bridge anchor** — midpoint of the two eye-center centroids (the point that
 *    sits on the nose bridge). `NOSE_BRIDGE` is used to bias the vertical position so the
 *    frame rests on the bridge rather than across the pupils.
 *  - **Width / scale** — the horizontal span of the `FACE_OVAL` (leftmost→rightmost,
 *    i.e. temple-to-temple near the ears), which is what a real frame spans.
 *  - **Roll** — `atan2` of the slope between the left and right eye centers.
 *
 * Pure function: deterministic, allocation-light, no framework types — safe to call on the
 * ML Kit callback thread and to unit-test directly.
 */
class ComputeFaceTransformUseCase {

    operator fun invoke(anchors: FaceAnchors): FaceTransform? {
        // A face is only usable if every contour we anchor against is present.
        if (anchors.leftEye.isEmpty() ||
            anchors.rightEye.isEmpty() ||
            anchors.faceOval.isEmpty()
        ) {
            return null
        }

        val leftEye = anchors.leftEye.centroid()
        val rightEye = anchors.rightEye.centroid()

        // ── Center bridge anchor ───────────────────────────────────────────────────
        val eyeMidX = (leftEye.x + rightEye.x) / 2f
        val eyeMidY = (leftEye.y + rightEye.y) / 2f
        // Pull the anchor down toward the nose bridge so lenses sit over the eyes, not above.
        val anchorY = anchors.noseBridge.firstOrNull()
            ?.let { bridgeTop -> (eyeMidY + bridgeTop.y) / 2f }
            ?: eyeMidY

        // ── Width & scale (FACE_OVAL temple-to-temple) ─────────────────────────────
        val ovalLeft = anchors.faceOval.minOf { it.x }
        val ovalRight = anchors.faceOval.maxOf { it.x }
        val faceWidth = (ovalRight - ovalLeft).coerceAtLeast(1f)

        // ── Roll (Z-axis) from the inter-eye slope ─────────────────────────────────
        val roll = atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x)

        // ── Coarse yaw / pitch (depth-based estimates; used only as subtle hints) ──
        val interocular = hypot(rightEye.x - leftEye.x, rightEye.y - leftEye.y)
            .coerceAtLeast(1f)
        // Eye nearer the camera (smaller z) means the head is turned toward the other side.
        val yaw = atan2(rightEye.z - leftEye.z, interocular)
        val pitch = anchors.noseBridge.pitchFromBridge(interocular)

        return FaceTransform(
            anchorX = eyeMidX,
            anchorY = anchorY,
            faceWidthPx = faceWidth,
            rollRadians = roll,
            yawRadians = yaw,
            pitchRadians = pitch,
            sourceWidth = anchors.sourceWidth,
            sourceHeight = anchors.sourceHeight,
        )
    }

    private fun List<Vec3>.centroid(): Vec3 {
        var sx = 0f
        var sy = 0f
        var sz = 0f
        for (p in this) {
            sx += p.x; sy += p.y; sz += p.z
        }
        val n = size.toFloat()
        return Vec3(sx / n, sy / n, sz / n)
    }

    /** Depth slope along the nose bridge → coarse pitch. Returns 0 if too few points. */
    private fun List<Vec3>.pitchFromBridge(interocular: Float): Float {
        if (size < 2) return 0f
        val top = first()
        val bottom = last()
        val vertical = hypot(bottom.x - top.x, bottom.y - top.y).coerceAtLeast(1f)
        // Normalise depth delta by the bridge length, scaled to interocular for stability.
        return atan2((bottom.z - top.z) / vertical * interocular, interocular)
    }
}
