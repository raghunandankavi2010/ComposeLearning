package com.example.composelearning.animcompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Home screen: a search field over the whole catalog plus one card per
 * [FeatureGroup]. Tapping a group opens [GroupFeaturesScreen] with just that
 * group's demos; typing a query replaces the group list with flat results.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(navigator: Navigator) {
    var query by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    val searchResults = remember(query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            FeatureCatalog.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jetpack Compose Animations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search ${FeatureCatalog.size} demos…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            val groups = remember { FeatureCatalog.groupBy { it.group } }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (query.isBlank()) {
                    items(
                        items = FeatureGroup.entries,
                        key = { "group_${it.name}" }
                    ) { group ->
                        GroupCard(
                            group = group,
                            count = groups[group]?.size ?: 0,
                            onClick = {
                                navigator.navigate(com.example.composelearning.AnimScreen.Group(group.name))
                            }
                        )
                    }
                } else {
                    if (searchResults.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(bottom = 64.dp), // Adjust for search bar height
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                    text = "No demos match your search",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(
                            items = searchResults,
                            key = { "result_${it.title}" }
                        ) { category ->
                            CategoryCard(
                                title = category.title,
                                description = category.description,
                                badge = category.group.title,
                                onClick = { navigator.navigate(category.route) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Second navigation level: every demo belonging to one [FeatureGroup]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFeaturesScreen(
    groupId: String,
    navigator: Navigator
) {
    val group = remember(groupId) { FeatureGroup.valueOf(groupId) }
    val features = remember(group) { FeatureCatalog.filter { it.group == group } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = features,
                key = { it.title }
            ) { category ->
                CategoryCard(
                    title = category.title,
                    description = category.description,
                    onClick = { navigator.navigate(category.route) }
                )
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: FeatureGroup,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("$count")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    badge: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (badge != null) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
