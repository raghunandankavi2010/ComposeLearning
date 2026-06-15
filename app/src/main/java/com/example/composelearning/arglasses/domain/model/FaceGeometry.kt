package com.example.composelearning.arglasses.domain.model

/**
 * A single 3D landmark in **source-image pixel** space.
 *
 * `x` / `y` are pixels in the upright (rotation-corrected) camera frame; `z` is ML Kit's
 * relative depth (smaller = closer to the camera), useful only for coarse yaw/pitch hints.
 *
 * Pure data — no Android or ML Kit types — so the geometry use case is unit-testable on a
 * plain JVM without Robolectric.
 */
data class Vec3(val x: Float, val y: Float, val z: Float)

/**
 * The minimal set of ML Kit Face Mesh contours the AR anchoring needs, already extracted
 * from the raw [com.google.mlkit.vision.facemesh.FaceMesh] in the data layer. Keeping the
 * domain dependent only on this (and not on ML Kit) keeps the dependency arrow pointing
 * inward and the math trivially testable.
 *
 * @property sourceWidth  width of the upright frame the landmarks were measured in.
 * @property sourceHeight height of the upright frame.
 */
data class FaceAnchors(
    val leftEye: List<Vec3>,
    val rightEye: List<Vec3>,
    val faceOval: List<Vec3>,
    val noseBridge: List<Vec3>,
    val sourceWidth: Int,
    val sourceHeight: Int,
)

/**
 * The result of resolving a face into a renderable transform for the glasses asset.
 * All spatial values are in **source-image pixel** space; the presentation layer's
 * coordinate mapper converts them to the on-screen viewport (handling fill-scaling and
 * front-camera mirroring).
 *
 * @property anchorX     X of the bridge anchor (between the eyes).
 * @property anchorY     Y of the bridge anchor.
 * @property faceWidthPx temple-to-temple width used to scale the frame.
 * @property rollRadians Z-axis tilt from the inter-eye slope (head tilt).
 * @property yawRadians  coarse left/right head turn estimate (from inter-eye depth delta).
 * @property pitchRadians coarse up/down head tilt estimate (from nose-bridge depth slope).
 */
data class FaceTransform(
    val anchorX: Float,
    val anchorY: Float,
    val faceWidthPx: Float,
    val rollRadians: Float,
    val yawRadians: Float,
    val pitchRadians: Float,
    val sourceWidth: Int,
    val sourceHeight: Int,
)
