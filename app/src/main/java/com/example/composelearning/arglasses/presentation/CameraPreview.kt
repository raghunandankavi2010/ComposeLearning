package com.example.composelearning.arglasses.presentation

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

/**
 * Hosts a CameraX [PreviewView] and binds the front camera's [Preview] + [ImageAnalysis]
 * use cases to the composition's lifecycle.
 *
 * - The analysis runs on a dedicated single-thread executor, off the main thread.
 * - `STRATEGY_KEEP_ONLY_LATEST` drops stale frames so the [analyzer] always sees the
 *   freshest image — the right choice for a live AR overlay.
 * - `FILL_CENTER` matches [FaceMeshCoordinateMapper]'s center-crop math, and `PreviewView`
 *   auto-mirrors the front camera so the preview and the overlay agree.
 * - Binding and teardown are scoped to [DisposableEffect]; the executor is shut down and
 *   the provider unbound on dispose to avoid leaking the camera.
 */
@Composable
fun CameraPreview(
    analyzer: ImageAnalysis.Analyzer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(lifecycleOwner) {
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null

        providerFuture.addListener({
            val provider = providerFuture.get().also { cameraProvider = it }

            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply { setAnalyzer(analysisExecutor, analyzer) }

            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis,
                )
            }.onFailure { Log.e(TAG, "Camera binding failed", it) }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
            analysisExecutor.shutdown()
        }
    }
}

private const val TAG = "CameraPreview"
