package com.example.composelearning.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.composelearning.animcompose.Navigator
import com.example.composelearning.animcompose.rememberNavigationState
import com.example.composelearning.animcompose.toEntries

/**
 * Single-NavHost tabbed navigation sample (Google's recommended Nav3 pattern):
 *  - one [NavDisplay] hosts every screen
 *  - [rememberNavigationState] keeps a separate [androidx.navigation3.runtime.NavBackStack] per
 *    top-level tab, so each tab has its own journey
 *  - the bottom nav bar is rendered by the Scaffold and only shown when the current tab's stack
 *    depth is 1 (i.e. you are on a top-level destination)
 *  - the whole thing is wrapped in [SharedTransitionLayout] and each entry reads
 *    [androidx.navigation3.ui.LocalNavAnimatedContentScope] so list↔detail can share elements
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TabsSampleNavigation() {
    val navState = rememberNavigationState(
        startRoute = TabsScreen.Photos,
        topLevelRoutes = TopLevelTabs.toSet(),
    )
    val navigator = remember { Navigator(navState) }

    val currentStack = navState.backStacks[navState.topLevelRoute]
    val visibleKey = currentStack?.lastOrNull() ?: navState.topLevelRoute
    val showBottomBar = (currentStack?.size ?: 0) <= 1 && (visibleKey as? TabsScreen)?.isTopLevel() == true

    SharedTransitionLayout {
        val sharedScope = this
        val provider: (NavKey) -> NavEntry<NavKey> = entryProvider {
            entry<TabsScreen.Photos> { PhotosTabScreen(navigator, sharedScope) }
            entry<TabsScreen.Articles> { ArticlesTabScreen(navigator, sharedScope) }
            entry<TabsScreen.Profile> { ProfileTabScreen(navigator) }
            entry<TabsScreen.PhotoDetail> { key ->
                PhotoDetailScreen(
                    id = key.id,
                    sharedScope = sharedScope,
                    onBack = { navigator.goBack() },
                )
            }
            entry<TabsScreen.ArticleDetail> { key ->
                ArticleDetailScreen(
                    id = key.id,
                    sharedScope = sharedScope,
                    onBack = { navigator.goBack() },
                )
            }
            entry<TabsScreen.SettingDetail> { key ->
                SettingDetailScreen(key = key.key, onBack = { navigator.goBack() })
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it },
                ) {
                    TabsBottomBar(
                        currentTab = navState.topLevelRoute as? TabsScreen ?: TabsScreen.Photos,
                        onTabClick = { navigator.navigate(it) },
                    )
                }
            },
        ) { padding ->
            @Suppress("UNCHECKED_CAST")
            NavDisplay(
                modifier = Modifier.padding(padding),
                entries = navState.toEntries(provider),
                onBack = { navigator.goBack() },
            )
        }
    }
}

@Composable
private fun TabsBottomBar(
    currentTab: TabsScreen,
    onTabClick: (TabsScreen) -> Unit,
) {
    NavigationBar {
        TopLevelTabs.forEach { tab ->
            NavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabClick(tab) },
                icon = { Icon(iconFor(tab), contentDescription = null) },
                label = { Text(tab.tabTitle()) },
            )
        }
    }
}

private fun iconFor(tab: TabsScreen): ImageVector = when (tab) {
    TabsScreen.Photos -> Icons.Filled.Photo
    TabsScreen.Articles -> Icons.Filled.Article
    TabsScreen.Profile -> Icons.Filled.AccountCircle
    else -> Icons.Filled.Photo
}
