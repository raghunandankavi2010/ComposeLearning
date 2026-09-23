package com.example.composelearning.fastimages

import androidx.compose.runtime.Immutable
import coil3.EventListener
import coil3.Extras
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.getExtra
import coil3.memory.MemoryCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.request.SuccessResult
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Marks a request as a speculative prefetch rather than something a visible
 * cell is waiting for, so the metrics can tell the two apart.
 *
 * `Extras` is Coil 3's request-scoped bag of typed values. Using it instead of
 * a side map keeps the flag attached to the request through every pipeline
 * stage, and — unlike `memoryCacheKeyExtra` — it does *not* change the cache
 * key, so a prefetched image is found by the display request later.
 */
internal val PrefetchExtraKey = Extras.Key(false)

/**
 * An aggregate view of what the image pipeline is actually doing.
 *
 * The distinction that matters for "make the feed feel instant" is
 * [instantPercent]: the share of displayed images that never touched the
 * network. Prefetching and cache-key discipline both show up here.
 */
@Immutable
data class ImageMetricsSnapshot(
    val displayed: Int = 0,
    val inFlight: Int = 0,
    val fromMemory: Int = 0,
    val fromDisk: Int = 0,
    val fromNetwork: Int = 0,
    val failed: Int = 0,
    /**
     * Requests abandoned before they finished — almost always because the tile
     * scrolled out of composition. A healthy fling *should* produce these: it
     * is the proof that work is being thrown away instead of finished
     * pointlessly. Zero cancellations during fast scrolling means something is
     * holding requests alive after their view is gone.
     */
    val cancelled: Int = 0,
    /** Requests that fell back to a smaller variant after failing. */
    val downgrades: Int = 0,
    val prefetched: Int = 0,
    val networkBytes: Long = 0L,
    val avgFetchMs: Int = 0,
    val avgDecodeMs: Int = 0,
    val memoryCacheBytes: Long = 0L,
    val memoryCacheMaxBytes: Long = 0L
) {
    val instantPercent: Int
        get() = if (displayed == 0) 0 else (100 * (fromMemory + fromDisk)) / displayed
}

/**
 * Collects pipeline timings by implementing Coil's [EventListener].
 *
 * Deliberately *not* a `StateFlow` of its own: image events fire hundreds of
 * times during a fling, and pushing each one into snapshot state would make the
 * metrics panel the most-recomposed composable on screen — the measurement
 * would cause the jank it is measuring. Counters are plain atomics and the UI
 * polls [snapshot] on a slow timer instead.
 */
class ImageLoadMetrics : EventListener() {

    private val fetchStartedAt = Collections.synchronizedMap(IdentityHashMap<ImageRequest, Long>())
    private val decodeStartedAt = Collections.synchronizedMap(IdentityHashMap<ImageRequest, Long>())

    private val inFlight = AtomicInteger()
    private val fromMemory = AtomicInteger()
    private val fromDisk = AtomicInteger()
    private val fromNetwork = AtomicInteger()
    private val failed = AtomicInteger()
    private val cancelled = AtomicInteger()
    private val downgrades = AtomicInteger()
    private val prefetched = AtomicInteger()
    private val networkBytes = AtomicLong()
    private val fetchMsTotal = AtomicLong()
    private val fetchSamples = AtomicInteger()
    private val decodeMsTotal = AtomicLong()
    private val decodeSamples = AtomicInteger()

    @Volatile
    private var memoryCacheRef: MemoryCache? = null

    /** Lets the snapshot report live memory-cache occupancy. */
    fun attach(loader: ImageLoader) {
        memoryCacheRef = loader.memoryCache
    }

    /** Exact on-the-wire byte count, reported by OkHttp's own EventListener. */
    fun addNetworkBytes(bytes: Long) {
        networkBytes.addAndGet(bytes)
    }

    fun recordDowngrade() {
        downgrades.incrementAndGet()
    }

    fun snapshot(): ImageMetricsSnapshot {
        val cache = memoryCacheRef
        val memory = fromMemory.get()
        val disk = fromDisk.get()
        val network = fromNetwork.get()
        return ImageMetricsSnapshot(
            displayed = memory + disk + network,
            inFlight = inFlight.get(),
            fromMemory = memory,
            fromDisk = disk,
            fromNetwork = network,
            failed = failed.get(),
            cancelled = cancelled.get(),
            downgrades = downgrades.get(),
            prefetched = prefetched.get(),
            networkBytes = networkBytes.get(),
            avgFetchMs = average(fetchMsTotal, fetchSamples),
            avgDecodeMs = average(decodeMsTotal, decodeSamples),
            memoryCacheBytes = cache?.size ?: 0L,
            memoryCacheMaxBytes = cache?.maxSize ?: 0L
        )
    }

    fun reset() {
        listOf(
            inFlight, fromMemory, fromDisk, fromNetwork, failed, cancelled,
            downgrades, prefetched, fetchSamples, decodeSamples
        )
            .forEach { it.set(0) }
        listOf(networkBytes, fetchMsTotal, decodeMsTotal).forEach { it.set(0L) }
        fetchStartedAt.clear()
        decodeStartedAt.clear()
    }

    override fun onStart(request: ImageRequest) {
        inFlight.incrementAndGet()
    }

    override fun fetchStart(request: ImageRequest, fetcher: Fetcher, options: Options) {
        fetchStartedAt[request] = System.nanoTime()
    }

    override fun fetchEnd(request: ImageRequest, fetcher: Fetcher, options: Options, result: FetchResult?) {
        record(fetchStartedAt.remove(request), fetchMsTotal, fetchSamples)
    }

    override fun decodeStart(request: ImageRequest, decoder: Decoder, options: Options) {
        decodeStartedAt[request] = System.nanoTime()
    }

    override fun decodeEnd(request: ImageRequest, decoder: Decoder, options: Options, result: DecodeResult?) {
        record(decodeStartedAt.remove(request), decodeMsTotal, decodeSamples)
    }

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        inFlight.decrementAndGet()
        if (request.getExtra(PrefetchExtraKey)) {
            prefetched.incrementAndGet()
            return
        }
        when (result.dataSource) {
            DataSource.MEMORY_CACHE, DataSource.MEMORY -> fromMemory.incrementAndGet()
            DataSource.DISK -> fromDisk.incrementAndGet()
            DataSource.NETWORK -> fromNetwork.incrementAndGet()
        }
    }

    override fun onError(request: ImageRequest, result: ErrorResult) {
        inFlight.decrementAndGet()
        cleanUp(request)
        if (!request.getExtra(PrefetchExtraKey)) failed.incrementAndGet()
    }

    override fun onCancel(request: ImageRequest) {
        inFlight.decrementAndGet()
        cleanUp(request)
        if (!request.getExtra(PrefetchExtraKey)) cancelled.incrementAndGet()
    }

    private fun cleanUp(request: ImageRequest) {
        fetchStartedAt.remove(request)
        decodeStartedAt.remove(request)
    }

    private fun record(startedAt: Long?, total: AtomicLong, samples: AtomicInteger) {
        val start = startedAt ?: return
        total.addAndGet((System.nanoTime() - start) / 1_000_000L)
        samples.incrementAndGet()
    }

    private fun average(total: AtomicLong, samples: AtomicInteger): Int {
        val count = samples.get()
        return if (count == 0) 0 else (total.get() / count).toInt()
    }
}
