package com.example.composelearning.imagecropper.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.example.composelearning.imagecropper.domain.CropShape

/**
 * Durable, snapshot-able state owned by [CropperViewModel].
 *
 * Deliberately excluded from this state: the live `zoom` / `pan` / crop-frame rectangle.
 * Those mutate at ~60fps during gestures and are *pure view interaction* — they live in
 * [CropController] (Compose snapshot state read in the draw/layout phase) so a drag never
 * triggers a ViewModel round-trip or a recomposition. The ViewModel owns only what must
 * survive process death and what crosses the UI boundary: the subject image and the
 * committed crop result.
 */
@Immutable
data class CropperUiState(
    val isLoading: Boolean = true,
    val image: ImageBitmap? = null,
    val imageSize: IntSize = IntSize.Zero,
    val shape: CropShape = CropShape.Rectangle,
    val isProcessing: Boolean = false,
    val result: Bitmap? = null,
    val message: String? = null,
)

/** Unidirectional events flowing UI → ViewModel. */
sealed interface CropperIntent {
    data object LoadDefault : CropperIntent
    data class LoadFromUri(val uri: Uri) : CropperIntent
    data class SelectShape(val shape: CropShape) : CropperIntent

    /** Commit a crop. [region] is in *source-bitmap pixel* coordinates (mapped by [CropController]). */
    data class Crop(val region: IntRect) : CropperIntent

    data object SaveResult : CropperIntent
    data object DismissResult : CropperIntent
    data object ConsumeMessage : CropperIntent
}
