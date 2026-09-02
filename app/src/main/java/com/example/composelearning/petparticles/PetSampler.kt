package com.example.composelearning.petparticles

import kotlin.random.Random

/**
 * A pet turned into a point cloud, stored as a **structure of arrays** and
 * already sorted by tone so that every tone owns one contiguous slice of the
 * arrays. That slice is exactly what a single `Canvas.drawPoints(pts, offset,
 * count, paint)` call needs — the sort is what buys us "one draw call per
 * colour" instead of "one draw call per particle".
 *
 * Coordinates are kept in **unit space** (0..1 inside the artwork's square) so
 * the same cloud can be fitted onto any canvas size without re-sampling.
 *
 * @param bucketStart size `palette.size + 1`; tone *b* covers `[bucketStart[b],
 *   bucketStart[b + 1])`.
 * @param tail 1 for particles belonging to the tail, used to drive the wag.
 */
class PetCloud(
    val pet: Pet,
    val count: Int,
    val ux: FloatArray,
    val uy: FloatArray,
    val tail: ByteArray,
    val bucketStart: IntArray,
    val palette: List<FurTone>,
    val tailPivotX: Float,
    val tailPivotY: Float
)

/**
 * Turns [PetArtwork]'s rasterised portraits into clouds.
 *
 * The method is a **jittered grid**: the artwork is rendered at exactly the
 * resolution we want particles at, so one opaque mask pixel becomes at most one
 * particle. That gives even coverage for free — uniform random sampling would
 * clump and leave holes — and the per-sample jitter breaks up the grid so the
 * result looks organic rather than like graph paper.
 */
object PetSampler {

    /** Mask pixels dimmer than this are treated as background / soft fringe. */
    private const val ALPHA_CUTOFF = 140

    /**
     * Sample every pet at the same [maskSize] and trim them all to a common
     * particle count, which is what lets particle *i* morph from one pet to the
     * other without any bookkeeping.
     */
    fun sampleAll(
        pets: List<Pet>,
        maskSize: Int,
        maxCount: Int,
        rng: Random
    ): List<PetCloud> {
        val raw = pets.map { rawSample(it, maskSize) }
        val count = raw.minOf { it.n }.coerceAtMost(maxCount).coerceAtLeast(1)
        return raw.map { it.toCloud(count, maskSize, rng) }
    }

    /** Opaque mask pixels in raster order, before decimation and sorting. */
    private class RawSamples(
        val pet: Pet,
        val n: Int,
        val xs: FloatArray,
        val ys: FloatArray,
        val tone: IntArray,
        val tail: ByteArray
    ) {
        fun toCloud(target: Int, maskSize: Int, rng: Random): PetCloud {
            val palette = PetArtwork.palette(pet)
            val k = palette.size

            // Counting sort into tone buckets. Two passes, no comparisons, no
            // allocation beyond the output arrays: O(n + k).
            val counts = IntArray(k)
            for (i in 0 until target) counts[tone[pick(i, target)]]++
            val bucketStart = IntArray(k + 1)
            for (b in 0 until k) bucketStart[b + 1] = bucketStart[b] + counts[b]
            val cursor = bucketStart.copyOf()

            val ux = FloatArray(target)
            val uy = FloatArray(target)
            val tailOut = ByteArray(target)
            val inv = 1f / maskSize
            for (i in 0 until target) {
                val src = pick(i, target)
                val dst = cursor[tone[src]]++
                // ±half a cell of jitter: enough to hide the lattice, not
                // enough to blur the silhouette.
                ux[dst] = (xs[src] + rng.nextFloat() - 0.5f) * inv
                uy[dst] = (ys[src] + rng.nextFloat() - 0.5f) * inv
                tailOut[dst] = tail[src]
            }

            val pivot = PetArtwork.tailPivot(pet)
            return PetCloud(
                pet = pet,
                count = target,
                ux = ux,
                uy = uy,
                tail = tailOut,
                bucketStart = bucketStart,
                palette = palette,
                tailPivotX = pivot[0],
                tailPivotY = pivot[1]
            )
        }

        /**
         * Stride decimation in raster order. Picking every `n/target`-th sample
         * keeps the thinning spread evenly over the whole body; dropping a
         * random subset instead would leave visible bald patches.
         */
        private fun pick(i: Int, target: Int): Int =
            if (target >= n) i else (i.toLong() * n / target).toInt()
    }

    private fun rawSample(pet: Pet, maskSize: Int): RawSamples {
        val palette = PetArtwork.palette(pet)
        val full = PetArtwork.rasterise(pet, maskSize)
        val tailMask = PetArtwork.rasterise(pet, maskSize, tailOnly = true)

        val pixels = IntArray(maskSize * maskSize)
        full.getPixels(pixels, 0, maskSize, 0, 0, maskSize, maskSize)
        val tailPixels = IntArray(maskSize * maskSize)
        tailMask.getPixels(tailPixels, 0, maskSize, 0, 0, maskSize, maskSize)
        full.recycle()
        tailMask.recycle()

        // Pre-split the palette into channels so the nearest-tone search is a
        // flat loop over primitives instead of repeated bit twiddling.
        val pr = IntArray(palette.size)
        val pg = IntArray(palette.size)
        val pb = IntArray(palette.size)
        palette.forEachIndexed { i, tone ->
            pr[i] = (tone.argb shr 16) and 0xFF
            pg[i] = (tone.argb shr 8) and 0xFF
            pb[i] = tone.argb and 0xFF
        }

        val cap = maskSize * maskSize
        val xs = FloatArray(cap)
        val ys = FloatArray(cap)
        val tone = IntArray(cap)
        val tail = ByteArray(cap)
        var n = 0

        for (y in 0 until maskSize) {
            val row = y * maskSize
            for (x in 0 until maskSize) {
                val argb = pixels[row + x]
                if ((argb ushr 24) < ALPHA_CUTOFF) continue
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF

                // Snap to the nearest palette entry. Flat-filled regions match
                // exactly; only anti-aliased seams between two layers need the
                // nearest-neighbour fallback.
                var best = 0
                var bestD = Int.MAX_VALUE
                for (t in pr.indices) {
                    val dr = r - pr[t]
                    val dg = g - pg[t]
                    val db = b - pb[t]
                    val d = dr * dr + dg * dg + db * db
                    if (d < bestD) {
                        bestD = d
                        best = t
                    }
                }

                xs[n] = x + 0.5f
                ys[n] = y + 0.5f
                tone[n] = best
                tail[n] = if ((tailPixels[row + x] ushr 24) >= ALPHA_CUTOFF) 1 else 0
                n++
            }
        }
        return RawSamples(pet, n, xs, ys, tone, tail)
    }
}
