package com.example.composelearning.styles

import android.os.Parcelable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * ---------------------------------------------------------------------------
 * LAYER 4 — EXTENSIONS FOR YOUR EXISTING NAVIGATION
 * ---------------------------------------------------------------------------
 *
 * This app's main graph is Navigation 3 (`entryProvider { entry<NavKey> {} }`
 * in AppNavigation.kt), while some sub-features use classic Navigation Compose.
 * Both integrations are provided below so this destination drops into whichever
 * graph you're wiring — you never build a NavHost here.
 */

/* ===========================================================================
 * OPTION A — Navigation 3 (matches your AppNavigation.kt)  ◀ recommended
 * ===========================================================================
 *
 * A route key for the Nav3 back stack. Mirror it into your `AnimScreen` sealed
 * interface if you prefer keeping all keys in one place; a standalone key works
 * just as well.
 */
@Serializable
@Parcelize
data object StylesShowcaseKey : NavKey, Parcelable

/**
 * Registers the Styles showcase destination in a Navigation 3 entry provider,
 * wrapped in [CustomTheme] so `AppTheme.styles` resolves inside the route.
 *
 * Wire it inside the existing `entryProvider { ... }` block in AppNavigation.kt:
 *
 * ```
 * val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
 *     entry<AnimScreen.Home> { ... }
 *     // ...existing entries...
 *     stylesShowcaseEntry(onBack = { navigator.goBack() })
 * }
 * ```
 *
 * And navigate to it from anywhere via `navigator` (e.g. add it to your home
 * list and push [StylesShowcaseKey] onto the back stack).
 */
fun EntryProviderScope<NavKey>.stylesShowcaseEntry(
    onBack: () -> Unit,
) {
    entry<StylesShowcaseKey> {
        CustomTheme {
            StylesShowcaseScreen(onBack = onBack)
        }
    }
}

/* ===========================================================================
 * OPTION B — classic Navigation Compose (NavGraphBuilder)
 * ===========================================================================
 *
 * The `NavGraphBuilder.stylesShowcaseScreen(onBack)` you asked for, for use in
 * any classic `NavHost { ... }` sub-graph in the app.
 */

/** Stable route string for the classic-navigation destination. */
const val StylesShowcaseRoute = "styles_showcase"

/**
 * Registers the Styles showcase destination in a classic Navigation Compose
 * graph, wrapped in [CustomTheme] so `AppTheme.styles` resolves inside the route.
 *
 * Usage inside an existing NavHost:
 *
 * ```
 * NavHost(navController, startDestination = Home) {
 *     // ...existing composable(...) destinations...
 *     stylesShowcaseScreen(onBack = navController::popBackStack)
 * }
 * ```
 */
fun NavGraphBuilder.stylesShowcaseScreen(
    onBack: () -> Unit,
) {
    composable(route = StylesShowcaseRoute) {
        CustomTheme {
            StylesShowcaseScreen(onBack = onBack)
        }
    }
}
