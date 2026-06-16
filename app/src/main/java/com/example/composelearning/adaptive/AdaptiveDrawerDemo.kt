package com.example.composelearning.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * An **adaptive navigation drawer** built by hand from the two drawer primitives,
 * choosing between them based on width:
 *
 *  - **Expanded** (tablet / unfolded foldable / desktop) → [PermanentNavigationDrawer].
 *    The drawer is always on screen beside the content; there is no hamburger button.
 *  - **Compact / Medium** (phone) → [ModalNavigationDrawer]. The drawer is hidden and
 *    slides in over the content when the user taps the menu icon, dimming the rest.
 *
 * Both share the **same** drawer sheet composable ([DrawerItems]) and the same content
 * ([DrawerBody]) — only the container differs. That separation is the whole trick:
 * write the pieces once, pick the container per size class.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveDrawerDemo(modifier: Modifier = Modifier) {
    val widthClass = rememberAdaptiveWidthClass()
    val permanent = widthClass == AdaptiveWidthClass.EXPANDED

    var current by rememberSaveable { mutableStateOf(NavDestination.HOME) }

    if (permanent) {
        PermanentNavigationDrawer(
            modifier = modifier,
            drawerContent = {
                PermanentDrawerSheet {
                    DrawerItems(current = current, onSelect = { current = it })
                }
            }
        ) {
            DrawerBody(current = current, showMenu = false, onMenuClick = {})
        }
    } else {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerItems(
                        current = current,
                        onSelect = {
                            current = it
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            DrawerBody(
                current = current,
                showMenu = true,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@Composable
private fun DrawerItems(
    current: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        "ComposeLearning",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
    )
    NavDestination.entries.forEach { dest ->
        NavigationDrawerItem(
            icon = { Icon(dest.icon, contentDescription = null) },
            label = { Text(dest.label) },
            selected = dest == current,
            onClick = { onSelect(dest) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerBody(
    current: NavDestination,
    showMenu: Boolean,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.label) },
                navigationIcon = {
                    if (showMenu) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(current.icon, contentDescription = null)
            Text(current.label, style = MaterialTheme.typography.headlineMedium)
            Text(
                if (showMenu) {
                    "Compact/medium width → ModalNavigationDrawer. Tap the menu icon to slide " +
                        "the drawer in over this content."
                } else {
                    "Expanded width → PermanentNavigationDrawer. The drawer is docked on the " +
                        "left at all times, so there is no menu button."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
