package com.example.composelearning.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A real-world adaptive example showing Bangalore temples.
 *
 * This demo uses [NavigableListDetailPaneScaffold] to show a list of temples and
 * their comprehensive details (location, transport, facilities) side-by-side
 * on wide screens, and as separate screens on narrow ones.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TempleShowcaseDemo(modifier: Modifier = Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {
            AnimatedPane {
                TempleList(
                    selectedId = navigator.currentDestination?.contentKey,
                ) { id ->
                    scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val id = navigator.currentDestination?.contentKey
                val temple = remember(id) { sampleTemples.firstOrNull { it.id == id } }
                if (temple == null) {
                    TemplePlaceholder()
                } else {
                    TempleDetail(
                        temple = temple,
                        showBack = navigator.canNavigateBack(),
                        onBack = { scope.launch { navigator.navigateBack() } }
                    )
                }
            }
        }
    )
}

@Composable
private fun TempleList(
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
    Surface {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    "Spiritual Bangalore",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(sampleTemples, key = { it.id }) { temple ->
                val selected = temple.id == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onSelect(temple.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(temple.accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                temple.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                temple.location,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TempleDetail(
    temple: Temple,
    showBack: Boolean,
    onBack: () -> Unit
) {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    temple.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1
                )
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description
                Text(temple.description, style = MaterialTheme.typography.bodyLarge)

                // Location & Timings
                InfoSection(title = "Visiting Info", icon = Icons.Default.Info) {
                    DetailRow(label = "Location", value = temple.location)
                    DetailRow(label = "Timings", value = temple.timings)
                }

                // How to Reach
                InfoSection(title = "How to Reach", icon = Icons.Default.LocationOn) {
                    DetailRow(label = "Road", value = temple.reach.road)
                    DetailRow(label = "Transport", value = temple.reach.transport)
                    DetailRow(label = "Own Vehicle", value = temple.reach.ownVehicle)
                }

                // Facilities
                InfoSection(title = "Facilities", icon = Icons.Default.Place) {
                    DetailRow(label = "Pooja", value = temple.facilities.pooja)
                    DetailRow(label = "Prasad", value = temple.facilities.prasad)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        temple.facilities.general.forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TemplePlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Select a temple to view details",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
