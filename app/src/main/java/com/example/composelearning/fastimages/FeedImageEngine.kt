package com.example.composelearning.fastimages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.decode.BitmapFactoryDecoder
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.bitmapConfig
import coil3.request.crossfade
import coil3.size.Precision
import coil3.util.DebugLogger
import com.example.composelearning.BuildConfig
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.Path.Companion.toOkioPath

private const val DISK_CACHE_DIR = "fast_image_feed"

/**
 * Disk cache is sized in absolute bytes, not as a share of the heap: it holds
 * *compressed* bytes, so it is bounded by storage, not by memory pressure.
 */
private const val DISK_CACHE_BYTES = 96L * 1024 * 1024

/**
 * OkHttp defaults to 5 concurrent requests *per host*, and an image CDN is one
 * host. On a 2-column grid that means the 6th tile waits for the 1st even
 * though the radio is idle. This is the least-known and highest-yield knob in
 * the whole file.
 */
private const val HTTP_MAX_REQUESTS = 32
private const val HTTP_MAX_REQUESTS_PER_HOST = 12

/** Keep sockets around: a TLS handshake costs more than a 15 KB WebP. */
private const val HTTP_IDLE_CONNECTIONS = 8
private const val HTTP_KEEP_ALIVE_MINUTES = 5L

/**
 * Long enough to hide a network arrival, short enough not to feel like a
 * transition. Coil skips it entirely for memory-cache hits, which is why a
 * prefetched tile appears to have always been there.
 */
private const val CROSSFADE_MS = 140

/** Set by the retry path so the interceptor can grant a longer budget. */
internal const val RETRY_ATTEMPT_HEADER = "X-Feed-Retry"

/**
 * Everything the feed needs to load an image, in one `@Immutable` holder.
 *
 * The annotation is the point: `ImageLoader` is an interface Compose can prove
 * nothing about, so passing it to a tile as a bare parameter would make that
 * tile *unskippable* and force it to recompose on every scroll frame. Wrapping
 * third-party types in a holder you can honestly annotate is the standard fix.
 */
@Immutable
class FeedImageEngine internal constructor(
    val loader: ImageLoader,
    val metrics: ImageLoadMetrics,
    val prefetcher: ImagePrefetcher,
    val resolver: VariantResolver,
    val monitor: NetworkMonitor
) {
    /**
     * Applies the device half of the policy without rebuilding anything.
     *
     * `MemoryCache.maxSize` is mutable in Coil 3, which is what makes this
     * possible — the alternative, rebuilding the loader, would discard the
     * memory cache, the disk cache handle and the connection pool, i.e. it
     * would cost far more than it saves.
     */
    fun applyDeviceBudget(bytes: Long) {
        loader.memoryCache?.maxSize = bytes
    }

    /**
     * What `ComponentCallbacks2.onTrimMemory` does for real. Coil already
     * registers its own system callbacks and trims on memory pressure, so this
     * exists only to make that path observable on demand.
     */
    fun simulateMemoryPressure() {
        loader.memoryCache?.clear()
        resolver.forget()
    }
}

@Immutable
internal class FeedImageStack(
    val loader: ImageLoader,
    val metrics: ImageLoadMetrics
)

/**
 * Process-scoped loader, exactly as a production app would do it. An
 * `ImageLoader` owns the memory cache, the disk cache and the OkHttp connection
 * pool; rebuilding one per screen throws all three away and turns every return
 * visit into a cold start.
 *
 * Note which half of the policy is baked in here and which is not. The
 * **device** tier is static for the life of the process, so it can configure
 * the loader. The **network** tier changes while the user scrolls, so it is
 * only ever allowed to influence individual requests.
 */
internal object FeedImageLoaders {

    @Volatile
    private var stack: FeedImageStack? = null

    fun get(context: Context, devicePolicy: DevicePolicy): FeedImageStack =
        stack ?: synchronized(this) {
            stack ?: build(context.applicationContext, devicePolicy).also { stack = it }
        }

    private fun build(context: Context, devicePolicy: DevicePolicy): FeedImageStack {
        val metrics = ImageLoadMetrics()
        val monitor = FeedNetworkMonitors.get(context)
        val loader = ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(feedHttpClient(metrics, monitor)))
                // We lead with BitmapFactoryDecoder as the primary engine.
                // WebP decoding issues are handled via URL fallback to JPEG
                // in ImageCdn.
                add(BitmapFactoryDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(devicePolicy.memoryCacheBytes)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(DISK_CACHE_DIR).toOkioPath())
                    .maxSizeBytes(DISK_CACHE_BYTES)
                    .build()
            }
            // The URL already encodes the exact bucket dimensions, so let the
            // decoder keep its nearest power-of-two sample instead of running a
            // second scaling pass to land on an exact pixel count.
            .precision(Precision.INEXACT)
            .crossfade(CROSSFADE_MS)
            .eventListener(metrics)
            // Without this, a failing load is silent: Coil swallows the throwable
            // into an ErrorResult and the UI just shows a placeholder forever.
            // `adb logcat -s coil3` is the difference between diagnosing and guessing.
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build()
        metrics.attach(loader)
        return FeedImageStack(loader, metrics)
    }

    private fun feedHttpClient(
        metrics: ImageLoadMetrics,
        monitor: NetworkMonitor
    ): OkHttpClient = OkHttpClient.Builder()
        .dispatcher(
            Dispatcher().apply {
                maxRequests = HTTP_MAX_REQUESTS
                maxRequestsPerHost = HTTP_MAX_REQUESTS_PER_HOST
            }
        )
        .connectionPool(
            ConnectionPool(HTTP_IDLE_CONNECTIONS, HTTP_KEEP_ALIVE_MINUTES, TimeUnit.MINUTES)
        )
        .eventListener(ImageTransportListener(metrics, monitor))
        // An application interceptor, not a network one: by the time a network
        // interceptor runs the connection already exists, so a connect timeout
        // set there would have nothing left to govern.
        .addInterceptor(AdaptiveTimeoutInterceptor())
        .allowInterceptingProxyInDebug()
        .build()
}

/**
 * Measures the transfer, not the request.
 *
 * `responseBodyStart` to `responseBodyEnd` brackets **body bytes only** — DNS,
 * TCP, TLS and time-to-first-byte are all latency, and folding them into a
 * throughput figure would make a fast link to a distant server look slow. The
 * byte count is also exact here, which `Content-Length` is not: it is absent
 * under chunked encoding and lies under transparent compression.
 */
private class ImageTransportListener(
    private val metrics: ImageLoadMetrics,
    private val monitor: NetworkMonitor
) : EventListener() {

    private val bodyStartedAt = ConcurrentHashMap<Call, Long>()

    override fun responseBodyStart(call: Call) {
        bodyStartedAt[call] = System.nanoTime()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        metrics.addNetworkBytes(byteCount)
        val startedAt = bodyStartedAt.remove(call) ?: return
        monitor.sampleTransfer(byteCount, (System.nanoTime() - startedAt) / 1_000_000L)
    }

    override fun callEnd(call: Call) {
        bodyStartedAt.remove(call)
    }

    override fun callFailed(call: Call, ioe: java.io.IOException) {
        bodyStartedAt.remove(call)
    }

    override fun canceled(call: Call) {
        bodyStartedAt.remove(call)
    }
}

/**
 * Per-call timeouts taken from the live [DeliveryProfile].
 *
 * A single client-wide timeout cannot serve both cases: a budget that is right
 * on WiFi is punitive on a congested cell, where it fires on connections that
 * are slow rather than broken and triggers a downgrade storm.
 *
 * Connect and read are separated deliberately. Connecting (DNS + TCP + TLS) is a
 * fixed cost that does not scale with throughput; the read budget has to absorb
 * the server's think time. Collapsing them into one number — which this file did
 * on its first pass — makes the tighter of the two constraints govern both.
 *
 * `Interceptor.Chain.withReadTimeout` is what makes this per-request instead of
 * per-client. Retries announce themselves with [RETRY_ATTEMPT_HEADER], which is
 * stripped before the request leaves the device — it must never reach the CDN,
 * or it would fragment the edge cache key.
 */
private class AdaptiveTimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val profile = ActiveProfile.current
        val isRetry = chain.request().header(RETRY_ATTEMPT_HEADER) != null
        val request = if (isRetry) {
            chain.request().newBuilder().removeHeader(RETRY_ATTEMPT_HEADER).build()
        } else {
            chain.request()
        }
        val budgetMs = when {
            profile == null -> chain.readTimeoutMillis()
            isRetry -> profile.retryTimeoutMs
            else -> profile.firstAttemptTimeoutMs
        }
        val connectMs = profile?.connectTimeoutMs ?: chain.connectTimeoutMillis()
        return chain
            .withReadTimeout(budgetMs, TimeUnit.MILLISECONDS)
            .withConnectTimeout(connectMs, TimeUnit.MILLISECONDS)
            .proceed(request)
    }
}

/**
 * Debug-only escape hatch for networks that intercept TLS.
 *
 * A corporate proxy (Zscaler, Netskope, a Charles/mitmproxy session) re-signs
 * every certificate with its own root. That root is not in Android's system
 * trust store, so the handshake fails with:
 *
 * ```
 * java.security.cert.CertPathValidatorException:
 *     Trust anchor for certification path not found
 * ```
 *
 * The clean fix is to install the proxy's root CA on the device — this project's
 * `network_security_config.xml` already has a `<debug-overrides>` block trusting
 * user-installed CAs, so that alone makes it work with validation intact:
 * Settings → Security → Encryption &amp; credentials → Install a certificate → CA.
 *
 * This is the blunt fix instead. It is **hard-gated on [BuildConfig.DEBUG]**, so
 * release builds keep full certificate validation and pinning behaviour — a
 * trust-all client that survives into a release build is a
 * man-in-the-middle vulnerability, not a convenience. Note also that it applies
 * only to this demo's own loader; the rest of the app is unaffected.
 */
@SuppressLint("CustomX509TrustManager", "BadHostnameVerifier")
private fun OkHttpClient.Builder.allowInterceptingProxyInDebug(): OkHttpClient.Builder {
    if (!BuildConfig.DEBUG) return this
    val trustAll = object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
    }
    sslSocketFactory(sslContext.socketFactory, trustAll)
    hostnameVerifier { _, _ -> true }
    return this
}

@Composable
fun rememberFeedImageEngine(devicePolicy: DevicePolicy): FeedImageEngine {
    val context = LocalContext.current
    // Tied to the composition: leaving the screen cancels every speculative
    // fetch still in the air, while the loader and its caches survive.
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        val stack = FeedImageLoaders.get(context, devicePolicy)
        FeedImageEngine(
            loader = stack.loader,
            metrics = stack.metrics,
            prefetcher = ImagePrefetcher(context.applicationContext, stack.loader, scope),
            resolver = VariantResolver(stack.loader),
            monitor = FeedNetworkMonitors.get(context)
        )
    }
}

/**
 * Applies an [ImageVariant] to a request. Used by both the visible tiles and
 * the prefetcher, via [VariantResolver], so the two can never disagree about
 * the cache key.
 *
 * Declaring the size up front also means the request does not wait for layout
 * to resolve it, so the fetch starts one frame earlier.
 */
internal fun ImageRequest.Builder.applyVariant(
    variant: ImageVariant,
    profile: DeliveryProfile,
    retryAttempt: Int = 0
): ImageRequest.Builder = apply {
    data(variant.url)
    size(variant.decodeWidthPx, variant.decodeHeightPx)
    if (variant.cacheKey != null) {
        memoryCacheKey(variant.cacheKey)
    }
    if (profile.useRgb565) {
        // Half the bytes per pixel, and safe here because dish photos are
        // opaque. The cost is giving up Coil's default hardware bitmap, which
        // keeps pixels out of the Java heap entirely — so this is a genuine
        // trade, not a free win. See DevicePolicy for the reasoning.
        bitmapConfig(Bitmap.Config.RGB_565)
    }
    if (retryAttempt > 0) {
        httpHeaders(
            NetworkHeaders.Builder()
                .set(RETRY_ATTEMPT_HEADER, retryAttempt.toString())
                .build()
        )
    }
}
