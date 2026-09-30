package com.example.composelearning.physiolens.presentation

import android.Manifest
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.physiolens.data.PhysioLensAnalyzer
import com.example.composelearning.physiolens.presentation.PhysioLensContract.Intent
import com.example.composelearning.physiolens.presentation.PhysioLensContract.MuscleStrainData
import com.example.composelearning.physiolens.presentation.PhysioLensContract.StrainStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhysioLensScreen(
    onBack: () -> Unit = {},
    viewModel: PhysioLensViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        viewModel.processIntent(Intent.PermissionResult(cameraPermissionState.status.isGranted))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PhysioLens AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (state.isSimulationMode) "Simulation Mode (Demo Engine)" else "Live Camera Kinetic Pipeline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.processIntent(Intent.ToggleSimulationMode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isSimulationMode) Color(0xFF00E5FF) else Color(0xFF37474F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (state.isSimulationMode) "Sim" else "Camera",
                            color = if (state.isSimulationMode) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Camera or Background
            if (!state.isSimulationMode && cameraPermissionState.status.isGranted) {
                CameraPreviewView(
                    onFrameAnalyzed = { poseMap, w, h ->
                        viewModel.onFrameAnalyzed(poseMap, w, h)
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF161B22))
                )
            }

            // Live Biomechanical Vector Overlay
            PhysioOverlayCanvas(
                posePoints = state.posePoints,
                analysis = state.analysis,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom HUD / Tab Content Panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xEE0D1117))
                    .padding(16.dp)
            ) {
                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF00E5FF)
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.processIntent(Intent.SelectTab(0)) },
                        text = { Text("Kinetic HUD", color = Color.White) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.processIntent(Intent.SelectTab(1)) },
                        text = { Text("Muscle Heatmap", color = Color.White) }
                    )
                    Tab(
                        selected = state.selectedTab == 2,
                        onClick = { viewModel.processIntent(Intent.SelectTab(2)) },
                        text = { Text("AI Recovery", color = Color.White) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (state.selectedTab) {
                    0 -> KineticHudView(state)
                    1 -> MuscleHeatmapView(state)
                    2 -> AiRecoveryView(state)
                }
            }
        }
    }
}

@Composable
private fun KineticHudView(state: PhysioLensContract.State) {
    val analysis = state.analysis ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HudBadge("Elbow R", "${analysis.jointKinematics.elbowAngleRight.toInt()}°", Color(0xFFFF9100))
            HudBadge("Knee R", "${analysis.jointKinematics.kneeAngleRight.toInt()}°", Color(0xFF00E676))
            HudBadge("Spine Flex", "${analysis.jointKinematics.lumbarFlexionAngle.toInt()}°", Color(0xFFE040FB))
            HudBadge("L4/L5 Disc", "${analysis.jointKinematics.L4L5ShearLoadN.toInt()} N", Color(0xFFFF1744))
        }

        Surface(
            color = Color(0xFF21262D),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Primary Target: ${analysis.primaryActiveMuscle.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Neuromuscular Fatigue: ${analysis.overallFatigueIndex.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun MuscleHeatmapView(state: PhysioLensContract.State) {
    val analysis = state.analysis ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        analysis.muscleStrains.forEach { strain ->
            MuscleStrainRow(strain)
        }
    }
}

@Composable
private fun MuscleStrainRow(strain: MuscleStrainData) {
    val statusColor = when (strain.status) {
        StrainStatus.OPTIMAL -> Color(0xFF00E676)
        StrainStatus.MODERATE -> Color(0xFFFFD600)
        StrainStatus.HIGH_STRAIN -> Color(0xFFFF9100)
        StrainStatus.OVERLOAD -> Color(0xFFFF1744)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strain.muscleGroup.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${strain.strainPercentage.toInt()}% (${String.format("%.1f", strain.torqueNm)} Nm)",
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { strain.strainPercentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = statusColor,
            trackColor = Color(0xFF21262D)
        )
    }
}

@Composable
private fun AiRecoveryView(state: PhysioLensContract.State) {
    val analysis = state.analysis ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9100))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biomechanical Correction", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(analysis.aiCorrectionTip, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B382B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Healing, contentDescription = null, tint = Color(0xFF00E676))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Targeted Recovery Protocol", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(analysis.aiRecoveryProtocol, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun HudBadge(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161B22))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun CameraPreviewView(
    onFrameAnalyzed: (Map<Int, PhysioLensContract.PosePoint>?, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        val analyzer = PhysioLensAnalyzer(
            context = context,
            onResult = { poseMap, w, h -> onFrameAnalyzed(poseMap, w, h) },
            onError = { err -> }
        )

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(cameraExecutor, analyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            analyzer.release()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
