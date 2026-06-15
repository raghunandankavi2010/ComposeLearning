package com.example.composelearning.imagecropper.domain

import android.graphics.Bitmap
import android.net.Uri

/**
 * Abstraction over image IO. Keeping this in the domain layer lets the ViewModel and
 * use cases depend on an interface rather than on Android's [android.content.ContentResolver]
 * / [android.provider.MediaStore], which keeps them testable and the dependency arrow
 * pointing inward (data → domain).
 *
 * Every returned [Bitmap] is guaranteed to be a **software**, mutable-friendly,
 * `ARGB_8888` bitmap so that region cropping ([Bitmap.createBitmap]) and canvas masking
 * are always legal — hardware bitmaps would throw.
 */
interface ImageRepository {

    /** Decodes the bundled sample photo used as the initial subject. */
    suspend fun loadDefault(): Bitmap

    /** Decodes a user-picked image, applying EXIF orientation. */
    suspend fun load(uri: Uri): Bitmap

    /**
     * Persists [bitmap] to the shared "Pictures" collection as a PNG (preserving the
     * transparency produced by the circle/star masks).
     *
     * @return the [Uri] of the saved item, or `null` if the insert failed.
     */
    suspend fun save(bitmap: Bitmap, displayName: String): Uri?
}
