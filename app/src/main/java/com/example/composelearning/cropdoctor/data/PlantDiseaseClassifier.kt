package com.example.composelearning.cropdoctor.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/** One label + its probability from the model, before it is mapped to farmer-facing advice. */
data class RawPrediction(
    val index: Int,
    val label: String,
    val confidence: Float
)

/**
 * On-device plant-disease image classifier.
 *
 * Wraps a MobileNet-based TFLite model trained on the **PlantVillage** dataset (38 classes across
 * 14 crops — see `assets/plant_labels.txt`). Everything runs locally: no network, no account, works
 * fully offline on any phone. See `cropdoctor/CROPDOCTOR.md` for the model source and license.
 *
 * ## Model I/O (must match how the model was trained, or accuracy collapses)
 * - Input: `[1, 224, 224, 3]` float32, RGB, normalized `pixel / 255f` → range `[0, 1]`, NHWC.
 * - Output: `[1, 38]` float32 softmax probabilities, indexed parallel to `plant_labels.txt`.
 */
class PlantDiseaseClassifier(context: Context) : Closeable {

    private val interpreter: Interpreter = Interpreter(loadModelFile(context, MODEL_ASSET))
    private val labels: List<String> = context.assets.open(LABELS_ASSET)
        .bufferedReader()
        .useLines { lines -> lines.map { it.trim() }.filter { it.isNotEmpty() }.toList() }

    /** Reused input buffer — inference is single-threaded via the ViewModel. */
    private val inputBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * CHANNELS * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
    private val output = Array(1) { FloatArray(labels.size) }

    val labelCount: Int get() = labels.size

    /** Returns the [topK] most likely classes, highest confidence first. */
    fun classify(bitmap: Bitmap, topK: Int = 3): List<RawPrediction> {
        val scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        writeNormalizedInput(scaled)
        interpreter.run(inputBuffer, output)

        val probs = output[0]
        return probs.indices
            .sortedByDescending { probs[it] }
            .take(topK)
            .map { i -> RawPrediction(i, labels.getOrElse(i) { "unknown" }, probs[i]) }
    }

    private fun writeNormalizedInput(bitmap: Bitmap) {
        inputBuffer.rewind()
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // PlantVillage model was trained on simple [0,1] normalization (mean 0, std 255).
            inputBuffer.putFloat(r / NORM)
            inputBuffer.putFloat(g / NORM)
            inputBuffer.putFloat(b / NORM)
        }
    }

    override fun close() = interpreter.close()

    private fun loadModelFile(context: Context, assetName: String): ByteBuffer {
        context.assets.openFd(assetName).use { fd ->
            FileInputStream(fd.fileDescriptor).channel.use { channel ->
                return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    }

    companion object {
        private const val MODEL_ASSET = "plant_disease_model.tflite"
        private const val LABELS_ASSET = "plant_labels.txt"
        private const val INPUT_SIZE = 224
        private const val CHANNELS = 3
        private const val NORM = 255f
    }
}
