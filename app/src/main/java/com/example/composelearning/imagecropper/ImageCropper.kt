package com.example.composelearning.imagecropper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.imagecropper.presentation.CropperIntent
import com.example.composelearning.imagecropper.presentation.CropperViewModel
import com.example.composelearning.imagecropper.presentation.ImageCropperScreen

/**
 * Entry point for the Image Cropper feature.
 *
 * A production-style cropper built with clean architecture:
 *  - **domain** — `CropShape`, `ImageRepository`, `CropImageUseCase` (pure logic + bitmap work)
 *  - **data** — `ImageRepositoryImpl` (decode / MediaStore IO)
 *  - **presentation** — `CropperViewModel` (durable MVI state) + `CropController`
 *    (live gesture geometry held as Compose snapshot state for recomposition-free dragging)
 *
 * Capabilities: load the bundled sample or pick a photo, pinch-to-zoom & pan the image,
 * drag/resize a rule-of-thirds crop frame, and produce a real cropped bitmap masked to a
 * rectangle, circle, or star — with save-to-gallery as PNG.
 *
 * Route this from `AppNavigation`'s `entryProvider`:
 * ```
 * entry<AnimScreen.ImageCropper> { ImageCropperRoute() }
 * ```
 */
@Composable
fun ImageCropperRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: CropperViewModel = viewModel(
        factory = CropperViewModel.Factory(context),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Kick off the one-shot initial load here rather than in the ViewModel's init, so
    // construction stays side-effect-free and testable. Keyed on Unit — it fires exactly
    // once when the screen enters composition; the handler is idempotent, so the re-run
    // after a configuration change is a no-op.
    LaunchedEffect(Unit) { viewModel.onIntent(CropperIntent.LoadDefault) }

    ImageCropperScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}
