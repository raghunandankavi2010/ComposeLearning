package com.example.composelearning.adaptive

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The set of top-level destinations shared by the two navigation demos
 * ([AdaptiveNavigationSuiteDemo] and [AdaptiveDrawerDemo]). Real apps would back
 * each of these with its own screen / nav graph; here each just shows a label.
 */
enum class NavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.Person),
    SETTINGS("Settings", Icons.Default.Settings)
}
