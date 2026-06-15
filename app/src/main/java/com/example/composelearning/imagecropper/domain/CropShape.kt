package com.example.composelearning.imagecropper.domain

/**
 * The output silhouette the cropped pixels are masked into.
 *
 * This is a pure domain concept — it carries no Android/Compose types so it can be
 * reasoned about and unit-tested in isolation. The actual pixel masking lives in
 * [CropImageUseCase] (for the produced bitmap) and the on-screen outline is drawn by
 * the overlay; both derive their geometry from this single source of truth.
 */
enum class CropShape {
    Rectangle,
    Circle,
    Star,
}
