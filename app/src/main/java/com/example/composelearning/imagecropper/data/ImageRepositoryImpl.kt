package com.example.composelearning.imagecropper.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.composelearning.R
import com.example.composelearning.imagecropper.domain.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ImageRepository] backed by Android's decoders and [MediaStore].
 *
 * Holds only the *application* context to avoid leaking an Activity. Decoding and disk IO
 * run on [Dispatchers.IO].
 */
class ImageRepositoryImpl(
    context: Context,
) : ImageRepository {

    private val appContext = context.applicationContext

    override suspend fun loadDefault(): Bitmap = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        BitmapFactory.decodeResource(appContext.resources, R.drawable.sample_photo, options)
            .toSoftwareArgb()
    }

    override suspend fun load(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(appContext.contentResolver, uri)
        // SOFTWARE allocator => readable, croppable bitmap; ImageDecoder applies EXIF rotation.
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }.toSoftwareArgb()
    }

    override suspend fun save(bitmap: Bitmap, displayName: String): Uri? = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/ComposeCropper",
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(collection, values) ?: return@withContext null
        runCatching {
            resolver.openOutputStream(uri)?.use { stream ->
                // PNG keeps the alpha channel produced by circle/star masks.
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: error("Unable to open output stream")
        }.onFailure {
            resolver.delete(uri, null, null)
            return@withContext null
        }

        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    }

    /** Guarantees a software `ARGB_8888` bitmap regardless of how the decoder allocated it. */
    private fun Bitmap.toSoftwareArgb(): Bitmap =
        if (config == Bitmap.Config.ARGB_8888 && !isHardwareBacked()) {
            this
        } else {
            copy(Bitmap.Config.ARGB_8888, false).also { if (it !== this) recycle() }
        }

    private fun Bitmap.isHardwareBacked(): Boolean =
        config == Bitmap.Config.HARDWARE
}
