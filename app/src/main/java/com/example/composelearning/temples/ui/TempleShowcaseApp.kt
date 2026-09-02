package com.example.composelearning.temples.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.R
import kotlinx.coroutines.launch

/** Top-level destinations. Rendered as a bottom bar, rail or drawer depending on width. */
enum class TempleTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    DISCOVER(R.string.tab_discover, Icons.Default.Search),
    NEARBY(R.string.tab_nearby, Icons.Default.MyLocation),
    FESTIVALS(R.string.tab_festivals, Icons.Default.Celebration),
    SAVED(R.string.tab_saved, Icons.Default.Favorite),
    LANGUAGE(R.string.tab_settings, Icons.Default.Translate)
}

/**
 * Entry point for the Temple Showcase feature.
 *
 * Two adaptive components are nested here, each doing the job it is built for:
 *
 *  * [NavigationSuiteScaffold] handles *primary* navigation — the five tabs become a bottom
 *    bar on a phone, a rail on a medium window and a drawer on a desktop-width one.
 *  * [NavigableListDetailPaneScaffold] handles the *list → detail* relationship inside the
 *    Discover and Nearby tabs, so a tablet shows the list beside the temple page instead of
 *    replacing it.
 *
 * Selection is hoisted to this level rather than living inside each tab: opening a temple
 * from the festival calendar or the saved trail has to land on the same detail pane, so
 * there is exactly one navigator and one selected id for the whole feature.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TempleShowcaseApp(modifier: Modifier = Modifier) {
    val viewModel: TempleViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(TempleTab.DISCOVER) }
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            // Replace rather than queue: these are all "no app can do that" notices, and a
            // backlog of identical snackbars is noise.
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val openTemple: (String) -> Unit = { id ->
        // Detail only exists inside the list-detail tabs, so bounce there first.
        if (tab != TempleTab.DISCOVER && tab != TempleTab.NEARBY) tab = TempleTab.DISCOVER
        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
    }

    val callbacks = remember(viewModel) {
        TempleListCallbacks(
            onQueryChange = viewModel::onQueryChange,
            onSelect = openTemple,
            onToggleFavourite = { viewModel.onToggleFavourite(it) },
            onDeitySelected = viewModel::onDeitySelected,
            onAreaSelected = viewModel::onAreaSelected,
            onToggleOpenNow = viewModel::onToggleOpenNow,
            onToggleWheelchair = viewModel::onToggleWheelchair,
            onToggleNearMetro = viewModel::onToggleNearMetro,
            onToggleAnnadana = viewModel::onToggleAnnadana,
            onSortChange = viewModel::onSortChange,
            onClearFilters = viewModel::onClearFilters,
            onRequestLocation = viewModel::requestLocation,
            onLocationPermissionDenied = viewModel::onLocationPermissionDenied
        )
    }

    val labels = TempleTab.entries.associateWith { stringResource(it.labelRes) }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            TempleTab.entries.forEach { entry ->
                val label = labels[entry]!!
                item(
                    selected = entry == tab,
                    onClick = { tab = entry },
                    icon = { Icon(entry.icon, contentDescription = label) },
                    label = { Text(label) }
                )
            }
        }
    ) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    TempleTab.DISCOVER, TempleTab.NEARBY -> {
                        // The Nearby tab is the same list with distance sorting forced on;
                        // duplicating the whole screen for that would be a copy for nothing.
                        NavigableListDetailPaneScaffold(
                            navigator = navigator,
                            modifier = Modifier.fillMaxSize(),
                            listPane = {
                                AnimatedPane {
                                    TempleListPane(
                                        state = state,
                                        selectedId = navigator.currentDestination?.contentKey,
                                        callbacks = callbacks
                                    )
                                }
                            },
                            detailPane = {
                                AnimatedPane {
                                    val selected =
                                        state.itemFor(navigator.currentDestination?.contentKey)
                                    if (selected == null) {
                                        EmptyState(
                                            icon = Icons.Default.Place,
                                            message = stringResource(
                                                R.string.empty_select_temple
                                            )
                                        )
                                    } else {
                                        TempleDetailScreen(
                                            item = selected,
                                            showBack = navigator.canNavigateBack(),
                                            onBack = { scope.launch { navigator.navigateBack() } },
                                            onToggleFavourite = {
                                                viewModel.onToggleFavourite(selected.temple.id)
                                            },
                                            onToggleVisited = {
                                                viewModel.onToggleVisited(selected.temple.id)
                                            },
                                            onMessage = showMessage
                                        )
                                    }
                                }
                            }
                        )
                    }

                    TempleTab.FESTIVALS -> FestivalsScreen(
                        now = state.now,
                        onOpenTemple = openTemple,
                        onMessage = showMessage
                    )

                    TempleTab.SAVED -> SavedScreen(
                        state = state,
                        onOpenTemple = openTemple,
                        onToggleFavourite = { viewModel.onToggleFavourite(it) },
                        onMessage = showMessage
                    )

                    TempleTab.LANGUAGE -> LanguageScreen()
                }
            }
        }
    }

    // Entering "Near me" *is* the request to be sorted by distance — asking the user to then
    // press a button would be a second, redundant confirmation of what they just chose.
    LaunchedEffect(tab) {
        if (tab == TempleTab.NEARBY) {
            viewModel.onSortChange(TempleSort.DISTANCE)
            viewModel.requestLocation()
        }
    }
}
