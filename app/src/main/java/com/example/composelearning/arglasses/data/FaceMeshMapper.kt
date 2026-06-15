package com.example.composelearning.arglasses.data

import com.example.composelearning.arglasses.domain.model.FaceAnchors
import com.example.composelearning.arglasses.domain.model.Vec3
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshPoint

/**
 * Extracts only the contours the AR anchoring needs from a raw ML Kit [FaceMesh] and lifts
 * them into the framework-free [FaceAnchors] domain model.
 *
 * Using the named contour constants (rather than raw indices into `getAllPoints()`) keeps
 * the anchoring readable and robust to mesh-ordering changes:
 *  - [FaceMesh.LEFT_EYE] / [FaceMesh.RIGHT_EYE] → eye centers (anchor + roll)
 *  - [FaceMesh.FACE_OVAL] → temple-to-temple width (scale)
 *  - [FaceMesh.NOSE_BRIDGE] → vertical bridge bias + coarse pitch
 *
 * @param sourceWidth  width of the upright frame the points were measured in.
 * @param sourceHeight height of the upright frame.
 */
fun FaceMesh.toFaceAnchors(sourceWidth: Int, sourceHeight: Int): FaceAnchors = FaceAnchors(
    leftEye = getPoints(FaceMesh.LEFT_EYE).toVecs(),
    rightEye = getPoints(FaceMesh.RIGHT_EYE).toVecs(),
    faceOval = getPoints(FaceMesh.FACE_OVAL).toVecs(),
    noseBridge = getPoints(FaceMesh.NOSE_BRIDGE).toVecs(),
    sourceWidth = sourceWidth,
    sourceHeight = sourceHeight,
)

private fun List<FaceMeshPoint>.toVecs(): List<Vec3> = map { point ->
    val position = point.position
    Vec3(position.x, position.y, position.z)
}
