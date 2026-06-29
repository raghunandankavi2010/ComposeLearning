package com.example.composelearning.photoquality

import android.content.Context
import android.graphics.Bitmap
import com.example.composelearning.photoquality.PhotoQualityClassifier.Companion.SHARPNESS_TILES
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Result of scoring a single image with [PhotoQualityClassifier].
 *
 * @param isGoodQuality true when the hybrid score (NIMA + Sharpness) passes our criteria.
 * @param meanScore the NIMA mean opinion score in [1f, 10f].
 * @param sharpnessScore a raw focus score (Laplacian variance); higher is sharper.
 */
data class QualityResult(
    val isGoodQuality: Boolean,
    val meanScore: Float,
    val sharpnessScore: Float
)

/**
 * On-device photo-quality scorer using a **Hybrid Accuracy Model**:
 * 1. **Google NIMA (Technical)**: A TFLite model that scores technical aesthetics (1-10).
 * 2. **Laplacian Variance**: A mathematical heuristic that detects focus and motion blur.
 *
 * ## What the model does
 * NIMA (Neural Image Assessment) does not emit a hard "good"/"bad" label. It outputs a **probability
 * distribution over 10 buckets** representing human opinion scores 1…10. The single-number quality
 * estimate is the distribution's expected value:
 *
 * ```
 * meanScore = Σ (i + 1) * p[i]   for i in 0..9        // in [1, 10]
 * ```
 *
 * The *technical* variant targets capture defects (blur, noise, exposure), so it is the right fit
 * for "is this photo clear?". We then threshold [meanScore] at [SCORE_THRESHOLD] → Good vs Unclear.
 *
 * ## Model I/O (confirmed from the model itself)
 * - Input: `[1, 224, 224, 3]` float32, normalized with `x / 127.5 - 1` → range [-1, 1]
 *   (Keras MobileNet `preprocess_input`).
 * - Output: `[1, 10]` float32 softmax probabilities.
 */
class PhotoQualityClassifier(context: Context) : Closeable {

    private val interpreter: Interpreter = Interpreter(loadModelFile(context, MODEL_ASSET))

    /** Reused input/output buffers — classification is single-threaded via the ViewModel. */
    private val inputBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * CHANNELS * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
    private val output = Array(1) { FloatArray(NUM_BUCKETS) }

    fun classify(bitmap: Bitmap): QualityResult {
        // 1. Run NIMA TFLite Inference
        val scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        writeNormalizedInput(scaled)
        interpreter.run(inputBuffer, output)

        var meanScore = 0f
        val probs = output[0]
        for (i in probs.indices) {
            meanScore += (i + 1) * probs[i]
        }

        // 2. Run mathematical Sharpness Check (Laplacian Variance)
        // We use the scaled 224x224 bitmap for speed; focus issues are visible even at this size.
        val sharpness = calculateSharpness(scaled)

        // 3. Hybrid Verdict:
        // NIMA is good at lighting/noise, but often misses motion blur (like the 4.56 keyboard).
        // A sharp image typically has a Laplacian variance > 10.0 on a 224px scaled bitmap.
        val isSharp = sharpness >= SHARPNESS_THRESHOLD
        val isTechnicallySound = meanScore >= NIMA_THRESHOLD

        return QualityResult(
            isGoodQuality = isSharp && isTechnicallySound,
            meanScore = meanScore,
            sharpnessScore = sharpness
        )
    }

    /**
     * Estimates image sharpness as the **median of per-tile Laplacian variance**.
     *
     * Plain *global* Laplacian variance fails on partially-focused photos: a small high-contrast
     * region (e.g. a laptop screen's scan-lines) spikes the global number even when most of the
     * frame is motion-blurred, so a blurry capture wrongly reads as sharp. Splitting the image into
     * an [SHARPNESS_TILES]×[SHARPNESS_TILES] grid and taking the **median** tile variance makes the
     * score robust: a few sharp tiles can't drag the median up if the bulk of the frame is blurry.
     */
    private fun calculateSharpness(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Grayscale conversion
        val gray = FloatArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            gray[i] = (0.299f * ((p shr 16) and 0xFF) +
                0.587f * ((p shr 8) and 0xFF) +
                0.114f * (p and 0xFF))
        }

        // Apply 3x3 Laplacian Kernel: [0,1,0 / 1,-4,1 / 0,1,0]
        val laplacian = FloatArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                laplacian[idx] = gray[idx - 1] + gray[idx + 1] +
                    gray[idx - width] + gray[idx + width] - 4 * gray[idx]
            }
        }

        // Per-tile variance over an NxN grid, then median across tiles.
        val tiles = SHARPNESS_TILES
        val variances = FloatArray(tiles * tiles)
        var t = 0
        for (ty in 0 until tiles) {
            val y0 = ty * height / tiles
            val y1 = (ty + 1) * height / tiles
            for (tx in 0 until tiles) {
                val x0 = tx * width / tiles
                val x1 = (tx + 1) * width / tiles
                var sum = 0f
                var count = 0
                for (y in y0 until y1) for (x in x0 until x1) {
                    sum += laplacian[y * width + x]; count++
                }
                val mean = sum / count
                var v = 0f
                for (y in y0 until y1) for (x in x0 until x1) {
                    val d = laplacian[y * width + x] - mean
                    v += d * d
                }
                variances[t++] = v / count
            }
        }
        variances.sort()
        val n = variances.size
        return (variances[n / 2 - 1] + variances[n / 2]) / 2f
    }

    private fun writeNormalizedInput(bitmap: Bitmap) {
        inputBuffer.rewind()
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            inputBuffer.putFloat(r / NORM - 1f)
            inputBuffer.putFloat(g / NORM - 1f)
            inputBuffer.putFloat(b / NORM - 1f)
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
        private const val MODEL_ASSET = "nima_mobilenet_technical.tflite"
        private const val INPUT_SIZE = 224
        private const val CHANNELS = 3
        private const val NUM_BUCKETS = 10
        private const val NORM = 127.5f

        /**
         * The NIMA Technical model tends to score even clear photos around 4.3 - 4.8.
         * We set this to 4.2 to be inclusive of well-lit subjects.
         */
        const val NIMA_THRESHOLD = 4.2f

        /** Grid resolution for the tile-median sharpness metric (8×8 = 64 tiles on a 224px image). */
        private const val SHARPNESS_TILES = 8

        /**
         * Median tile Laplacian-variance threshold (see [calculateSharpness]). Measured on the
         * bundled samples: sharp photos sit at ~190–550, fully-blurred ones at ~3, and a
         * mostly-blurred frame with one sharp corner lands in the blurry bulk (well below this).
         */
        const val SHARPNESS_THRESHOLD = 100.0f
    }
}
