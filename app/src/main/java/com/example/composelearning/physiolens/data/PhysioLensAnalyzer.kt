package com.example.composelearning.physiolens.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composelearning.physiolens.presentation.PhysioLensContract.PosePoint
import com.google.mediapipe.framework.image.MediaImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PhysioLensAnalyzer(
    context: Context,
    private val onResult: (Map<Int, PosePoint>?, width: Int, height: Int) -> Unit,
    private val onError: (Throwable) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile private var uprightWidth = 1080
    @Volatile private var uprightHeight = 1920
    private var cachedRotation = Int.MIN_VALUE
    private var imageProcessingOptions: ImageProcessingOptions = ImageProcessingOptions.builder().build()

    private val landmarker: PoseLandmarker? = runCatching {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .setDelegate(Delegate.GPU)
            .build()
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setResultListener { result, _ -> handleResult(result) }
            .setErrorListener { error -> onError(error) }
            .build()
        PoseLandmarker.createFromOptions(context, options)
    }.onFailure { error ->
        Log.w(TAG, "MediaPipe PoseLandmarker GPU delegate init failed, falling back to CPU", error)
        runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.CPU)
                .build()
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(1)
                .setResultListener { result, _ -> handleResult(result) }
                .setErrorListener { error -> onError(error) }
                .build()
            PoseLandmarker.createFromOptions(context, options)
        }.onFailure { cpuError ->
            Log.e(TAG, "MediaPipe PoseLandmarker unavailable", cpuError)
            onError(cpuError)
        }.getOrNull()
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
            Log.e(TAG, "Frame submission error", t)
            onError(t)
        } finally {
            imageProxy.close()
        }
    }

    private fun handleResult(result: PoseLandmarkerResult) {
        val pose = result.landmarks().firstOrNull()
        if (pose.isNullOrEmpty()) {
            onResult(null, uprightWidth, uprightHeight)
            return
        }
        val points = mutableMapOf<Int, PosePoint>()
        for (i in pose.indices) {
            val lm = pose[i]
            points[i] = PosePoint(
                x = lm.x(),
                y = lm.y(),
                z = lm.z(),
                visibility = lm.visibility().orElse(1f)
            )
        }
        onResult(points, uprightWidth, uprightHeight)
    }

    fun release() {
        runCatching { landmarker?.close() }
    }

    private companion object {
        const val TAG = "PhysioLensAnalyzer"
    }
}
