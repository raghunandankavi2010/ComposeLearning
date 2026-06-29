package com.example.composelearning.photoquality

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import java.io.File
import java.util.Locale

/**
 * Route for the on-device photo-quality demo. Scores bundled samples plus any image the user picks
 * from the gallery or captures with the camera, using the Google NIMA (technical) TFLite model.
 * See [PhotoQualityClassifier] for how the 1–10 mean opinion score is derived.
 */
@Composable
fun PhotoQualityRoute(
    initialUri: Uri? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PhotoQualityViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Analyze the URI if passed via navigation (e.g. "moved" from another screen)
    LaunchedEffect(initialUri) {
        initialUri?.let { viewModel.analyzeUri(it) }
    }

    // ── Gallery / camera launchers ──────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let(viewModel::analyzeUri) }

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) pendingCameraUri?.let(viewModel::analyzeUri) }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "On-Device Photo Quality",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Hybrid Accuracy Model: We combine Google NIMA (lighting/exposure) " +
                    "with a Laplacian Variance check (sharpness/focus). This prevents blurred " +
                    "captures from passing, even if the model scores them highly.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            state.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        "Model failed to load: $err",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // ── Your photo: gallery + camera ──────────────────────────────────
            Text("Your photo", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = state.modelReady,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text("  Gallery")
                }
                OutlinedButton(
                    onClick = { cameraPermission.launch(Manifest.permission.CAMERA) },
                    enabled = state.modelReady,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Text("  Camera")
                }
            }

            state.userCapture?.let { capture ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = capture.uri,
                            contentDescription = "Selected photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(8.dp))
                        when {
                            capture.isAnalyzing -> AnalyzingRow()
                            capture.error != null -> Text(
                                "Could not read image: ${capture.error}",
                                color = MaterialTheme.colorScheme.error
                            )
                            capture.result != null -> ResultRow(capture.result)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Bundled samples ───────────────────────────────────────────────
            Text("Sample images", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.samples.chunked(2).forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEachIndexed { colIndex, sample ->
                        val index = rowIndex * 2 + colIndex
                        SampleCard(
                            sample = sample,
                            enabled = state.modelReady,
                            onAnalyze = { viewModel.analyze(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = { viewModel.analyzeAll() },
                enabled = state.modelReady,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
            ) {
                Text(if (state.modelReady) "Analyze All Samples" else "Loading model…")
            }
        }
    }
}

@Composable
private fun SampleCard(
    sample: SampleImage,
    enabled: Boolean,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = sample.resId),
                contentDescription = sample.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.height(6.dp))
            Text(sample.caption, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            when {
                sample.isAnalyzing -> AnalyzingRow()
                sample.result != null -> ResultRow(sample.result)
                else -> Button(
                    onClick = onAnalyze,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Analyze") }
            }
        }
    }
}

@Composable
private fun AnalyzingRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.height(18.dp))
        Text("  Analyzing…", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ResultRow(result: QualityResult) {
    val good = result.isGoodQuality
    val accent = if (good) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (good) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = accent
            )
            Text(
                text = if (good) "  Good" else "  Unclear",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Text(
            text = String.format(Locale.getDefault(), "NIMA: %.2f | Sharpness: %.1f", result.meanScore, result.sharpnessScore),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** Creates a FileProvider Uri in cacheDir/images for the camera app to write the full-res capture. */
private fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
