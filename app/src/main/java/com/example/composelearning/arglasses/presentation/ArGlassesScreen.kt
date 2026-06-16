package com.example.composelearning.arglasses.presentation

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.arglasses.domain.model.FaceTransform
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Entry point for the AR Glasses feature. Wires the ViewModel to the stateless screen and
 * exposes the per-frame transform via a provider lambda (read in the overlay's draw phase).
 */
@Composable
fun ArGlassesRoute(modifier: Modifier = Modifier) {
    val viewModel: ArGlassesViewModel = viewModel(factory = ArGlassesViewModel.Factory())
    val state by viewModel.state.collectAsStateWithLifecycle()

    ArGlassesScreen(
        state = state,
        analyzer = viewModel.analyzer,
        transformProvider = { viewModel.trackedFace },
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArGlassesScreen(
    state: ArGlassesUiState,
    analyzer: androidx.camera.core.ImageAnalysis.Analyzer,
    transformProvider: () -> FaceTransform?,
    onIntent: (ArGlassesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Surface errors as a toast, then clear them from state.
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onIntent(ArGlassesIntent.ConsumeError)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when (cameraPermission.status) {
            PermissionStatus.Granted -> {
                CameraPreview(analyzer = analyzer, modifier = Modifier.fillMaxSize())

                val selectedStyle = GlassesCatalog.byId(state.selectedStyleId)

                if (state.trackingEnabled) {
                    // Front camera → mirror to match the auto-mirrored selfie preview.
                    GlassesOverlay(
                        image = ImageBitmap.imageResource(selectedStyle.drawableRes),
                        colorFilter = selectedStyle.tintFilter(),
                        transformProvider = transformProvider,
                        mirror = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                StatusChip(
                    trackingEnabled = state.trackingEnabled,
                    faceDetected = state.faceDetected,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    GlassesPicker(
                        selectedStyleId = state.selectedStyleId,
                        onSelect = { onIntent(ArGlassesIntent.SelectStyle(it)) },
                    )
                    TrackingToggle(
                        enabled = state.trackingEnabled,
                        onToggle = { onIntent(ArGlassesIntent.ToggleTracking) },
                    )
                }
            }

            else -> CameraPermissionRequest(
                shouldShowRationale = (cameraPermission.status as? PermissionStatus.Denied)
                    ?.shouldShowRationale == true,
                onRequest = cameraPermission::launchPermissionRequest,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun StatusChip(
    trackingEnabled: Boolean,
    faceDetected: Boolean,
    modifier: Modifier = Modifier,
) {
    val (label, tint) = when {
        !trackingEnabled -> "Tracking paused" to Color(0xCC616161)
        faceDetected -> "Face tracked" to Color(0xCC2E7D32)
        else -> "Searching for a face…" to Color(0xCCB26A00)
    }
    Box(
        modifier = modifier
            .background(tint, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Horizontal strip of selectable spectacles. Each thumbnail shows the real artwork with its
 * tint applied, so the swatch matches exactly what gets drawn on the face.
 */
@Composable
private fun GlassesPicker(
    selectedStyleId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xAA000000), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(GlassesCatalog.styles, key = { it.id }) { style ->
            val selected = style.id == selectedStyleId
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = ImageBitmap.imageResource(style.drawableRes),
                    contentDescription = style.name,
                    colorFilter = style.tintFilter(),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onSelect(style.id) }
                        .background(
                            if (selected) Color(0x33FFFFFF) else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .border(
                            width = if (selected) 2.dp else 0.dp,
                            color = if (selected) Color(0xFF4FC3F7) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(6.dp),
                )
                Text(
                    text = style.name,
                    color = if (selected) Color(0xFF4FC3F7) else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun TrackingToggle(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xAA000000), RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Virtual glasses", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Switch(checked = enabled, onCheckedChange = { onToggle() })
        }
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
                "Camera access is needed to track your face and place the glasses."
            } else {
                "Grant camera access to try on the virtual glasses."
            },
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRequest) { Text("Enable camera") }
    }
}
