package com.example.composelearning.temples.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.temples.data.TempleRepository
import com.example.composelearning.temples.platform.DeviceLocationSource
import com.example.composelearning.temples.platform.LocationState
import com.example.composelearning.temples.platform.TempleIntents
import com.example.composelearning.temples.platform.local
import kotlin.math.roundToInt

/**
 * The list pane: search, filters, a location banner, and the temple rows.
 *
 * The filter panel starts collapsed. On a phone the list is the whole screen, and four rows
 * of chips above it would push the actual content below the fold — so the common path
 * (scroll, or type a name) stays unobstructed and the facets are one tap away.
 */
@Composable
fun TempleListPane(
    state: TempleUiState,
    selectedId: String?,
    callbacks: TempleListCallbacks,
    modifier: Modifier = Modifier
) {
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "search") {
                SearchRow(
                    query = state.filters.query,
                    filtersActive = state.filters.isActive,
                    filtersExpanded = filtersExpanded,
                    onQueryChange = callbacks.onQueryChange,
                    onToggleFilters = { filtersExpanded = !filtersExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item(key = "quick_filters") {
                QuickFilterRow(state = state, callbacks = callbacks)
            }

            item(key = "facets") {
                AnimatedVisibility(visible = filtersExpanded) {
                    FacetPanel(state = state, callbacks = callbacks)
                }
            }

            item(key = "location") {
                LocationBanner(
                    location = state.location,
                    onRequestLocation = callbacks.onRequestLocation,
                    onPermissionDenied = callbacks.onLocationPermissionDenied,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item(key = "count") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.result_count, state.items.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    if (state.filters.isActive) {
                        TextButton(onClick = callbacks.onClearFilters) {
                            Text(stringResource(R.string.filter_clear))
                        }
                    }
                }
            }

            if (state.items.isEmpty()) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.empty_no_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.items, key = { it.temple.id }) { listItem ->
                TempleRow(
                    item = listItem,
                    selected = listItem.temple.id == selectedId,
                    onClick = { callbacks.onSelect(listItem.temple.id) },
                    onToggleFavourite = { callbacks.onToggleFavourite(listItem.temple.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/** Grouped so the list pane signature does not grow a dozen lambda parameters. */
data class TempleListCallbacks(
    val onQueryChange: (String) -> Unit,
    val onSelect: (String) -> Unit,
    val onToggleFavourite: (String) -> Unit,
    val onDeitySelected: (com.example.composelearning.temples.data.Deity?) -> Unit,
    val onAreaSelected: (String?) -> Unit,
    val onToggleOpenNow: () -> Unit,
    val onToggleWheelchair: () -> Unit,
    val onToggleNearMetro: () -> Unit,
    val onToggleAnnadana: () -> Unit,
    val onSortChange: (TempleSort) -> Unit,
    val onClearFilters: () -> Unit,
    val onRequestLocation: () -> Unit,
    val onLocationPermissionDenied: () -> Unit
)

@Composable
private fun SearchRow(
    query: String,
    filtersActive: Boolean,
    filtersExpanded: Boolean,
    onQueryChange: (String) -> Unit,
    onToggleFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.search_clear)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Search
            )
        )
        IconButton(onClick = onToggleFilters) {
            Icon(
                Icons.Default.Tune,
                contentDescription = stringResource(R.string.filter_title),
                tint = if (filtersActive || filtersExpanded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/** The four filters worth a permanent slot — each answers "can I go there today". */
@Composable
private fun QuickFilterRow(state: TempleUiState, callbacks: TempleListCallbacks) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = state.filters.openNow,
                onClick = callbacks.onToggleOpenNow,
                label = { Text(stringResource(R.string.filter_open_now)) }
            )
        }
        item {
            FilterChip(
                selected = state.filters.nearMetro,
                onClick = callbacks.onToggleNearMetro,
                label = { Text(stringResource(R.string.filter_metro)) }
            )
        }
        item {
            FilterChip(
                selected = state.filters.wheelchair,
                onClick = callbacks.onToggleWheelchair,
                label = { Text(stringResource(R.string.filter_wheelchair)) }
            )
        }
        item {
            FilterChip(
                selected = state.filters.annadana,
                onClick = callbacks.onToggleAnnadana,
                label = { Text(stringResource(R.string.filter_annadana)) }
            )
        }
    }
}

/** Deity, area and sort — the long facets, revealed only when asked for. */
@Composable
private fun FacetPanel(state: TempleUiState, callbacks: TempleListCallbacks) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FacetLabel(stringResource(R.string.filter_deity))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TempleRepository.deities, key = { it.name }) { deity ->
                FilterChip(
                    selected = state.filters.deity == deity,
                    onClick = { callbacks.onDeitySelected(deity) },
                    label = { Text(stringResource(deity.labelRes)) }
                )
            }
        }

        FacetLabel(stringResource(R.string.filter_area))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TempleRepository.areas, key = { it.en }) { area ->
                FilterChip(
                    selected = state.filters.area == area.en,
                    onClick = { callbacks.onAreaSelected(area.en) },
                    label = { Text(area.local()) }
                )
            }
        }

        FacetLabel(stringResource(R.string.sort_title))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TempleSort.entries.toList(), key = { it.name }) { sort ->
                val label = when (sort) {
                    TempleSort.NAME -> R.string.sort_name
                    TempleSort.DISTANCE -> R.string.sort_distance
                    TempleSort.AREA -> R.string.sort_area
                }
                FilterChip(
                    selected = state.filters.sort == sort,
                    onClick = { callbacks.onSortChange(sort) },
                    // Sorting by distance is meaningless without a fix, so it stays disabled
                    // until one arrives rather than silently doing nothing.
                    enabled = sort != TempleSort.DISTANCE ||
                        state.location is LocationState.Ready,
                    label = { Text(stringResource(label)) }
                )
            }
        }
    }
}

@Composable
private fun FacetLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * Location, as one adaptive strip rather than a dialog.
 *
 * Each state offers the single next useful action: ask for permission, open settings when it
 * was denied, switch location on when the OS toggle is off, or refresh when we have a fix.
 * Nothing here blocks the list — the guide is fully usable with location off.
 */
@Composable
fun LocationBanner(
    location: LocationState,
    onRequestLocation: () -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        // Coarse-only is fine: we sort by kilometres, not metres.
        if (grants.values.any { it }) onRequestLocation() else onPermissionDenied()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (location) {
                LocationState.Locating -> CircularProgressIndicator(Modifier.size(20.dp))
                else -> Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = if (location is LocationState.Ready) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                when (location) {
                    LocationState.Idle -> {
                        Text(
                            stringResource(R.string.location_prompt_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.location_prompt_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LocationState.Locating -> Text(
                        stringResource(R.string.location_locating),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    LocationState.PermissionRequired -> {
                        Text(
                            stringResource(R.string.location_denied_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.location_denied_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is LocationState.Unavailable -> Text(
                        stringResource(location.reasonRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    is LocationState.Ready -> {
                        Text(
                            if (location.stale) {
                                stringResource(R.string.location_stale)
                            } else {
                                stringResource(R.string.sort_distance)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        location.accuracyMeters?.let { accuracy ->
                            Text(
                                stringResource(
                                    R.string.location_accuracy,
                                    accuracy.roundToInt()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when (location) {
                LocationState.Idle -> Button(
                    onClick = {
                        permissionLauncher.launch(DeviceLocationSource.PERMISSIONS.toTypedArray())
                    }
                ) { Text(stringResource(R.string.location_grant)) }

                LocationState.PermissionRequired -> TextButton(
                    onClick = { TempleIntents.openAppSettings(context) }
                ) { Text(stringResource(R.string.location_open_settings)) }

                is LocationState.Unavailable -> TextButton(
                    onClick = { TempleIntents.openLocationSettings(context) }
                ) { Text(stringResource(R.string.location_enable)) }

                is LocationState.Ready -> IconButton(onClick = onRequestLocation) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = stringResource(R.string.location_refresh)
                    )
                }

                LocationState.Locating -> Unit
            }
        }
    }
}
