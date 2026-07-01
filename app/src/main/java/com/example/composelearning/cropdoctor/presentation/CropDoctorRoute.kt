package com.example.composelearning.cropdoctor.presentation

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

/**
 * Entry point for the on-device **Crop Doctor** (pest & disease detection) feature.
 *
 * Wires the [CropDoctorViewModel] to two image sources:
 *  - **Gallery** via the system photo picker ([ActivityResultContracts.PickVisualMedia]) — needs no
 *    runtime permission, the simplest path for a non-tech-savvy farmer.
 *  - **Camera** via [ActivityResultContracts.TakePicture] into a FileProvider Uri, gated behind the
 *    CAMERA permission.
 *
 * The diagnosis itself runs entirely on-device (see [com.example.composelearning.cropdoctor.data.PlantDiseaseClassifier]).
 */
@Composable
fun CropDoctorRoute(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: CropDoctorViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Gallery: system photo picker (no permission required).
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let(viewModel::analyze) }

    // Camera: capture into a FileProvider Uri, then analyze on success.
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) pendingCameraUri?.let(viewModel::analyze) }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(context)
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
    }

    CropDoctorScreen(
        state = state,
        onTakePhoto = { cameraPermission.launch(Manifest.permission.CAMERA) },
        onPickGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onReset = viewModel::reset,
        onBack = onBack
    )
}

/** Creates a FileProvider Uri in cacheDir/images for the camera app to write the full-res capture. */
private fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "leaf_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
