package com.example.composelearning.formguard.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composelearning.formguard.domain.model.PoseFrame
import com.example.composelearning.formguard.domain.model.PoseLandmark
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * CameraX [ImageAnalysis.Analyzer] that runs the **MediaPipe Pose Landmarker** on each streamed
 * frame and emits the extracted [PoseFrame] (or `null` when no person is found).
 *
 * The landmarker runs in [RunningMode.LIVE_STREAM] on the **GPU** delegate: [analyze] submits a
 * frame with `detectAsync` and returns immediately; results arrive on the result-listener callback
 * (which is wired to [onResult] at construction). Backpressure is handled at the `ImageAnalysis`
 * level with `KEEP_ONLY_LATEST`, so we always work on the freshest frame.
 *
 * ### ImageProxy lifecycle
 * MediaPipe's [MediaImageBuilder] conversion happens **synchronously** inside `detectAsync` (it
 * copies the frame into the graph before returning), so — unlike ML Kit's async `Task` — the
 * correct place to free the buffer is a plain `finally` right after the call. The zero-copy media
 * image also avoids a per-frame `Bitmap` allocation, keeping the analysis loop GC-quiet.
 *
 * @param context  application context (never an Activity) used to load the model from assets.
 * @param onResult invoked on the landmarker's listener thread with the latest frame, or `null`.
 * @param onError  invoked if the model fails to load or a frame can't be submitted.
 */
class FormGuardAnalyzer(
    context: Context,
    private val onResult: (PoseFrame?) -> Unit,
    private val onError: (Throwable) -> Unit,
) : ImageAnalysis.Analyzer {

    /** Upright frame dimensions of the last submitted frame, read back when results return. */
    @Volatile private var uprightWidth = 0
    @Volatile private var uprightHeight = 0

    // Cache the rotation options and only rebuild when the device rotation actually changes.
    private var cachedRotation = Int.MIN_VALUE
    private var imageProcessingOptions: ImageProcessingOptions =
        ImageProcessingOptions.builder().build()

    private val landmarker: PoseLandmarker? = runCatching {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET_PATH)
            .setDelegate(Delegate.GPU)
            .build()
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, _ -> handleResult(result) }
            .setErrorListener { error -> onError(error) }
            .build()
        PoseLandmarker.createFromOptions(context, options)
    }.onFailure { error ->
        Log.e(TAG, "Failed to create PoseLandmarker", error)
        onError(MissingModelException(error))
    }.getOrNull()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val landmarker = this.landmarker
        val mediaImage = imageProxy.image
        if (landmarker == null || mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        // MediaPipe returns coordinates in the upright frame; for 90°/270° those dimensions are
        // swapped relative to the buffer. Stash them so handleResult can report the right aspect.
        if (rotation == 90 || rotation == 270) {
            uprightWidth = imageProxy.height
            uprightHeight = imageProxy.width
        } else {
            uprightWidth = imageProxy.width
            uprightHeight = imageProxy.height
        }
        if (rotation != cachedRotation) {
            cachedRotation = rotation
            imageProcessingOptions = ImageProcessingOptions.builder()
                .setRotationDegrees(rotation)
                .build()
        }

        try {
            val mpImage = MediaImageBuilder(mediaImage).build()
            landmarker.detectAsync(mpImage, imageProcessingOptions, SystemClock.uptimeMillis())
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to submit frame for detection", t)
            onError(t)
        } finally {
            // MediaImageBuilder copies synchronously inside detectAsync, so freeing here is safe.
            imageProxy.close()
        }
    }

    private fun handleResult(result: PoseLandmarkerResult) {
        val pose = result.landmarks().firstOrNull()
        if (pose.isNullOrEmpty()) {
            onResult(null)
            return
        }
        // Map MediaPipe's NormalizedLandmark list into the domain's pure PoseLandmark list.
        val landmarks = ArrayList<PoseLandmark>(pose.size)
        for (lm in pose) {
            landmarks.add(
                PoseLandmark(
                    x = lm.x(),
                    y = lm.y(),
                    visibility = lm.visibility().orElse(0f),
                ),
            )
        }
        onResult(PoseFrame(landmarks, uprightWidth, uprightHeight))
    }

    /** Releases the landmarker's native resources. Call from the owner's teardown. */
    fun release() {
        runCatching { landmarker?.close() }
    }

    /** Thrown when the model asset can't be loaded, so the UI can show actionable instructions. */
    class MissingModelException(cause: Throwable) : Exception(
        "Couldn't load $MODEL_ASSET_PATH. Add it under app/src/main/assets/ (see FORMGUARD.md).",
        cause,
    )

    private companion object {
        const val TAG = "FormGuardAnalyzer"
        const val MODEL_ASSET_PATH = "pose_landmarker_lite.task"
    }
}
