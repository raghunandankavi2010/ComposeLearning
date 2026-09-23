package com.example.composelearning.fastimages

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

/**
 * Trick number one: the phone never downloads the master image.
 *
 * Catalogs hold one high-resolution master per dish and the CDN derives a
 * variant per request:
 *
 * ```
 * https://media.example.com/.../<image-id>?w=320&h=240&q=60&fm=webp&dpr=2
 * ```
 *
 * Three independent wins stack up there:
 *  - **w/h** caps the pixels at what the cell actually paints. A 1200px master
 *    dropped into a 320px grid cell is ~14x the bytes for zero visible gain.
 *  - **q=40..74** — near-linear in bytes, and at thumbnail size the top of that
 *    range is indistinguishable from the bottom to most eyes.
 *  - **fm=webp** (or avif) is another 25-35% smaller than JPEG at equal quality.
 *
 * On top of that the width is quantised into **buckets**. If every device asked
 * for its exact cell width (`w=347`, `w=352`, `w=360`...) the CDN edge cache and
 * the on-device disk cache would both miss constantly. Rounding up to a fixed
 * ladder means a handful of variants serve the whole device population, so the
 * edge stays hot and the derivative cache at the origin stays small. Bucketing
 * is an origin-protection strategy, not a client micro-optimisation.
 *
 * Which bucket and which quality get asked for is [DeliveryPolicy]'s decision.
 *
 * [Picsum](https://picsum.photos) plays the CDN here because it resizes by path
 * (`/seed/<seed>/<w>/<h>`) and encodes WebP via the `.webp` extension, so the
 * request shape is real without needing a private CDN key.
 *
 * One honest caveat: **picsum has no quality knob.** `q` is carried in the URL
 * and in the cache key because that is the correct shape, and [realCdnQuery]
 * prints what a production CDN would receive — but the demo's byte counter only
 * moves when the *bucket* or the *format* changes. On a real CDN quality is
 * worth roughly another 2x by itself, so the savings measured here are a
 * conservative floor.
 */
object ImageCdn {

    private const val BASE = "https://picsum.photos"

    /**
     * The width ladder. Requests round *up* to the next bucket so an image is
     * never upscaled, and so that near-identical cell widths collapse onto one
     * cache key.
     */
    private val WIDTH_BUCKETS = intArrayOf(96, 160, 240, 320, 400, 480, 640, 800, 1080)

    /** Width of the deliberately un-optimised "download the master" variant. */
    private const val MASTER_WIDTH = 1200

    fun bucketWidth(targetWidthPx: Int): Int =
        WIDTH_BUCKETS.firstOrNull { it >= targetWidthPx } ?: WIDTH_BUCKETS.last()

    /** Steps [steps] rungs down the ladder from whatever bucket [widthPx] sits in. */
    fun bucketDown(widthPx: Int, steps: Int): Int {
        val index = WIDTH_BUCKETS.indexOfFirst { it >= widthPx }
            .takeIf { it >= 0 } ?: WIDTH_BUCKETS.lastIndex
        return WIDTH_BUCKETS[(index - steps).coerceAtLeast(0)]
    }

    /**
     * The optimised variant: dimensions capped by [DeliveryProfile.bucketCapPx],
     * quality from the profile, WebP, and a cache key encoding all three — so
     * two requests that agree on the key genuinely agree on the bytes.
     *
     * The bytes arrive at exactly the bucket size, so that is also the decode
     * size. Source, decode size and cache key all agree, which is what makes a
     * cache hit a real hit.
     */
    fun variant(
        seed: String,
        targetWidthPx: Int,
        aspectRatio: Float,
        profile: DeliveryProfile,
        downgradeSteps: Int = 0
    ): ImageVariant {
        // The layout asks for targetWidthPx; the profile decides how much of
        // that request the device and the link can actually afford.
        val width = bucketWidth(minOf(targetWidthPx, profile.bucketCapPx))
        val height = heightFor(width, aspectRatio)

        // WebP is 25-35% smaller but can fail with "unimplemented" on some
        // emulators and newer API levels. If we have already failed once,
        // we fallback to JPEG (no extension) which is safe everywhere.
        val useWebP = downgradeSteps == 0
        val extension = if (useWebP) ".webp" else ""
        val formatLabel = if (useWebP) "webp" else "jpg"

        return ImageVariant(
            url = "$BASE/seed/$seed/$width/$height$extension",
            cacheKey = "$seed@${width}x$height#$formatLabel-q${profile.quality}",
            sourceWidthPx = width,
            sourceHeightPx = height,
            decodeWidthPx = width,
            decodeHeightPx = height,
            quality = profile.quality,
            label = "${width}x$height q${profile.quality}"
        )
    }

    /**
     * The naive variant: the full-size JPEG master. Coil still downsamples it
     * to [targetWidthPx] while decoding, so the *pixels* on screen are
     * identical — every one of those extra bytes crossed the network for
     * nothing.
     *
     * No explicit cache key here, because the decode size is the only thing
     * distinguishing two requests for the same master; letting Coil derive the
     * key from the URL plus the resolved size is the correct behaviour.
     */
    fun master(seed: String, targetWidthPx: Int, aspectRatio: Float): ImageVariant {
        val sourceHeight = heightFor(MASTER_WIDTH, aspectRatio)
        return ImageVariant(
            url = "$BASE/seed/$seed/$MASTER_WIDTH/$sourceHeight",
            cacheKey = null,
            sourceWidthPx = MASTER_WIDTH,
            sourceHeightPx = sourceHeight,
            decodeWidthPx = targetWidthPx,
            decodeHeightPx = heightFor(targetWidthPx, aspectRatio),
            quality = 0,
            label = "${MASTER_WIDTH}x$sourceHeight jpg"
        )
    }

    /** The query string the same variant would carry on a production CDN. */
    fun realCdnQuery(variant: ImageVariant, dpr: Int = 1): String =
        "?w=${variant.sourceWidthPx}&h=${variant.sourceHeightPx}" +
            "&q=${variant.quality}&fm=webp&dpr=$dpr"

    private fun heightFor(width: Int, aspectRatio: Float): Int =
        (width / aspectRatio).roundToInt().coerceAtLeast(1)
}

/**
 * One concrete image request: where the bytes come from, how many pixels they
 * carry, what size they are decoded to, and the key they are cached under.
 *
 * Keeping all of that in a single value is the point. A prefetch that warms a
 * different key than the one the visible cell later asks for is worse than no
 * prefetch at all — it spends the bandwidth twice. Since Coil's derived cache
 * key includes the decode size, the decode size has to be part of the shared
 * description too, not something each call site resolves for itself.
 *
 * A `null` [cacheKey] means "let Coil derive it".
 */
@Immutable
data class ImageVariant(
    val url: String,
    val cacheKey: String?,
    val sourceWidthPx: Int,
    val sourceHeightPx: Int,
    val decodeWidthPx: Int,
    val decodeHeightPx: Int,
    /** The `q=` this variant was built with. 0 for the un-optimised master. */
    val quality: Int,
    val label: String
) {
    /** Ranking used to decide whether an already-cached variant beats a new request. */
    val richness: Long
        get() = sourceWidthPx.toLong() * sourceHeightPx * quality.coerceAtLeast(1)
}
