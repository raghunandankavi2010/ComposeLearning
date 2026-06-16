package com.example.composelearning.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * **Adaptive primary navigation in one component.** [NavigationSuiteScaffold]
 * automatically picks the right navigation UI for the available width — you list
 * the destinations once and it renders:
 *
 *  - **Compact** → a **bottom navigation bar** (the classic phone pattern)
 *  - **Medium**  → a **navigation rail** down the left edge
 *  - **Expanded** → a **navigation drawer** permanently docked on the left
 *
 * This is the recommended way to do top-level navigation across form factors —
 * far less work than swapping `NavigationBar` / `NavigationRail` / drawer yourself.
 * We compute the active [NavigationSuiteType] purely to *show* it; the scaffold
 * already derives it internally from [currentWindowAdaptiveInfo].
 */
@Composable
fun AdaptiveNavigationSuiteDemo(modifier: Modifier = Modifier) {
    var current by rememberSaveable { mutableStateOf(NavDestination.HOME) }

    // Same calculation the scaffold does internally — handy for teaching/labels.
    val suiteType = NavigationSuiteScaffoldDefaults
        .calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            NavDestination.entries.forEach { dest ->
                item(
                    selected = dest == current,
                    onClick = { current = dest },
                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                    label = { Text(dest.label) }
                )
            }
        }
    ) {
        DestinationContent(destination = current, suiteType = suiteType)
    }
}

@Composable
private fun DestinationContent(
    destination: NavDestination,
    suiteType: NavigationSuiteType
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                destination.icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
            Text(destination.label, style = MaterialTheme.typography.headlineMedium)
            Text(
                "Current navigation UI: ${suiteType.readableName()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Resize the window or unfold the device: the same destinations move " +
                    "between a bottom bar (compact), a rail (medium) and a drawer (expanded) " +
                    "— with no code change on your side.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Friendly name for the otherwise opaque [NavigationSuiteType] value. */
private fun NavigationSuiteType.readableName(): String = when (this) {
    NavigationSuiteType.NavigationBar -> "Bottom navigation bar (compact)"
    NavigationSuiteType.NavigationRail -> "Navigation rail (medium)"
    NavigationSuiteType.NavigationDrawer -> "Navigation drawer (expanded)"
    else -> "None / hidden"
}
