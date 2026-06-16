package com.example.composelearning.formguard.presentation

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.formguard.domain.model.PoseFrame
import com.example.composelearning.formguard.domain.model.PoseLandmarks
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Entry point for the FormGuard feature. Wires the ViewModel to the stateless screen and exposes
 * the per-frame skeleton via a provider lambda (read in the overlay's draw phase).
 */
@Composable
fun FormGuardRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: FormGuardViewModel =
        viewModel(factory = FormGuardViewModel.Factory(context.applicationContext))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FormGuardScreen(
        state = state,
        analyzer = viewModel.analyzer,
        poseFrameProvider = { viewModel.poseFrame },
        onResetReps = viewModel::resetReps,
        modifier = modifier,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FormGuardScreen(
    state: PoseUiState,
    analyzer: ImageAnalysis.Analyzer,
    poseFrameProvider: () -> PoseFrame?,
    onResetReps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when {
            cameraPermission.status !is PermissionStatus.Granted -> CameraPermissionRequest(
                shouldShowRationale = (cameraPermission.status as? PermissionStatus.Denied)
                    ?.shouldShowRationale == true,
                onRequest = cameraPermission::launchPermissionRequest,
                modifier = Modifier.align(Alignment.Center),
            )

            state is PoseUiState.Error -> ErrorContent(
                message = state.errorMsg,
                modifier = Modifier.align(Alignment.Center),
            )

            else -> {
                val tracking = state as? PoseUiState.Tracking

                CameraPreview(analyzer = analyzer, modifier = Modifier.fillMaxSize())

                SkeletonOverlay(
                    poseFrameProvider = poseFrameProvider,
                    caving = tracking?.isKneesCaving == true,
                    modifier = Modifier.fillMaxSize(),
                )

                FormHud(
                    repCount = tracking?.repCount ?: 0,
                    kneeAngle = tracking?.kneeAngle ?: 0f,
                    feedback = tracking?.feedbackMessage ?: "Initializing…",
                    caving = tracking?.isKneesCaving == true,
                    onResetReps = onResetReps,
                )

                AudioCues(
                    repCount = tracking?.repCount ?: 0,
                    caving = tracking?.isKneesCaving == true,
                )
            }
        }
    }
}

/**
 * Hosts a CameraX [PreviewView] and binds the **rear** camera's [Preview] + [ImageAnalysis] to the
 * composition lifecycle. Analysis runs on a dedicated single-thread executor; `KEEP_ONLY_LATEST`
 * drops stale frames; binding/teardown is scoped to [DisposableEffect] so the camera never leaks.
 */
@Composable
private fun CameraPreview(
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

    androidx.compose.ui.viewinterop.AndroidView(factory = { previewView }, modifier = modifier)

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
                    CameraSelector.DEFAULT_BACK_CAMERA,
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

/**
 * Draws the Hip → Knee → Ankle skeleton over the preview. [poseFrameProvider] is read **inside the
 * Canvas draw lambda**, so each ~30 fps frame invalidates drawing only, never recomposition. Turns
 * red while [caving] (knee valgus) is flagged.
 */
@Composable
private fun SkeletonOverlay(
    poseFrameProvider: () -> PoseFrame?,
    caving: Boolean,
    modifier: Modifier = Modifier,
) {
    val boneColor = if (caving) Color(0xFFE53935) else Color(0xFF4FC3F7)
    val jointColor = if (caving) Color(0xFFFF8A80) else Color(0xFF80D8FF)

    Canvas(modifier = modifier) {
        val frame = poseFrameProvider() ?: return@Canvas
        if (frame.landmarks.size < PoseLandmarks.COUNT ||
            frame.sourceWidth == 0 || frame.sourceHeight == 0
        ) {
            return@Canvas
        }

        val mapper = PoseCoordinateMapper(
            sourceWidth = frame.sourceWidth.toFloat(),
            sourceHeight = frame.sourceHeight.toFloat(),
            viewWidth = size.width,
            viewHeight = size.height,
        )

        fun point(index: Int): Offset {
            val lm = frame.landmarks[index]
            return Offset(mapper.mapX(lm.x), mapper.mapY(lm.y))
        }

        fun visible(index: Int) = frame.landmarks[index].visibility >= MIN_VISIBILITY

        for ((from, to) in SKELETON_BONES) {
            if (visible(from) && visible(to)) {
                drawLine(boneColor, point(from), point(to), strokeWidth = 10f)
            }
        }
        for (index in TRACKED_JOINTS) {
            if (visible(index)) {
                drawCircle(jointColor, radius = 14f, center = point(index))
            }
        }
    }
}

/** Top rep counter / knee angle and a bottom feedback banner with a reset action. */
@Composable
private fun FormHud(
    repCount: Int,
    kneeAngle: Float,
    feedback: String,
    caving: Boolean,
    onResetReps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Metric(label = "REPS", value = repCount.toString())
            Metric(label = "KNEE", value = "${kneeAngle.roundToInt()}°")
            TextButton(onClick = onResetReps) { Text("Reset", color = Color.White) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .background(
                    if (caving) Color(0xCCC62828) else Color(0xAA000000),
                    RoundedCornerShape(50),
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
        ) {
            Text(
                text = feedback,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFB0BEC5), style = MaterialTheme.typography.labelMedium)
    }
}

/**
 * Plays a short tone on every completed rep and a distinct warning tone when valgus is first
 * detected. The [ToneGenerator] is remembered and released on dispose.
 */
@Composable
private fun AudioCues(repCount: Int, caving: Boolean) {
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 90) }
    DisposableEffect(Unit) { onDispose { toneGenerator.release() } }

    LaunchedEffect(repCount) {
        if (repCount > 0) toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }
    LaunchedEffect(caving) {
        if (caving) toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
    }
}

@Composable
private fun CameraPermissionRequest(
    shouldShowRationale: Boolean,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (shouldShowRationale) {
                "Camera access is needed to analyse your squat form on-device. No video leaves your phone."
            } else {
                "Grant camera access to start the FormGuard squat coach."
            },
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRequest) { Text("Enable camera") }
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        color = Color(0xFFFF8A80),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.padding(32.dp),
    )
}

/**
 * Converts **normalized** `[0,1]` landmark coordinates (upright camera frame) into on-screen
 * viewport pixels, reproducing `PreviewView`'s `FILL_CENTER` center-crop. Pure value type; no
 * allocation beyond itself, constructed fresh per draw.
 */
private class PoseCoordinateMapper(
    private val sourceWidth: Float,
    private val sourceHeight: Float,
    viewWidth: Float,
    viewHeight: Float,
) {
    private val scale = max(viewWidth / sourceWidth, viewHeight / sourceHeight)
    private val offsetX = (viewWidth - sourceWidth * scale) / 2f
    private val offsetY = (viewHeight - sourceHeight * scale) / 2f

    fun mapX(normX: Float): Float = normX * sourceWidth * scale + offsetX
    fun mapY(normY: Float): Float = normY * sourceHeight * scale + offsetY
}

private const val TAG = "FormGuardScreen"
private const val MIN_VISIBILITY = 0.5f

/** Hip → Knee → Ankle bones (plus the hip line) drawn for both legs. */
private val SKELETON_BONES = listOf(
    PoseLandmarks.LEFT_HIP to PoseLandmarks.LEFT_KNEE,
    PoseLandmarks.LEFT_KNEE to PoseLandmarks.LEFT_ANKLE,
    PoseLandmarks.RIGHT_HIP to PoseLandmarks.RIGHT_KNEE,
    PoseLandmarks.RIGHT_KNEE to PoseLandmarks.RIGHT_ANKLE,
    PoseLandmarks.LEFT_HIP to PoseLandmarks.RIGHT_HIP,
)

private val TRACKED_JOINTS = listOf(
    PoseLandmarks.LEFT_HIP, PoseLandmarks.RIGHT_HIP,
    PoseLandmarks.LEFT_KNEE, PoseLandmarks.RIGHT_KNEE,
    PoseLandmarks.LEFT_ANKLE, PoseLandmarks.RIGHT_ANKLE,
)
