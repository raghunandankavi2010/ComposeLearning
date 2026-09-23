# Fast Image Feed — how Swiggy / Zomato / Blinkit make a photo grid feel instant

A 2-column food grid that loads its images the way the delivery apps do — and,
more importantly, the way they do it **for a cheap phone on a slow link**. Live
metrics panel, forceable device/network tiers, and toggles so every trick can be
switched off and measured.

> The system-design narrative, with diagrams, is in
> [`docs/image-delivery-system-design.pdf`](../../../../../../../../docs/image-delivery-system-design.pdf)
> at the repo root. This file is the code-level companion.

Route: `AnimScreen.FastImageFeed` → **Lists, Layouts & Pagers → Fast Image Feed**.
Images come from [picsum.photos](https://picsum.photos), which stands in for a
real image CDN because it resizes by path and encodes WebP on demand.

## The four things that actually make it fast

### 1. The phone never downloads the master image

`ImageCdn` builds a **variant** per surface instead of one URL per dish:

| | URL | bytes on the wire |
|---|---|---|
| naive | `/seed/dish-7/1200/900` (JPEG master) | ~150–250 KB |
| turbo | `/seed/dish-7/320/240.webp` | ~10–20 KB |

On a production CDN the same request carries
`?w=320&h=240&q=60&fm=webp&dpr=2` — `ImageCdn.realCdnQuery()` prints exactly
that string. Three independent wins stack: pixel count, quality (55–65 is
invisible at thumbnail size), and format (WebP/AVIF is another 25–35% under
JPEG).

The non-obvious part is **bucketing**. Widths round up to a fixed ladder
(`96, 160, 240, 320, 400, 480, 640, 800, 1080`) instead of using each device's
exact cell width. Exact widths would give every device population its own
variant and keep both the CDN edge cache and the on-device disk cache
permanently cold.

Note that the *category circles* ask for a 160px square while the *dish tiles*
ask for a 320px 4:3. Sizing per surface rather than per app is where most of
the "why are our images so big" problem actually lives.

### 2. A tuned `ImageLoader`, built once for the process

`FeedImageLoaders` (in `FeedImageEngine.kt`):

- **memory cache 25% of the heap** — decoded bitmaps, the difference between
  "appears instantly" and "fades in".
- **disk cache 96 MB** in its own directory — survives process death, so a
  return visit is a disk read, not a download.
- **`maxRequestsPerHost = 12`.** OkHttp defaults to **5 per host**, and an image
  CDN is one host. On a grid that means the 6th tile waits for the 1st even
  though the radio is idle. This is the least-known high-yield knob here.
- **connection pool kept warm** — a TLS handshake costs more than a 15 KB WebP.
- **`Precision.INEXACT`** — the URL already encodes the exact bucket, so the
  decoder can keep its nearest power-of-two sample instead of running a second
  scaling pass.
- **crossfade 140 ms.** Coil skips the crossfade entirely for memory-cache hits,
  which is why a prefetched tile looks like it was always there.

The loader is process-scoped on purpose. Rebuilding one per screen throws away
the memory cache, the disk cache *and* the connection pool, turning every
navigation into a cold start.

### 3. Two-tier prefetching in the direction of travel

`ImagePrefetcher` + `prefetchPlan()`:

- **eager (6 items)** — a normal request. Bytes land on disk *and* the decoded
  bitmap lands in the memory cache, so the tile paints on its first frame.
- **disk only (next 12 items)** — same fetch with `BlackholeDecoder` and
  `memoryCachePolicy(DISABLED)`. Coil downloads and stores the bytes but never
  allocates a bitmap. Decoding images 12 rows away would evict the near ones
  and burn CPU mid-fling.

Details that matter more than the window size:

- **Direction-biased.** Prefetching *n* items either side wastes roughly half
  the bandwidth; a user scrolling down almost never reverses that far.
- **`submit()` cancels the previous window.** Reversing direction immediately
  stops paying for what is now behind the user.
- **Settle delay (90 ms) + `collectLatest`.** A fling passes through dozens of
  windows; without this you queue and cancel hundreds of requests.
- **Bounded parallelism (3 eager, 2 disk).** Prefetches and visible cells share
  one OkHttp dispatcher — an unbounded fan-out starves the images the user is
  looking at.
- **Same cache key as the display request.** `imageVariant()` is called by both
  the tile and the prefetcher for exactly this reason. A prefetch that warms a
  different key than the visible cell later asks for has a 100% miss rate while
  still spending the bandwidth. This is the most common way prefetching is
  implemented and gains nothing.

### 4. Compose-side: recomposition, layout and scroll smoothness

- **Stable keys** (`key = DishCard::id`) plus **`contentType`** on the grid
  items, so a scrolled-off tile's slot is reused rather than rebuilt.
- **`@Immutable` models.** `DishCard`, `FeedTuning` and `ImageVariant` are all
  immutable, so a tile receiving the same instance is skipped outright.
- **`@Immutable` holder for unstable third-party types.** `ImageLoader` is an
  interface Compose can prove nothing about; passing it to a tile as a bare
  parameter makes that tile *unskippable*. `FeedImageEngine` wraps the loader,
  the metrics and the prefetcher in one holder that can honestly be annotated.
- **Cell width computed once**, from `LocalWindowInfo.containerSize`, instead of
  a `BoxWithConstraints` subcomposition per tile. Cheaper, and it gives the CDN
  a single width bucket.
- **`Modifier.aspectRatio` on the image box.** The tile has its final size
  before the image exists, so an arriving bitmap never re-triggers layout for
  the rest of the grid.
- **Dominant-colour placeholder.** `DishCard.accentArgb` ships in the catalog
  and is drawn as the box background: no second request (unlike a BlurHash or
  LQIP thumbnail), no grey flash, no shimmer animation to keep running.
- **Scroll position is read in a `snapshotFlow` inside a coroutine**, never in
  composition. Reading `gridState.layoutInfo` from a composable body recomposes
  the whole screen on every scroll frame — the classic way to lose 60 fps while
  trying to make a list faster.
- **Metrics are polled, not pushed.** Image events fire hundreds of times per
  fling; feeding them into snapshot state would make the metrics panel the most
  recomposed thing on screen and cause the jank it is measuring. The counters
  are plain atomics and the UI reads `snapshot()` every 400 ms.

### 5. Capability-aware delivery: `variant = f(layout, density, device, network, intent)`

Sizing by pixels alone is the *floor*, and it breaks on exactly the devices you
are trying to protect. A budget phone sold in India today has a 1080x2400 panel
with 2-3 GB of RAM, eight slow cores and a congested cellular link. Density-based
sizing hands it the same payload as a flagship and then asks its weaker CPU to
decode it. **Density tells you how many pixels the screen has; it says nothing
about how many the device can afford.**

So capability is resolved on-device into variant parameters:

| file | role |
|---|---|
| `DevicePolicy.kt` | static: `isLowRamDevice`, RAM, cores, heap, API → `LOW/MID/HIGH` → memory-cache share, bucket ceiling, bitmap config, AVIF eligibility |
| `NetworkMonitor.kt` | dynamic: transport, metered, Data Saver, plus a **measured** EWMA throughput → `OFFLINE/SLOW/MODERATE/FAST` |
| `DeliveryPolicy.kt` | the decision function → `DeliveryProfile` (bucket cap, quality, prefetch window, timeouts, cache budget) |

Four things worth saying out loud about this design:

1. **No device info is ever sent to the server.** The URL is the contract. If the
   app posted its hardware and the server decided, responses would vary per
   device model — you would need `Vary` headers and the CDN edge hit rate would
   collapse across tens of thousands of Android SKUs. Local resolution collapses
   that population into `buckets x qualities x formats`: a few dozen shared
   derivatives.
2. **The device half configures the loader; the network half only touches
   requests.** Device tier is static for the process, so it can own the
   `ImageLoader`. Network tier changes mid-scroll, and rebuilding a loader to
   react would discard the memory cache, the disk cache handle *and* the
   connection pool. `MemoryCache.maxSize` is mutable, which is how the budget
   still adapts without a rebuild.
3. **Tier flapping is a real bug.** The tier feeds the URL, so every flip changes
   the cache key and re-downloads the visible screen. `NetworkMonitor` defends
   with a 10 s dwell time, a 25% upgrade margin, re-classification on a 5 s timer
   rather than per sample, and a rounded published kbit/s so the *policy* never
   sees jitter that only the *panel* should see.
4. **Never downgrade something already decoded.** `VariantResolver` keeps a
   per-image ledger of the richest variant requested and, if that variant is
   still resident in the memory cache, serves it instead of fetching a smaller
   one. Upgrades are still allowed. Without this, walking out of WiFi re-fetches
   the whole viewport at lower quality.

`NetworkCapabilities.getLinkDownstreamBandwidthKbps()` is a radio capability
hint, not a measurement — it reports "LTE, 20 Mbps" on a congested cell
delivering 300 kbit/s. So throughput is measured from
`okhttp3.EventListener.responseBodyStart/End`, which brackets **body bytes
only**: DNS, TCP, TLS and TTFB are latency, and folding them in would make a
fast link to a distant server look slow.

### 6. Placeholders: the ladder, and why "no extra request" is the whole argument

| technique | bytes in JSON | extra request |
|---|---|---|
| dominant colour | ~4 | no |
| **`MicroThumb` / BlurHash / ThumbHash** | ~28-40 | no |
| inline base64 micro-JPEG | ~300-500 | no |
| separate LQIP thumbnail URL | ~1-2 KB | **yes** |

On a high-latency link, round trips hurt more than bytes. A second request per
tile doubles the round trips before the user sees anything, so anything that
rides along in a response you were already making is close to free.

`MicroThumb.kt` stores a 4x3 grid of RGB444 cells (36 chars) and lets the GPU's
bilinear filter do the smoothing when the 12-pixel bitmap is scaled up — same
perceptual result as BlurHash at thumbnail size, without a hand-rolled DCT that
could be subtly wrong. **In production use the real BlurHash or ThumbHash
library and run the encoder server-side at upload.** Here the hash is synthesised
from the accent colour, because picsum cannot tell us anything about the photo
it is about to serve: the pipeline is real, the resemblance is not.

`DishCard.aspectRatio` also now comes from the "catalog" rather than being
hardcoded — and it is **bucketed to two crops**, for the same reason widths are
bucketed. A catalog where every photo declares its natural ratio makes the CDN
derive a different `w x h` per image and the derivative cache explodes.

### 7. Hardware and memory — including one piece of common advice that is now wrong

**`BitmapPool` + `inBitmap` reuse: do not add this.** It was correct, necessary
advice in the Glide/Fresco era. Coil deliberately removed its bitmap pool, and
on Coil 3 reimplementing one would make things *worse*:

- ART's generational GC (Android 8+) made short-lived bitmap allocation cheap,
  which removed most of the original motivation;
- `inBitmap` reuse requires software bitmaps, so a pool forces
  `allowHardware(false)` — you would trade the biggest win for a smaller one;
- hardware bitmaps cannot be recycled into a pool at all: their pixels live in
  graphics memory, outside the Java heap, which is exactly why they help.

`Bitmap.Config.HARDWARE` on API 26+ is **already the default** — Coil's
`allowHardware` is on unless you turn it off, so the feed gets it for free. The
real tension is that hardware bitmaps and `RGB_565` are mutually exclusive:

| | heap cost | notes |
|---|---|---|
| `HARDWARE` | zero Java heap | no pixel access; ~1 FD each, and Coil tracks the process FD budget |
| `RGB_565` | half of ARGB_8888 | software; fine for opaque photos, wrong wherever there is alpha |

`DevicePolicy` picks `RGB_565` on the low tier and leaves hardware bitmaps
everywhere else. That choice is genuinely contestable — many teams keep hardware
bitmaps on every tier and cap the *bucket* instead, which is the lever that
reduces both transfer and decode at once. The panel lets you watch both.

**`ComponentCallbacks2` / `onTrimMemory`:** Coil registers its own system
callbacks and trims the memory cache under pressure, so there is nothing to add.
The `Trim memory` button in the policy panel exists only to make that path
observable — press it and the feed refills from *disk*, not from the network.

### 8. Network resiliency: timeout, downgrade, cancel

- **Adaptive per-call timeouts.** `AdaptiveTimeoutInterceptor` reads the live
  profile and applies `Chain.withReadTimeout` / `withConnectTimeout` per request.
  Connect is a flat 5 s (DNS + TCP + TLS is a fixed cost that does not scale with
  throughput); the read budget is tier-sized at 5 / 8 / 12 s.

  **Those numbers are measured, not chosen — and getting this wrong is how the
  first run of this feature shipped a feed of flat colour rectangles.** The
  original ladder was 1.5 s on FAST and 1.8 s on MODERATE, which is the right
  budget for a CDN edge returning a pre-warmed derivative in under 200 ms. This
  demo's backend is not that: `picsum.photos` 302-redirects to Fastly and
  *generates* the crop, measuring **1.25-1.68 s TTFB from a desktop on fibre,
  under a 16-request burst**. So every request timed out, downgraded twice, hit
  the cap and gave up, leaving only the placeholder. Two lessons worth stating in
  an interview:

  1. A timeout ladder is calibrated against the **TTFB distribution of your
     actual backend**, at the p95 under your own concurrency — not picked from a
     blog post. It is also the first thing you put behind remote config, because
     a backend regression turns an aggressive client budget into a total outage.
  2. **A read timeout is not a total call budget.** "Auto-cancel anything over
     1.5 s" means `OkHttpClient.callTimeout` or `Call.timeout()`; an interceptor
     can only shape connect, read and write. Collapsing connect and read into one
     number — which this file also did at first — lets the tighter constraint
     govern both.

  The `Tight timeout` chip in the policy panel forces a 1.2 s read budget on
  demand, so the downgrade ladder can still be watched working.
- **Timeout downgrades instead of retrying.** Retrying the same variant on a link
  that just proved it cannot deliver it only wastes the budget again. Each
  failure drops a rung off both ladders (`DeliveryProfile.downgraded`), bounded
  at two steps. Retries announce themselves with a request header that is
  **stripped before leaving the device** — it must never reach the CDN, or it
  fragments the edge cache key.
- **Cancel on detachment is already free, and now visible.** Coil's
  `AsyncImagePainter` is a `RememberObserver`: when a tile leaves composition its
  request is cancelled. Prefetches die with the `Job` that `submit()` replaces
  and with the composition's `CoroutineScope`. The `cancelled` counter in the
  metrics panel makes this legible — **a healthy fling should produce
  cancellations.** Zero cancellations during fast scrolling means something is
  keeping requests alive after their view is gone.

## Reading the metrics panel

| field | meaning |
|---|---|
| `Downloaded` | exact on-the-wire bytes from `okhttp3.EventListener.responseBodyEnd` — more accurate than `Content-Length`, which is absent under chunked encoding and lies under compression. Cache hits never reach it. |
| `No network` | share of displayed images served from memory or disk |
| `Avg fetch` / `Avg decode` | Coil `EventListener` timings, split by pipeline stage |
| `memory` / `disk` / `network` | where each *displayed* image came from |
| `prefetched` | speculative requests that completed (tagged with an `Extras.Key`, so they never pollute the display stats) |
| `cancelled` | requests abandoned because the tile scrolled away — this number *should* be non-zero while flinging |
| `downgraded` | images that fell back to a smaller variant after a timeout |
| `bitmap cache` | live memory-cache occupancy against its cap |

## Things to try

1. **Scroll with everything on**, then hit reset and scroll back up.
   `No network` should jump towards 100% — memory and disk are doing the work.
2. **Turn off `CDN resize`** and scroll a fresh stretch. The pixels on screen
   are identical — Coil still downsamples while decoding — but `Downloaded`
   climbs roughly an order of magnitude and `Avg fetch` goes up with it. Those
   bytes bought nothing.
3. **Turn off `Prefetch`** and fling. Tiles now arrive after the crossfade
   instead of being there already; `network` climbs and `memory` stalls.
4. **Turn off `Stable keys`** and press `+4 new arrivals`. Every slot now holds
   a different dish, so all of them re-bind and re-request — watch the counters
   jump for four prepended items.
5. **`Show variant`** labels each tile with the dimensions and format it
   actually requested.
6. **`adb logcat -s coil3`** (debug builds) shows every load, including the
   throwable behind a failure. Without a `DebugLogger` a failing image is
   completely silent — Coil folds the exception into an `ErrorResult` and the UI
   just shows a placeholder forever.
7. **`adb logcat -s FastImageFeed`** (debug builds) prints a composition count
   per tile via the project's `LogCompositions` helper — each tile should
   compose once, not once per scroll frame.

## Still missing, and what I would say about it

- `PersistentList` for the item lists, so `FeedUiState` is stable without
  leaning on the `@Immutable` annotation.
- Paging 3 instead of a fixed 120-item list.
- A baseline profile: the first fling is dominated by JIT, not by images.
- **Remote config over every ladder** — the bucket ladder, the quality ladder,
  the tier cut-offs, the prefetch windows and the timeouts are all constants in
  this sample, which is the one thing you must not ship. You cannot fix a
  quality/bytes trade you can only change with a release.
- **Telemetry segmented by device tier x network tier.** A global p50 hides the
  users this whole design exists for. The SLIs that matter: image p95 by
  segment, cache hit rate per layer, bytes per session, prefetch waste rate
  (prefetched-but-never-shown), and time to first meaningful image.
- Real BlurHash/ThumbHash with a server-side encoder, and server-provided
  dominant colours rather than a hardcoded palette.
- Progressive JPEG, which paints something at ~15% of bytes received on one
  request — though note `BitmapFactory` gives you no incremental display for
  free, which is why many apps skip it.

## What a production version would add on top

- A `PersistentList`/`ImmutableList` for the item lists, so `FeedUiState` is
  stable without leaning on the `@Immutable` annotation.
- Paging 3 instead of a fixed 120-item list, with the prefetch window driven
  from the same scroll observer.
- A baseline profile: the first fling is dominated by JIT, not by images.
- `Coil`'s `AsyncImagePreviewHandler` so `@Preview` renders without network.
- Server-side dominant colours (or a BlurHash) in the catalog payload rather
  than a hardcoded palette.
