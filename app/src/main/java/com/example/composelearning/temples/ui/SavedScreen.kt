package com.example.composelearning.temples.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.R
import com.example.composelearning.temples.data.formatDistance
import com.example.composelearning.temples.platform.LocationState
import com.example.composelearning.temples.platform.TempleIntents
import com.example.composelearning.temples.platform.local

/**
 * Saved temples, doubling as a trip planner.
 *
 * Once a location fix exists the saved list is ordered nearest-first and numbered, which
 * turns "temples I like" into a route someone can actually walk or ride in one morning.
 * The consecutive distances shown are straight-line, and the header says so — road distance
 * is the maps app's job.
 */
@Composable
fun SavedScreen(
    state: TempleUiState,
    onOpenTemple: (String) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val noMaps = stringResource(R.string.error_no_maps_app)
    val saved = state.savedByDistance

    if (saved.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FavoriteBorder,
            message = stringResource(R.string.empty_no_favourites),
            modifier = modifier
        )
        return
    }

    // Sum of straight-line hops between consecutive stops, once we can order them.
    val legTotal = saved.mapNotNull { it.distanceKm }
        .zipWithNext { a, b -> (b - a).let { if (it < 0) -it else it } }
        .sum()

    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                Column {
                    Text(
                        stringResource(R.string.planner_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (state.location is LocationState.Ready) {
                            stringResource(R.string.planner_body)
                        } else {
                            stringResource(R.string.planner_needs_location)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.location is LocationState.Ready && saved.size > 1) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(
                                R.string.planner_total,
                                saved.size,
                                formatDistance(legTotal)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            itemsIndexed(saved, key = { _, item -> item.temple.id }) { index, item ->
                Column {
                    if (index > 0) {
                        PlannerConnector(modifier = Modifier.padding(start = 30.dp))
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTemple(item.temple.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(item.temple.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.temple.name.local(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OpenStatusPill(item.status)
                                    item.distanceKm?.let { km ->
                                        Text(
                                            stringResource(
                                                R.string.label_distance_away,
                                                formatDistance(km)
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            FilledTonalButton(
                                onClick = {
                                    val name = item.temple.name.en
                                    val launched = TempleIntents.navigate(
                                        context = context,
                                        point = item.temple.location,
                                        label = name,
                                        fallbackQuery = "$name, ${item.temple.address}"
                                    )
                                    if (!launched) onMessage(noMaps)
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Navigation,
                                    contentDescription = stringResource(R.string.action_navigate),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { onToggleFavourite(item.temple.id) }) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.action_unsave)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
