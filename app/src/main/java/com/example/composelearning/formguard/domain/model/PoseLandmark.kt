package com.example.composelearning.formguard.domain.model

/**
 * A single body landmark in **normalized** image space: [x] and [y] are in `[0, 1]` relative to
 * the upright (rotation-corrected) camera frame, top-left origin. [visibility] is MediaPipe's
 * confidence that the point is actually present in the frame (`[0, 1]`).
 *
 * Pure data — no Android or MediaPipe types — so the squat maths is unit-testable on a plain JVM.
 */
data class PoseLandmark(
    val x: Float,
    val y: Float,
    val visibility: Float,
)

/**
 * One detected person for a single frame: the 33 MediaPipe pose landmarks plus the dimensions of
 * the upright frame they were measured against (needed by the overlay's center-crop mapper).
 */
data class PoseFrame(
    val landmarks: List<PoseLandmark>,
    val sourceWidth: Int,
    val sourceHeight: Int,
)

/**
 * Indices into the MediaPipe Pose Landmarker's 33-point topology. Only the lower-body points the
 * squat analysis needs are named here.
 */
object PoseLandmarks {
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28

    /** Total landmarks MediaPipe emits; used to validate a [PoseFrame] before indexing. */
    const val COUNT = 33
}
