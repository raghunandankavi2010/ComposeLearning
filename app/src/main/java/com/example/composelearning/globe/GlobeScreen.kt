package com.example.composelearning.globe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.globe.data.Country
import com.example.composelearning.globe.data.WorldAtlas

/**
 * Route for the country globe. The maths behind every pixel is written up in `GLOBE.md`
 * next to this file.
 */
@Composable
fun CountryGlobeRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val atlas by produceState<Result<WorldAtlas>?>(initialValue = null, context) {
        value = runCatching { WorldAtlas.load(context) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlobeSpace)
    ) {
        when (val result = atlas) {
            null -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = GlobeCountryPalette[1]
            )

            else -> result.fold(
                onSuccess = { CountryGlobeScreen(it) },
                onFailure = {
                    Text(
                        text = "Could not load the world atlas:\n${it.message}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun CountryGlobeScreen(atlas: WorldAtlas) {
    val state = rememberGlobeState()
    var selected by remember { mutableStateOf<Country?>(null) }
    var showLabels by remember { mutableStateOf(true) }
    var showGraticule by remember { mutableStateOf(true) }
    var sunlight by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Country Globe",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "${atlas.countries.size} countries on a sphere. Drag to spin, fling for " +
                "inertia, pinch to zoom, tap one to identify it.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.62f)
        )

        GlobeView(
            atlas = atlas,
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            showLabels = showLabels,
            showGraticule = showGraticule,
            sunlight = sunlight,
            selected = selected,
            onSelect = { selected = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlobeChip("Labels", showLabels) { showLabels = !showLabels }
            GlobeChip("Grid", showGraticule) { showGraticule = !showGraticule }
            GlobeChip("Sunlight", sunlight) { sunlight = !sunlight }
            GlobeChip("Spin", state.spinEnabled) { state.spinEnabled = !state.spinEnabled }
        }

        AnimatedVisibility(visible = selected != null) {
            selected?.let { country ->
                SelectionCard(
                    country = country,
                    onCentre = {
                        state.snapTo(country.lon, country.lat)
                        state.spinEnabled = false
                    },
                    onClear = { selected = null }
                )
            }
        }

        Text(
            text = "Natural Earth 1:50m admin-0 · " +
                "${atlas.texWidth}×${atlas.texHeight} index raster",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.32f),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun SelectionCard(country: Country, onCentre: () -> Unit, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .background(Color.White.copy(alpha = 0.06f), MaterialTheme.shapes.medium)
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = country.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "${formatArea(country.areaKm2)} · ${formatLatLon(country.lat, country.lon)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.60f)
            )
        }
        TextButton(onClick = onCentre) { Text("Centre") }
        TextButton(onClick = onClear) { Text("Clear") }
    }
}

@Composable
private fun GlobeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            labelColor = Color.White.copy(alpha = 0.75f),
            selectedContainerColor = GlobeCountryPalette[1].copy(alpha = 0.30f),
            selectedLabelColor = Color.White
        )
    )
}
