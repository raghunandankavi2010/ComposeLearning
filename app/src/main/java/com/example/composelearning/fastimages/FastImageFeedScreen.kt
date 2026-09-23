package com.example.composelearning.fastimages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.composelearning.util.LogCompositions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private const val GRID_COLUMNS = 2

/** Policy panel, metrics panel and category strip all sit above the dish grid. */
private const val HEADER_ITEM_COUNT = 3

private val GRID_EDGE_PADDING = 12.dp
private val GRID_GAP = 12.dp
private val CATEGORY_SIZE = 64.dp

/**
 * Image events fire hundreds of times per fling. Polling the counters on a slow
 * timer keeps the panel to ~2 recompositions a second instead of hundreds.
 */
private const val METRICS_REFRESH_MS = 400L

/**
 * A fling walks through dozens of scroll windows. Waiting for the scroll to
 * settle means we only ever queue the window the user actually stopped near,
 * instead of racing 200 cancelled requests through OkHttp.
 */
private const val PREFETCH_SETTLE_MS = 90L

/**
 * How far a failing image is allowed to fall down the ladder. Bounded, because
 * an unbounded retry chain on a broken link is just a slower way to fail.
 */
private const val MAX_DOWNGRADE_STEPS = 2

/** Used only until the window reports its real width on the first frame. */
private const val FALLBACK_WINDOW_WIDTH_PX = 1080

@Composable
fun FastImageFeedRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: FastImageFeedViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FastImageFeedScreen(
        state = state,
        onBack = onBack,
        onTuningChange = viewModel::setTuning,
        onOverridesChange = viewModel::setOverrides,
        onNewArrivals = viewModel::addNewArrivals,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastImageFeedScreen(
    state: FeedUiState,
    onBack: () -> Unit,
    onTuningChange: (FeedTuning) -> Unit,
    onOverridesChange: (PolicyOverrides) -> Unit,
    onNewArrivals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val signals = remember(context) { DeviceSignals.detect(context) }
    // The loader is configured from the *detected* tier, because it is built
    // once for the process. The resolved profile below can still override the
    // budget at runtime without rebuilding anything.
    val devicePolicy = remember(signals) { signals.policyFor() }
    val engine = rememberFeedImageEngine(devicePolicy)
    val profile = rememberDeliveryProfile(state.overrides)
    val gridState = rememberLazyGridState()
    val tuning = state.tuning

    LaunchedEffect(engine, profile.memoryCacheBytes) {
        engine.applyDeviceBudget(profile.memoryCacheBytes)
    }

    // Every cell in a fixed-column grid is the same width, and that width is
    // arithmetic — not something worth a BoxWithConstraints subcomposition per
    // tile. Computing it once here keeps tiles cheap and gives the CDN a single
    // width bucket to cache.
    val windowWidthPx = LocalWindowInfo.current.containerSize.width
    val density = LocalDensity.current
    val cellWidthPx = remember(windowWidthPx, density) {
        val width = if (windowWidthPx > 0) windowWidthPx else FALLBACK_WINDOW_WIDTH_PX
        val chrome = with(density) {
            (GRID_EDGE_PADDING * 2 + GRID_GAP * (GRID_COLUMNS - 1)).toPx()
        }
        ((width - chrome) / GRID_COLUMNS).toInt().coerceAtLeast(1)
    }

    PrefetchAheadOfScroll(
        gridState = gridState,
        dishes = state.dishes,
        engine = engine,
        profile = profile,
        cellWidthPx = cellWidthPx,
        cdnResize = tuning.cdnResize,
        enabled = tuning.prefetch
    )

    val metrics by produceState(ImageMetricsSnapshot(), engine) {
        while (true) {
            value = engine.metrics.snapshot()
            delay(METRICS_REFRESH_MS)
        }
    }
    val liveKbps by produceState(0, engine) {
        while (true) {
            value = engine.monitor.liveKbps()
            delay(METRICS_REFRESH_MS)
        }
    }

    // A function reference, so the key lambda's identity is stable across
    // recompositions and the grid does not treat it as changed content.
    val dishKey: ((DishCard) -> Any)? = if (tuning.stableKeys) DishCard::id else null

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Fast Image Feed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = GRID_EDGE_PADDING,
                end = GRID_EDGE_PADDING,
                top = GRID_EDGE_PADDING,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
            verticalArrangement = Arrangement.spacedBy(GRID_GAP)
        ) {
            item(
                key = "policy",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "policy"
            ) {
                DeliveryPolicyPanel(
                    profile = profile,
                    signals = signals,
                    liveKbps = liveKbps,
                    overrides = state.overrides,
                    onOverridesChange = onOverridesChange,
                    onSimulateMemoryPressure = engine::simulateMemoryPressure
                )
            }

            item(
                key = "metrics",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "metrics"
            ) {
                FeedMetricsPanel(
                    metrics = metrics,
                    tuning = tuning,
                    onTuningChange = onTuningChange,
                    onNewArrivals = onNewArrivals,
                    onResetStats = engine.metrics::reset
                )
            }

            item(
                key = "categories",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "categories"
            ) {
                CategoryStrip(
                    categories = state.categories,
                    engine = engine,
                    profile = profile,
                    cdnResize = tuning.cdnResize
                )
            }

            items(
                items = state.dishes,
                key = dishKey,
                // A content type lets the grid reuse a scrolled-off tile's slot
                // for the next tile instead of building a fresh subtree.
                contentType = { "dish" }
            ) { dish ->
                DishTile(
                    dish = dish,
                    cellWidthPx = cellWidthPx,
                    engine = engine,
                    profile = profile,
                    tuning = tuning
                )
            }
        }
    }
}

/**
 * Turns scroll position into prefetch work.
 *
 * The scroll position is read inside a [snapshotFlow] in a coroutine, never in
 * composition. Reading `gridState.layoutInfo` from a composable body would make
 * this screen recompose on every scroll frame — the classic way to lose 60fps
 * while trying to make a list faster.
 */
@Composable
private fun PrefetchAheadOfScroll(
    gridState: LazyGridState,
    dishes: List<DishCard>,
    engine: FeedImageEngine,
    profile: DeliveryProfile,
    cellWidthPx: Int,
    cdnResize: Boolean,
    enabled: Boolean
) {
    LaunchedEffect(gridState, dishes, engine, profile, cellWidthPx, cdnResize, enabled) {
        if (!enabled) {
            engine.prefetcher.cancel()
            return@LaunchedEffect
        }
        var previousFirst = 0
        snapshotFlow {
            val visible = gridState.layoutInfo.visibleItemsInfo
            val first = (visible.firstOrNull()?.index ?: 0) - HEADER_ITEM_COUNT
            val last = (visible.lastOrNull()?.index ?: 0) - HEADER_ITEM_COUNT
            first.coerceAtLeast(0) to last.coerceAtLeast(0)
        }
            .distinctUntilChanged()
            .collectLatest { (firstDish, lastDish) ->
                delay(PREFETCH_SETTLE_MS)
                val scrollingDown = firstDish >= previousFirst
                previousFirst = firstDish
                val plan = prefetchPlan(
                    firstVisible = firstDish,
                    lastVisible = lastDish,
                    itemCount = dishes.size,
                    scrollingDown = scrollingDown,
                    eagerAhead = profile.eagerPrefetch,
                    diskAhead = profile.diskPrefetch
                )
                fun variantFor(index: Int): ImageVariant {
                    val dish = dishes[index]
                    return engine.resolver.resolve(
                        seed = dish.imageSeed,
                        targetWidthPx = cellWidthPx,
                        aspectRatio = dish.aspectRatio,
                        profile = profile,
                        cdnResize = cdnResize
                    )
                }
                engine.prefetcher.submit(
                    eager = plan.eager.map(::variantFor),
                    diskOnly = plan.diskOnly.map(::variantFor),
                    profile = profile
                )
            }
    }
}

@Composable
private fun DishTile(
    dish: DishCard,
    cellWidthPx: Int,
    engine: FeedImageEngine,
    profile: DeliveryProfile,
    tuning: FeedTuning,
    modifier: Modifier = Modifier
) {
    LogCompositions("FastImageFeed", "tile ${dish.id}")
    val context = LocalContext.current

    // Timeout fallback state, scoped to this dish. When the adaptive read
    // timeout fires, retrying the *same* variant on a link that just proved it
    // cannot deliver it only wastes the budget again — so each failure drops a
    // rung off the bucket and quality ladders instead.
    var downgradeSteps by remember(dish.id) { mutableIntStateOf(0) }

    val variant = remember(dish.imageSeed, cellWidthPx, profile, tuning.cdnResize, downgradeSteps) {
        engine.resolver.resolve(
            seed = dish.imageSeed,
            targetWidthPx = cellWidthPx,
            aspectRatio = dish.aspectRatio,
            profile = profile,
            cdnResize = tuning.cdnResize,
            downgradeSteps = downgradeSteps
        )
    }
    // Built once per variant. An ImageRequest is a fairly heavy object, and
    // rebuilding it on every recomposition would also make AsyncImage restart
    // its load because the model compares unequal.
    val request = remember(context, variant, profile, downgradeSteps) {
        ImageRequest.Builder(context)
            .applyVariant(variant, profile, retryAttempt = downgradeSteps)
            .build()
    }
    // 12 pixels, 48 bytes, decoded once. Scaled up with a bilinear filter it
    // reads as a blur, which is the whole trick — the GPU does the smoothing.
    val thumb = remember(dish.thumbHash) { MicroThumb.decode(dish.thumbHash) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // The aspect ratio comes from the catalog, so the tile has its
                // final size before the image exists and an arriving bitmap
                // never re-triggers layout for the rest of the grid.
                .aspectRatio(dish.aspectRatio)
                // Dominant colour from the catalog: the floor of the
                // placeholder ladder, and what shows if the hash is missing.
                .background(Color(dish.accentArgb))
        ) {
            if (tuning.thumbHashPlaceholder && thumb != null) {
                Image(
                    bitmap = thumb,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.Low
                )
            }
            AsyncImage(
                model = request,
                contentDescription = dish.name,
                imageLoader = engine.loader,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    if (downgradeSteps < MAX_DOWNGRADE_STEPS) {
                        downgradeSteps++
                        engine.metrics.recordDowngrade()
                    }
                }
            )
            if (tuning.showVariant) {
                VariantBadge(
                    variant = variant,
                    downgradeSteps = downgradeSteps,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = dish.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dish.restaurant,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${dish.rating} · ${dish.etaLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dish.priceLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VariantBadge(
    variant: ImageVariant,
    downgradeSteps: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f)
    ) {
        Text(
            text = variant.label + if (downgradeSteps > 0) " ↓$downgradeSteps" else "",
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
private fun CategoryStrip(
    categories: List<DishCategory>,
    engine: FeedImageEngine,
    profile: DeliveryProfile,
    cdnResize: Boolean
) {
    val density = LocalDensity.current
    val sizePx = remember(density) { with(density) { CATEGORY_SIZE.roundToPx() } }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items = categories, key = { it.id }) { category ->
            CategoryChip(
                category = category,
                sizePx = sizePx,
                engine = engine,
                profile = profile,
                cdnResize = cdnResize
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: DishCategory,
    sizePx: Int,
    engine: FeedImageEngine,
    profile: DeliveryProfile,
    cdnResize: Boolean
) {
    val context = LocalContext.current
    var downgradeSteps by remember(category.id) { mutableIntStateOf(0) }

    // A 64dp circle asks for a small square bucket, not the wider bucket the
    // grid tiles use. Sizing per surface rather than per app is where most of
    // the "why are our images so big" problem actually lives.
    val variant = remember(category.imageSeed, sizePx, profile, cdnResize, downgradeSteps) {
        engine.resolver.resolve(
            seed = category.imageSeed,
            targetWidthPx = sizePx,
            aspectRatio = ASPECT_SQUARE,
            profile = profile,
            cdnResize = cdnResize,
            downgradeSteps = downgradeSteps
        )
    }
    val request = remember(context, variant, profile, downgradeSteps) {
        ImageRequest.Builder(context)
            .applyVariant(variant, profile, retryAttempt = downgradeSteps)
            .build()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = request,
            contentDescription = category.label,
            imageLoader = engine.loader,
            modifier = Modifier
                .size(CATEGORY_SIZE)
                .clip(CircleShape)
                .background(Color(category.accentArgb)),
            contentScale = ContentScale.Crop,
            onError = {
                if (downgradeSteps < MAX_DOWNGRADE_STEPS) {
                    downgradeSteps++
                    engine.metrics.recordDowngrade()
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedMetricsPanel(
    metrics: ImageMetricsSnapshot,
    tuning: FeedTuning,
    onTuningChange: (FeedTuning) -> Unit,
    onNewArrivals: () -> Unit,
    onResetStats: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Image pipeline",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onResetStats) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset stats")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCell(
                    label = "Downloaded",
                    value = formatBytes(metrics.networkBytes),
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "No network",
                    value = "${metrics.instantPercent}%",
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "Avg fetch",
                    value = "${metrics.avgFetchMs} ms",
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    label = "Avg decode",
                    value = "${metrics.avgDecodeMs} ms",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "shown ${metrics.displayed} · memory ${metrics.fromMemory} · " +
                    "disk ${metrics.fromDisk} · network ${metrics.fromNetwork} · " +
                    "prefetched ${metrics.prefetched} · in flight ${metrics.inFlight}\n" +
                    "cancelled ${metrics.cancelled} (scrolled away) · " +
                    "downgraded ${metrics.downgrades} · failed ${metrics.failed}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            val cacheFraction = if (metrics.memoryCacheMaxBytes == 0L) {
                0f
            } else {
                metrics.memoryCacheBytes.toFloat() / metrics.memoryCacheMaxBytes
            }
            LinearProgressIndicator(
                progress = { cacheFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "bitmap cache ${formatBytes(metrics.memoryCacheBytes)} / " +
                    formatBytes(metrics.memoryCacheMaxBytes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TuningChip(
                    label = "CDN resize",
                    selected = tuning.cdnResize,
                    onClick = { onTuningChange(tuning.copy(cdnResize = !tuning.cdnResize)) }
                )
                TuningChip(
                    label = "Prefetch",
                    selected = tuning.prefetch,
                    onClick = { onTuningChange(tuning.copy(prefetch = !tuning.prefetch)) }
                )
                TuningChip(
                    label = "Stable keys",
                    selected = tuning.stableKeys,
                    onClick = { onTuningChange(tuning.copy(stableKeys = !tuning.stableKeys)) }
                )
                TuningChip(
                    label = "Blur preview",
                    selected = tuning.thumbHashPlaceholder,
                    onClick = {
                        onTuningChange(
                            tuning.copy(thumbHashPlaceholder = !tuning.thumbHashPlaceholder)
                        )
                    }
                )
                TuningChip(
                    label = "Show variant",
                    selected = tuning.showVariant,
                    onClick = { onTuningChange(tuning.copy(showVariant = !tuning.showVariant)) }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onNewArrivals) { Text("+4 new arrivals") }
                Text(
                    text = "prepends items — watch the counters with stable keys off",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TuningChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
    )
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024f * 1024f))
    bytes >= 1024L -> "${bytes / 1024L} KB"
    else -> "$bytes B"
}
