package com.example.composelearning.arglasses.data

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composelearning.arglasses.domain.model.FaceAnchors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions

/**
 * CameraX [ImageAnalysis.Analyzer] that runs ML Kit Face Mesh on each streamed frame and
 * emits the extracted [FaceAnchors] (or an "empty" signal when no face is found).
 *
 * Backpressure is handled at the `ImageAnalysis` level with `KEEP_ONLY_LATEST`, so this
 * analyzer always works on the freshest frame and never queues — essential for a real-time
 * AR overlay.
 *
 * ### ImageProxy lifecycle (the "finally" for async work)
 * ML Kit reads from the backing `mediaImage` *asynchronously*. Closing the [ImageProxy] in
 * a synchronous `finally` would free the buffer before detection finishes and starve the
 * camera (frozen preview / dropped frames). The correct equivalent of `finally` for an
 * async `Task` is [com.google.android.gms.tasks.Task.addOnCompleteListener], which is
 * guaranteed to run exactly once on success *or* failure — that's where we close. The
 * surrounding `try/catch` is the safety net for the rare *synchronous* throw (e.g. building
 * the `InputImage`), which closes the proxy so the pipeline can't leak a frame.
 */
class FaceMeshAnalyzer(
    private val onFace: (FaceAnchors) -> Unit,
    private val onEmpty: () -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

    private val detector = FaceMeshDetection.getClient(
        FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build(),
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        // ML Kit returns coordinates in the *upright* frame, so report upright dimensions:
        // they are swapped relative to the buffer for 90°/270° sensor rotations.
        val uprightWidth: Int
        val uprightHeight: Int
        if (rotation == 90 || rotation == 270) {
            uprightWidth = imageProxy.height
            uprightHeight = imageProxy.width
        } else {
            uprightWidth = imageProxy.width
            uprightHeight = imageProxy.height
        }

        try {
            val input = InputImage.fromMediaImage(mediaImage, rotation)
            detector.process(input)
                .addOnSuccessListener { meshes ->
                    val mesh = meshes.firstOrNull()
                    if (mesh == null) {
                        onEmpty()
                    } else {
                        onFace(mesh.toFaceAnchors(uprightWidth, uprightHeight))
                    }
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Face mesh detection failed", error)
                    onError(error)
                }
                .addOnCompleteListener {
                    // Always frees the frame, on success or failure — the async "finally".
                    imageProxy.close()
                }
        } catch (t: Throwable) {
            // Synchronous failure path: the complete-listener never attached, so close here.
            Log.e(TAG, "Unable to submit frame for detection", t)
            onError(t)
            imageProxy.close()
        }
    }

    /** Releases the detector's native resources. Call from the owner's teardown. */
    fun release() {
        runCatching { detector.close() }
    }

    private companion object {
        const val TAG = "FaceMeshAnalyzer"
    }
}
