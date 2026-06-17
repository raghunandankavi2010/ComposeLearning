# Shorts Video Feed — Architectural Decisions

Endless TikTok/YouTube-Shorts-style vertical video feed built on Jetpack Compose
`VerticalPager` + Media3 ExoPlayer.

```
shortsfeed/
├── data/
│   └── VideoFeedRepository.kt    # Endless page generator over public sample MP4s
├── player/
│   ├── VideoFeedCache.kt         # Process-wide SimpleCache (LRU) + CacheDataSource.Factory
│   └── FeedPlayerManager.kt      # THE shared ExoPlayer + Media3 DefaultPreloadManager
└── presentation/
    ├── ShortsFeedViewModel.kt    # UiState / UiEvent contract, paging, playback commands
    └── ShortsFeedScreen.kt       # Route → Screen → Page composables, SurfaceView host
```

Data flow (unidirectional):

```
User gesture ──► ShortsFeedEvent ──► ViewModel ──► FeedPlayerManager / Repository
                                         │
UI ◄── ShortsFeedUiState ◄── combine(videos, userPaused, playbackState)
```

---

## ADR-1: One shared ExoPlayer, not a player per page (and not a pool of N)

**Decision:** A single `ExoPlayer` owned by `FeedPlayerManager`, re-targeted with
`setMediaItem()` on every page settle and re-attached to the settled page's
`SurfaceView`.

**Why not per-item players:** each ExoPlayer instance owns MediaCodec decoders,
an audio session, renderer threads and load buffers (tens of MB each). A lazy
pager that composes pages ±1 would keep 3 alive — and codec instances are a
*hard* system-wide resource; exhausting them fails playback for the whole device,
not just this app. Per-item players also serialize codec configure/release on
the fling path, which is exactly where jank is most visible.

**Why not a 2–3 player pool:** a pool's only benefit is a *pre-prepared* next
item (decoder warm, first GOP decoded), buying ~100–200 ms of first-frame
latency — at the cost of surface juggling across players, doubled codec
pressure, audio-focus edge cases, and a much larger state machine. Media3's
`DefaultPreloadManager` (ADR-4) gives that win *without* a player pool: it
prepares sources, selects tracks, and pre-loads the first few seconds of the
adjacent items into RAM using the shared player's resources, then playback
re-uses the already-warm `MediaSource`. One player, pool-like instant starts.

**Mechanics that make one player safe:**
- Playback starts only on `pagerState.settledPage` (never mid-fling), so the
  player is never asked to race the scroll.
- `repeatMode = REPEAT_MODE_ONE` loops the current clip (shorts UX).
- `currentItem` identity check makes `play()` idempotent for the same page
  (e.g. lifecycle resume) while forcing a full re-prepare after an error.

## ADR-2: Raw `SurfaceView` in `AndroidView`, exists only on the settled page

**Decision:** Each settled page hosts a raw `SurfaceView` (no `PlayerView`),
created in Compose and attached via `player.setVideoSurfaceView()` inside a
`DisposableEffect`. Non-settled pages render a placeholder only — **at most one
SurfaceView is alive at any time**.

**Why `SurfaceView`:** it renders on a hardware overlay plane, bypassing the
app's GPU composition entirely — measurably cheaper and lower-latency than
`TextureView` (which routes frames through the view hierarchy as a GL texture)
and DRM-capable. `PlayerView` was rejected because we need none of its
controller chrome, and wrapping our own SurfaceView keeps the page fully
declarative.

**Why one surface, not one per composed page:** SurfaceViews hold their last
rendered frame. If pages kept their surfaces while recycled, page N+1 could
flash a stale frame from a previous clip. Composing the surface *only* when
`page == settledPage` destroys it on swipe, so the next page always starts from
the placeholder — the same pattern TikTok uses (thumbnail → first frame).

**Aspect-fill math:** the player reports `onVideoSizeChanged`; a custom
`Modifier.layout` (`cropToFill`) measures the SurfaceView at the smallest size
that *covers* the page at the video's aspect ratio, centers it, and the page's
`clipToBounds()` crops the overflow — center-crop without `graphicsLayer`
scaling (which is unreliable on surface-backed views). This is what
`AspectRatioFrameLayout(RESIZE_MODE_ZOOM)` does internally, re-expressed as a
Compose layout modifier.

**Cleanup contract:** `onDispose → clearVideoSurfaceView(view)` is identity
-checked by Media3, so the unavoidable Compose ordering ambiguity (new page's
attach vs old page's dispose) is harmless in both orders.

## ADR-3: `VerticalPager` with hard one-page snapping

- `PagerSnapDistance.atMost(1)`: a fling moves exactly one page — no skipped
  clips, and the pager can only ever settle flush on a page boundary (zero
  half-screen visibility by construction).
- `beyondViewportPageCount = 1`: neighbors are pre-composed (text, scrim,
  placeholder) so the UI is ready the instant a fling starts — but per ADR-2
  their video surfaces are not.
- `key = { video.id }`: stable identity across list appends, so growing the
  endless list never re-binds visible pages.
- `settledPage` (not `currentPage`/`targetPage`) drives playback via
  `snapshotFlow`, read inside `LaunchedEffect` so scroll progress never
  recomposes the screen.

## ADR-4: Media3 `DefaultPreloadManager` (in-RAM) over an LRU disk cache

Two complementary layers warm the next clip: the **preload manager** holds a few
seconds of the adjacent items decoded-and-ready in memory, while the **disk
cache** persists bytes across screen open/close. This replaces the previous
hand-rolled `CacheWriter` pre-buffer, following Meta's Media3 PreloadManager
guidance (Instagram Reels / Facebook).

**Preload manager (the instant-start win):**
- Built via `DefaultPreloadManager.Builder(context, control)`; the shared
  `ExoPlayer` is built from the *same* builder (`buildExoPlayer(...)`) so they
  share load control, bandwidth meter and renderers — no second player.
- Items are registered with `add(mediaItem, index)` (rankingData = feed index)
  as pages are appended.
- On every settle: `setCurrentPlayingIndex(index)` + `invalidate()`. The
  `TargetPreloadStatusControl` returns `PreloadStatus.specifiedRangeOf(3 s)` for
  items within **±2** of the current page (Instagram "adjacent" strategy) and
  `null` beyond it, so memory is bounded and the window self-prioritizes by
  distance. Playback calls `getMediaSource(mediaItem)` and hands the *already
  prepared, tracks-selected, partially-loaded* source to the player.

**Disk cache (persistence + de-dupe):** one process-wide `SimpleCache` (256 MB,
`LeastRecentlyUsedCacheEvictor`) behind a `CacheDataSource.Factory` that is the
upstream of the preload manager's `DefaultMediaSourceFactory`. So preloaded *and*
played bytes land on disk under the same key (the URI); re-watching or swiping
back is free. `FLAG_IGNORE_CACHE_ON_ERROR` degrades a corrupt/contended cache to
plain streaming. **Singleton, never released:** Media3 throws if two
`SimpleCache`s open one directory; eviction is the LRU's job, not `release()`'s.

**LoadControl tuning:** max buffer 20 s, start-playback threshold 1 s (default
50 s is long-form). The same `LoadControl` is set on the preload builder, so
over-buffering the current clip never starves preloading of the adjacent ones.

## ADR-5: Lifecycle & leak prevention — three concentric guards

1. **`LifecycleResumeEffect` (Route level):** leaving `RESUMED` (home button,
   app switch, screen covered by another route) pauses instantly via
   `AppBackgrounded`; returning resumes — *unless the user had explicitly
   paused*, which is tracked separately (`userPaused`) so we never override an
   intentional pause.
2. **`DisposableEffect` (Surface level):** detaches the surface the moment a
   page stops being settled or leaves composition — the player can never render
   into (or hold a reference to) a destroyed surface.
3. **`ViewModel.onCleared` (Screen level):** popping the route releases the
   player (codecs, audio session, renderer threads) and the preload manager
   (all preloaded sources). This is the guarantee against "ghost audio" after
   back-navigation.

The player lives in the ViewModel (with the *application* context only) so
playback survives configuration changes for free and ownership matches the
screen's logical lifetime.

## ADR-6: State/Event contract

- `ShortsFeedUiState` is one immutable snapshot:
  `combine(videos, userPaused, playbackState)` — the UI has zero imperative
  knowledge of the player beyond the read-only `Player` handle it needs for
  surface attachment.
- All inputs funnel through `sealed interface ShortsFeedEvent`
  (`PageSettled`, `TogglePlayPause`, `AppForegrounded`, `AppBackgrounded`,
  `RetryPlayback`) — trivially loggable/testable, and the ViewModel is the only
  component that issues player commands.
- **Endless paging:** the repository generates deterministic pages forever;
  the ViewModel appends the next page when the user settles within 4 items of
  the end. A re-entrancy flag prevents duplicate loads; the first page's
  arrival re-fires `PageSettled(0)` because the pager settled while the list
  was still empty.
- **Recomposition discipline:** playback *progress* is polled at 4 Hz via
  `produceState` inside the single active page only — it never passes through
  the ViewModel's `StateFlow`, so the 4 Hz tick invalidates one hairline `Box`,
  not the feed.
- **No DI framework** (project convention): manual construction +
  `viewModelFactory { initializer { … } }` with `APPLICATION_KEY`.

## Known trade-offs / future work

- **First-frame latency** is now preload-instant for in-window pages (ADR-4):
  the adjacent ±2 items are prepared and partially loaded in RAM. Items reached
  by a long fling past the window still start from cache/network.
- **Tuning knobs** live in `FeedPlayerManager`: `PRELOAD_WINDOW` (how many
  adjacent items) and `PRELOAD_DURATION_MS` (how much of each) trade RAM for
  instant starts. Meta's app additionally pauses preloading under CPU/I-O load —
  the seam for that is the `TargetPreloadStatusControl`.
- **Thumbnails:** the placeholder is a gradient; production would show a server
  -provided first-frame thumbnail (Coil is already in the project's deps).
- **Sample MP4s are progressive**; a real product would serve HLS/DASH with
  ABR — only `VideoFeedRepository` URLs would change (the preload manager works
  the same for adaptive sources).
- **`@UnstableApi`:** Media3's cache APIs are marked unstable (as is all of
  `media3-datasource`); usage is opted-in explicitly at the class level. This
  is the officially documented way to use Media3 caching today — there is no
  stable alternative.
