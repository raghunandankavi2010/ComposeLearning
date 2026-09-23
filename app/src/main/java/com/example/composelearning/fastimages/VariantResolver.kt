package com.example.composelearning.fastimages

import coil3.ImageLoader
import coil3.memory.MemoryCache
import java.util.concurrent.ConcurrentHashMap

/**
 * The single place that answers "which bytes does this image need, right now".
 *
 * Every caller goes through here — visible tiles and the prefetcher both — which
 * is what guarantees a prefetch warms the exact cache key the tile will later
 * ask for. A prefetch that warms a *different* key is worse than no prefetch:
 * it spends the bandwidth twice and reports a 100% hit-rate miss.
 *
 * It also solves a problem the delivery policy creates. Because the variant URL
 * encodes the network tier's quality and bucket, a network downgrade changes the
 * URL, which changes the cache key, which would re-download images the user is
 * *already looking at* — at lower quality. So:
 *
 *  - **never downgrade something already decoded.** If a richer variant for this
 *    image is still resident in the memory cache, serve that and skip the
 *    request entirely.
 *  - **do allow upgrades.** If the link improved, fetching the crisper variant
 *    is a real gain and the user paid for the bandwidth they now have.
 */
class VariantResolver internal constructor(private val loader: ImageLoader) {

    /** seed → richest variant requested so far this session. */
    private val richestSeen = ConcurrentHashMap<String, ImageVariant>()

    fun resolve(
        seed: String,
        targetWidthPx: Int,
        aspectRatio: Float,
        profile: DeliveryProfile,
        cdnResize: Boolean,
        downgradeSteps: Int = 0
    ): ImageVariant {
        if (!cdnResize) return ImageCdn.master(seed, targetWidthPx, aspectRatio)

        val effective = profile.downgraded(downgradeSteps)
        val wanted = ImageCdn.variant(seed, targetWidthPx, aspectRatio, effective, downgradeSteps)

        if (downgradeSteps == 0) {
            val previous = richestSeen[seed]
            // Only reuse if it is genuinely better *and* genuinely still there.
            // Checking residency matters: the ledger remembers what we asked
            // for, the memory cache knows what survived eviction.
            if (previous != null && previous.richness > wanted.richness && isResident(previous)) {
                return previous
            }
        }

        richestSeen.merge(seed, wanted) { old, new -> if (old.richness >= new.richness) old else new }
        return wanted
    }

    /**
     * Dropped when the memory cache is cleared, so the ledger cannot keep
     * pointing at variants that no longer exist.
     */
    fun forget() = richestSeen.clear()

    private fun isResident(variant: ImageVariant): Boolean {
        val key = variant.cacheKey ?: return false
        return loader.memoryCache?.get(MemoryCache.Key(key)) != null
    }
}
