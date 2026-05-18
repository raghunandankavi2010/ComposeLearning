package com.example.composelearning.peritemvm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// region ── Scoped ViewModelStoreOwner helpers ───────────────────────────────
//
// These mirror the APIs Google ships in androidx.lifecycle 2.11 (currently in
// beta as of May 2026) and apply Google's recommended pattern for scoping a
// ViewModel to a sub-screen UI component:
//
//   • rememberViewModelStoreOwner()                     — one owner per call
//     site. Cleared when the composable leaves composition. Use inside a
//     LazyColumn's items{} when each item should get its own ViewModel that
//     dies on scroll-off.
//
//   • rememberViewModelStoreProvider()                  — hoisted parent that
//     mints keyed owners. Use above a Pager/Tabs so each page keeps its
//     ViewModel even when its page content temporarily leaves composition.
//
// When you bump androidx.lifecycle to 2.11+, you can delete this block and
// import the same names from androidx.lifecycle.viewmodel.compose — call
// sites stay identical.
//
// See: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-apis

@Composable
fun rememberViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}

class ViewModelStoreProvider {
    private val stores = mutableMapOf<Any, ViewModelStore>()
    fun storeFor(key: Any): ViewModelStore = stores.getOrPut(key) { ViewModelStore() }
    fun clear() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}

@Composable
fun rememberViewModelStoreProvider(): ViewModelStoreProvider {
    val provider = remember { ViewModelStoreProvider() }
    DisposableEffect(provider) {
        onDispose { provider.clear() }
    }
    return provider
}

@Composable
fun rememberViewModelStoreOwner(
    provider: ViewModelStoreProvider,
    key: Any,
): ViewModelStoreOwner = remember(provider, key) {
    val store = provider.storeFor(key)
    object : ViewModelStoreOwner {
        override val viewModelStore = store
    }
}
// endregion

// region ── Per-item ViewModel ──────────────────────────────────────────────
//
// Each Post gets one of these. The ViewModel simulates an item-scoped network
// load on init and owns the item's like / expand state. The screen-level VM
// stays free of per-item concerns — exactly the bloat the blog warns about.

@Immutable
data class PostItemUiState(
    val isLoading: Boolean = true,
    val body: String = "",
    val likes: Int = 0,
    val liked: Boolean = false,
    val expanded: Boolean = false,
)

class PostItemViewModel(private val postId: Int) : ViewModel() {
    private val _state = MutableStateFlow(PostItemUiState())
    val state: StateFlow<PostItemUiState> = _state.asStateFlow()

    // Stable identity stamp — same VM instance => same value, every time.
    // Visible in the UI so you can see exactly when a VM gets recreated.
    val vmTag: String = "#" + Integer.toHexString(System.identityHashCode(this)).padStart(8, '0')

    init {
        viewModelScope.launch {
            delay(Random.nextLong(400, 1400))
            _state.update {
                it.copy(
                    isLoading = false,
                    body = SAMPLE_BODIES[postId % SAMPLE_BODIES.size],
                )
            }
        }
    }

    fun toggleLike() = _state.update {
        it.copy(liked = !it.liked, likes = if (it.liked) it.likes - 1 else it.likes + 1)
    }

    fun toggleExpanded() = _state.update { it.copy(expanded = !it.expanded) }

    companion object {
        fun factory(postId: Int) = viewModelFactory {
            initializer { PostItemViewModel(postId) }
        }
    }
}

private val SAMPLE_BODIES = listOf(
    "Each item owns its data fetch and view state. The screen-level VM no longer juggles per-item loading flags.",
    "Scoped ViewModels die when their composable leaves composition — a clean lifecycle without manual cleanup.",
    "Two items of the same type each get their own VM instance, so a stale fetch from one never bleeds into the other.",
    "Like, expand, retry — all are item-local state. The list controller stays a list controller.",
    "Lifecycle 2.11's rememberViewModelStoreOwner() formalises this pattern; the helpers above are a 2.10 backport.",
)
// endregion

// region ── Demo 1: LazyColumn with per-item VMs (resets on scroll) ─────────

@Composable
private fun PerItemLazyColumnDemo(modifier: Modifier = Modifier) {
    val postIds = remember { (1..30).toList() }
    Column(modifier = modifier) {
        ExplainerBanner(
            title = "rememberViewModelStoreOwner() per list item",
            body = "Each item provides its own ViewModelStoreOwner. The VM is cleared when " +
                "the item scrolls off-screen — scroll back and you'll see a new VM tag and a fresh load."
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(postIds, key = { it }) { postId ->
                // Per-item scoped owner. Lives as long as this composable is
                // in composition; cleared (VM destroyed) when it scrolls off.
                val itemOwner = rememberViewModelStoreOwner()
                CompositionLocalProvider(LocalViewModelStoreOwner provides itemOwner) {
                    val vm: PostItemViewModel = viewModel(
                        factory = PostItemViewModel.factory(postId),
                    )
                    PostCard(postId = postId, vm = vm)
                }
            }
        }
    }
}
// endregion

// region ── Demo 2: HorizontalPager with hoisted provider (retained) ────────

@Composable
private fun PerPagePagerDemo(modifier: Modifier = Modifier) {
    val pageIds = remember { (1..6).toList() }
    val pagerState = rememberPagerState(pageCount = { pageIds.size })

    // Hoist the provider ABOVE the Pager so it survives page changes — each
    // page's ViewModel is keyed by the page id and kept alive while the
    // provider lives. Swipe away and back: same VM tag, same like count.
    val provider = rememberViewModelStoreProvider()

    Column(modifier = modifier) {
        ExplainerBanner(
            title = "rememberViewModelStoreProvider() above the Pager",
            body = "Each page mints a keyed owner from the hoisted provider. Swipe to a different page " +
                "and back — the VM tag is preserved, the load doesn't replay, and the like count survives."
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(pageIds.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (selected) 10.dp else 8.dp)
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
        ) { pageIndex ->
            val pageId = pageIds[pageIndex]
            val pageOwner = rememberViewModelStoreOwner(provider, key = pageId)
            CompositionLocalProvider(LocalViewModelStoreOwner provides pageOwner) {
                val vm: PostItemViewModel = viewModel(
                    factory = PostItemViewModel.factory(pageId),
                )
                PostCard(postId = pageId, vm = vm, modifier = Modifier.padding(vertical = 12.dp))
            }
        }
    }
}
// endregion

// region ── Demo 3: Anti-pattern — screen-level VM owns every item's state ─

class FeedScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(buildInitial())
    val state: StateFlow<Map<Int, PostItemUiState>> = _state.asStateFlow()

    init {
        // Single VM has to spin up a load for every item up-front — pays a cost
        // even for items the user never sees, and grows linearly with the feed.
        (1..30).forEach { postId ->
            viewModelScope.launch {
                delay(Random.nextLong(400, 1400))
                _state.update { current ->
                    current + (postId to current.getValue(postId).copy(
                        isLoading = false,
                        body = SAMPLE_BODIES[postId % SAMPLE_BODIES.size],
                    ))
                }
            }
        }
    }

    fun toggleLike(postId: Int) = _state.update { current ->
        val s = current.getValue(postId)
        current + (postId to s.copy(liked = !s.liked, likes = if (s.liked) s.likes - 1 else s.likes + 1))
    }

    fun toggleExpanded(postId: Int) = _state.update { current ->
        val s = current.getValue(postId)
        current + (postId to s.copy(expanded = !s.expanded))
    }

    private companion object {
        fun buildInitial(): Map<Int, PostItemUiState> =
            (1..30).associateWith { PostItemUiState() }
    }
}

@Composable
private fun ScreenLevelViewModelDemo(modifier: Modifier = Modifier) {
    val vm: FeedScreenViewModel = viewModel()
    val byId by vm.state.collectAsStateWithLifecycle()
    Column(modifier = modifier) {
        ExplainerBanner(
            title = "One screen-level ViewModel owns every item",
            body = "All loading flags, like counts and expand state live in a Map<Int, …> inside one VM. " +
                "Works at this scale but bloats fast — and the VM has to fan out loads for items the user may never see.",
            tone = BannerTone.Warning,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(byId.keys.toList(), key = { it }) { postId ->
                val ui = byId.getValue(postId)
                PostCardStateless(
                    postId = postId,
                    vmTag = "(screen VM)",
                    state = ui,
                    onLike = { vm.toggleLike(postId) },
                    onExpand = { vm.toggleExpanded(postId) },
                )
            }
        }
    }
}
// endregion

// region ── Shared item UI ──────────────────────────────────────────────────

@Composable
private fun PostCard(
    postId: Int,
    vm: PostItemViewModel,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    PostCardStateless(
        postId = postId,
        vmTag = vm.vmTag,
        state = state,
        onLike = vm::toggleLike,
        onExpand = vm::toggleExpanded,
        modifier = modifier,
    )
}

@Composable
private fun PostCardStateless(
    postId: Int,
    vmTag: String,
    state: PostItemUiState,
    onLike: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "P$postId",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Post #$postId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "VM $vmTag",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                    )
                }
                IconButton(onClick = onExpand) {
                    Icon(
                        imageVector = if (state.expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (state.expanded) "Collapse" else "Expand",
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (state.isLoading) {
                LoadingSkeleton()
            } else {
                Text(
                    text = state.body,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (state.expanded) Int.MAX_VALUE else 2,
                )
            }
            AnimatedVisibility(visible = state.expanded && !state.isLoading) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Expanded detail — this state is owned by the item's own VM, " +
                            "not a screen-level container.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, enabled = !state.isLoading) {
                    Icon(
                        imageVector = if (state.liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (state.liked) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${state.likes}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun LoadingSkeleton() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Loading from this item's own ViewModel…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class BannerTone { Info, Warning }

@Composable
private fun ExplainerBanner(
    title: String,
    body: String,
    tone: BannerTone = BannerTone.Info,
) {
    val container = when (tone) {
        BannerTone.Info -> MaterialTheme.colorScheme.secondaryContainer
        BannerTone.Warning -> MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = when (tone) {
        BannerTone.Info -> MaterialTheme.colorScheme.onSecondaryContainer
        BannerTone.Warning -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = container,
        contentColor = onContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = MaterialTheme.typography.bodySmall)
        }
    }
}
// endregion

// region ── Showcase host ───────────────────────────────────────────────────

private val TABS = listOf("Per-item (LazyColumn)", "Per-page (Pager)", "Anti-pattern")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerItemViewModelShowcaseScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        PrimaryTabRow(selectedTabIndex = selected) {
            TABS.forEachIndexed { index, label ->
                Tab(
                    selected = index == selected,
                    onClick = { selected = index },
                    text = { Text(label, color = if (index == selected) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (selected) {
                0 -> PerItemLazyColumnDemo(modifier = Modifier.fillMaxSize())
                1 -> PerPagePagerDemo(modifier = Modifier.fillMaxSize())
                2 -> ScreenLevelViewModelDemo(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
// endregion
