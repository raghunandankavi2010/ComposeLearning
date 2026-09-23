package com.example.composelearning.fastimages

import android.content.Context
import androidx.compose.runtime.Immutable
import coil3.ImageLoader
import coil3.decode.BlackholeDecoder
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Parallelism per lane. Kept small on purpose: the visible cells and the
 * prefetches share one OkHttp dispatcher, so an unbounded prefetch fan-out
 * would push the images the user is *looking at* to the back of the queue.
 */
private const val EAGER_PARALLELISM = 3
private const val DISK_PARALLELISM = 2

/**
 * Which item indices to warm, split by how aggressively.
 *
 * Both lists are ordered nearest-first, because the item about to scroll into
 * view is worth more than the one twelve rows down.
 */
@Immutable
data class PrefetchPlan(
    val eager: List<Int>,
    val diskOnly: List<Int>
)

/**
 * Two-tier prefetch window, biased in the direction of travel.
 *
 * Symmetric prefetching (n items either side) wastes roughly half the
 * bandwidth: a user scrolling down almost never reverses far enough to need
 * what is above the viewport. Watching the direction and only spending ahead is
 * the single cheapest improvement over "prefetch everything nearby".
 */
fun prefetchPlan(
    firstVisible: Int,
    lastVisible: Int,
    itemCount: Int,
    scrollingDown: Boolean,
    eagerAhead: Int,
    diskAhead: Int
): PrefetchPlan {
    if (itemCount == 0 || (eagerAhead <= 0 && diskAhead <= 0)) {
        return PrefetchPlan(emptyList(), emptyList())
    }
    val valid = 0 until itemCount
    return if (scrollingDown) {
        val eagerFrom = lastVisible + 1
        val diskFrom = eagerFrom + eagerAhead
        PrefetchPlan(
            eager = (eagerFrom until diskFrom).filter { it in valid },
            diskOnly = (diskFrom until diskFrom + diskAhead).filter { it in valid }
        )
    } else {
        val eagerFrom = firstVisible - 1
        val diskFrom = eagerFrom - eagerAhead
        PrefetchPlan(
            eager = (eagerFrom downTo diskFrom + 1).filter { it in valid },
            diskOnly = (diskFrom downTo diskFrom - diskAhead + 1).filter { it in valid }
        )
    }
}

/**
 * Warms images the user has not asked for yet.
 *
 * Two tiers, because they cost different things:
 *  - **eager**: a normal request. The bytes land in the disk cache *and* the
 *    decoded bitmap lands in the memory cache, so the tile paints on its very
 *    first frame with no crossfade and no placeholder.
 *  - **disk only**: the same fetch with [BlackholeDecoder] and the memory cache
 *    switched off. Coil downloads and stores the bytes but never allocates a
 *    bitmap — which is the whole point, since a decoded far-away image would
 *    evict the near ones from the memory cache and burn CPU during a fling.
 *
 * [submit] replaces the previous window: cancelling the old [Job] cancels every
 * in-flight prefetch under it, so reversing direction stops paying for the
 * images that are now behind the user.
 *
 * The window sizes are not constants — they come from [DeliveryProfile], which
 * shrinks them on a slow link, halves the eager lane on a low-tier device
 * (where decode CPU, not bandwidth, is the ceiling) and takes them to zero when
 * Data Saver is on. Prefetching is a bet placed with someone else's data
 * allowance, and the size of the bet has to match the odds.
 */
class ImagePrefetcher(
    private val context: Context,
    private val loader: ImageLoader,
    private val scope: CoroutineScope
) {

    private var job: Job? = null

    /** Called from the scroll observer on the main thread. */
    fun submit(
        eager: List<ImageVariant>,
        diskOnly: List<ImageVariant>,
        profile: DeliveryProfile
    ) {
        job?.cancel()
        if (eager.isEmpty() && diskOnly.isEmpty()) {
            job = null
            return
        }
        job = scope.launch {
            launch { lane(eager, EAGER_PARALLELISM, decode = true, profile = profile) }
            launch { lane(diskOnly, DISK_PARALLELISM, decode = false, profile = profile) }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    private suspend fun lane(
        targets: List<ImageVariant>,
        parallelism: Int,
        decode: Boolean,
        profile: DeliveryProfile
    ) = coroutineScope {
        val gate = Semaphore(parallelism)
        targets.forEach { target ->
            launch { gate.withPermit { warm(target, decode, profile) } }
        }
    }

    private suspend fun warm(target: ImageVariant, decode: Boolean, profile: DeliveryProfile) {
        if (decode && isAlreadyDecoded(target)) return
        val request = ImageRequest.Builder(context)
            .applyVariant(target, profile)
            .apply {
                extras.set(PrefetchExtraKey, true)
                if (!decode) {
                    // Fetch the bytes into the disk cache without ever
                    // allocating a bitmap for them.
                    decoderFactory(BlackholeDecoder.Factory())
                    memoryCachePolicy(CachePolicy.DISABLED)
                }
            }
            .build()
        // execute() returns an ErrorResult instead of throwing, so a 404 on a
        // speculative fetch can never take the feed down with it.
        loader.execute(request)
    }

    /**
     * Best-effort skip. A hit here saves building and dispatching the request;
     * a miss is harmless, because the normal pipeline would have found the
     * entry anyway.
     */
    private fun isAlreadyDecoded(target: ImageVariant): Boolean {
        val key = target.cacheKey ?: return false
        return loader.memoryCache?.get(MemoryCache.Key(key)) != null
    }
}
