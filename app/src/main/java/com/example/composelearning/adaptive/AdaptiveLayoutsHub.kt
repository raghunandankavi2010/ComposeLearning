package com.example.composelearning.adaptive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Entry point for the adaptive layouts learning module.
 *
 * It is itself a tiny piece of navigation: a catalog of demos, each opening into a
 * full-screen demo with a back arrow. The four demos progress from the highest-level
 * helper to the lowest:
 *
 *  1. [ListDetailDemo]     — ListDetailPaneScaffold (the pattern you reach for 80% of the time)
 *  2. [SupportingPaneDemo] — SupportingPaneScaffold (primary + auxiliary content)
 *  3. [AdaptiveGridDemo]   — adaptive vs size-class-driven column counts
 *  4. [WindowSizeClassDemo]— raw WindowSizeClass branching, the foundation of all the above
 *
 * To really see them adapt, run on a resizable emulator / foldable and rotate,
 * unfold, or enter multi-window — or just use the "Resizable (Experimental)" AVD
 * and drag the window edge.
 */
enum class AdaptiveDemo(val title: String, val blurb: String) {
    LIST_DETAIL(
        "1 · List–Detail Pane",
        "NavigableListDetailPaneScaffold. One pane on a phone, two side-by-side on a " +
            "tablet/foldable. The go-to pattern for inbox, settings, master-detail screens."
    ),
    SUPPORTING_PANE(
        "2 · Supporting Pane",
        "NavigableSupportingPaneScaffold. A main article plus an auxiliary 'related' pane " +
            "that docks beside it on wide screens and is reachable via a button on narrow ones."
    ),
    ADAPTIVE_GRID(
        "3 · Adaptive Grid",
        "LazyVerticalGrid with GridCells.Adaptive vs GridCells.Fixed driven by WindowSizeClass. " +
            "Compare a continuously-reflowing grid against hand-tuned per-breakpoint columns."
    ),
    REFLOWING_DETAIL(
        "4 · Reflowing Detail (WindowSizeClass)",
        "A product page that reads currentWindowAdaptiveInfo() and rearranges itself: single " +
            "column when compact, two columns when medium/expanded. The primitive under everything."
    ),
    NAVIGATION_SUITE(
        "5 · Adaptive Navigation Suite",
        "NavigationSuiteScaffold. The same destinations render as a bottom navigation bar " +
            "(compact), a navigation rail (medium), then a navigation drawer (expanded) — automatically."
    ),
    ADAPTIVE_DRAWER(
        "6 · Adaptive Drawer (Modal ↔ Permanent)",
        "Hand-built drawer: ModalNavigationDrawer with a menu button on phones, swapped for an " +
            "always-docked PermanentNavigationDrawer on expanded width. Same sheet, different container."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLayoutsHub(modifier: Modifier = Modifier) {
    var selected by rememberSaveable { mutableStateOf<AdaptiveDemo?>(null) }

    // Back returns to the catalog before bubbling up to the app's own nav.
    BackHandler(enabled = selected != null) { selected = null }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selected?.title ?: "Adaptive Layouts",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (selected != null) {
                        IconButton(onClick = { selected = null }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to catalog"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                null -> AdaptiveCatalog(onOpen = { selected = it })
                AdaptiveDemo.LIST_DETAIL -> ListDetailDemo(Modifier.fillMaxSize())
                AdaptiveDemo.SUPPORTING_PANE -> SupportingPaneDemo(Modifier.fillMaxSize())
                AdaptiveDemo.ADAPTIVE_GRID -> AdaptiveGridDemo(Modifier.fillMaxSize())
                AdaptiveDemo.REFLOWING_DETAIL -> WindowSizeClassDemo(Modifier.fillMaxSize())
                AdaptiveDemo.NAVIGATION_SUITE -> AdaptiveNavigationSuiteDemo(Modifier.fillMaxSize())
                AdaptiveDemo.ADAPTIVE_DRAWER -> AdaptiveDrawerDemo(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun AdaptiveCatalog(onOpen: (AdaptiveDemo) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "intro") {
            Text(
                "Four scenarios, smallest helper last. Each adapts to the width it is given — " +
                    "test by rotating, unfolding, or resizing the window. Never branch on device " +
                    "type; branch on the size class you actually receive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(AdaptiveDemo.entries, key = { it.name }) { demo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(demo) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        demo.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        demo.blurb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
