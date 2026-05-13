package com.example.composelearning.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessLineChartScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Fitness — Daily Steps") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Scroll horizontally to load older history.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            FitnessLineChart()
            Spacer(Modifier.height(24.dp))
            ExampleLineChartCard()
        }
    }
}

@Composable
private fun ExampleLineChartCard() {
    val series = remember {
        listOf(
            LineSeries(
                label = "Heart rate (resting)",
                points = (0..23).map { LinePoint(it.toFloat(), (58 + Random(it).nextInt(-3, 6)).toFloat()) },
                smoothing = LineSmoothing.Cubic,
            ),
            LineSeries(
                label = "Heart rate (active avg)",
                points = (0..23).map { LinePoint(it.toFloat(), (88 + Random(it + 100).nextInt(-5, 12)).toFloat()) },
                smoothing = LineSmoothing.Cubic,
                showArea = false,
                dashed = true,
            ),
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Heart rate (24h)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LineChart(
                series = series,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                spec = LineChartSpec(
                    xAxis = ChartAxis(
                        tickCount = 6,
                        labelFormatter = { "${it.toInt()}h" },
                    ),
                    yAxis = ChartAxis(
                        tickCount = 4,
                        labelFormatter = { "${it.toInt()}" },
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarChartShowcaseScreen(onBack: () -> Unit) {
    var mode by remember { mutableStateOf(BarMode.Grouped) }
    val entries = remember {
        listOf(
            BarEntry("Mon", listOf(7200f, 5400f, 1800f)),
            BarEntry("Tue", listOf(8400f, 4900f, 2200f)),
            BarEntry("Wed", listOf(6100f, 6300f, 1500f)),
            BarEntry("Thu", listOf(9100f, 5100f, 2700f)),
            BarEntry("Fri", listOf(7600f, 4400f, 1900f)),
            BarEntry("Sat", listOf(10400f, 3900f, 3300f)),
            BarEntry("Sun", listOf(4800f, 3100f, 2500f)),
        )
    }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Bar Chart") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = mode == BarMode.Grouped,
                    onClick = { mode = BarMode.Grouped },
                    label = { Text("Grouped") },
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = mode == BarMode.Stacked,
                    onClick = { mode = BarMode.Stacked },
                    label = { Text("Stacked") },
                )
            }
            Spacer(Modifier.height(16.dp))
            BarChart(
                entries = entries,
                seriesLabels = listOf("Steps", "Calories", "Active min"),
                modifier = Modifier.fillMaxWidth().height(280.dp),
                spec = BarChartSpec(
                    mode = mode,
                    yAxis = ChartAxis(
                        tickCount = 4,
                        labelFormatter = { "${(it / 1000).toInt()}k" },
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonutChartShowcaseScreen(onBack: () -> Unit) {
    val slices = remember {
        listOf(
            DonutSlice("Running", 42f),
            DonutSlice("Cycling", 30f),
            DonutSlice("Walking", 18f),
            DonutSlice("Strength", 10f),
        )
    }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Donut Chart") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Weekly activity breakdown", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            DonutChart(
                slices = slices,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            Spacer(Modifier.height(24.dp))
            DonutLegend(slices = slices)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandleChartShowcaseScreen(onBack: () -> Unit) {
    val candles = remember {
        val rng = Random(42)
        var price = 145f
        List(40) { i ->
            val open = price
            val drift = rng.nextFloat() * 6f - 3f
            val close = (open + drift).coerceAtLeast(20f)
            val high = maxOf(open, close) + rng.nextFloat() * 2.5f
            val low = (minOf(open, close) - rng.nextFloat() * 2.5f).coerceAtLeast(15f)
            val volume = rng.nextFloat() * 100f + 30f
            price = close
            Candle("D${i + 1}", open, high, low, close, volume)
        }
    }
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Candle Chart") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("ACME stock — last 40 days", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            CandleChart(
                candles = candles,
                modifier = Modifier.fillMaxWidth().height(360.dp),
                spec = CandleChartSpec(
                    showVolume = true,
                    yAxis = ChartAxis(
                        tickCount = 5,
                        labelFormatter = { "$${"%.0f".format(it)}" },
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureShowcaseScreen(onBack: () -> Unit) {
    var temp by remember { mutableFloatStateOf(22f) }
    var roomTemp by remember { mutableFloatStateOf(0f) }
    // ThermometerV2 reading: animate to oscillating value when screen opens.
    val thermometerValue by remember { mutableStateOf(78f) }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Thermometer & Temperature") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Temperature gauge — drag the indicator", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            TemperatureGaugeV2(
                value = temp,
                onValueChange = { temp = it },
                spec = TemperatureGaugeSpec(minValue = 0f, maxValue = 60f),
            )
            Spacer(Modifier.height(24.dp))
            Text("Reading: ${temp.toInt()} °C", style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(32.dp))
            Text("Thermometer — Material-themed", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            ) {
                ThermometerV2(
                    value = thermometerValue,
                    modifier = Modifier.fillMaxSize(),
                    spec = ThermometerSpec(minValue = 0f, maxValue = 100f),
                    label = "Body",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedometerNavScreen(onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Speedometer Showcase") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SpeedometerSection(title = "Speedometer1 — slider + animation") {
                com.example.composelearning.speedometer.SpeedometerScreen()
            }
            SpeedometerSection(title = "Speedometer — markers, pointer, color tiers") {
                var progress by remember { mutableStateOf(72) }
                Column {
                    com.example.composelearning.speedometer.Speedometer(progress = progress)
                    Spacer(Modifier.height(8.dp))
                    SpeedometerProgressChips(
                        options = listOf(0, 25, 50, 75, 100),
                        selected = progress,
                        onSelect = { progress = it },
                    )
                }
            }
            SpeedometerSection(title = "SpeedometerTry — vector pointer, half-arc") {
                var progress by remember { mutableStateOf(40) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.example.composelearning.speedometer.SpeedometerTry(progress = progress)
                    Spacer(Modifier.height(8.dp))
                    SpeedometerProgressChips(
                        options = listOf(0, 25, 50, 75, 100),
                        selected = progress,
                        onSelect = { progress = it },
                    )
                }
            }
            SpeedometerSection(title = "Speedometer2 — vector arc + animated needle") {
                var progress by remember { mutableStateOf(65) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.example.composelearning.speedometer.Speedometer2(progress = progress)
                    Spacer(Modifier.height(8.dp))
                    SpeedometerProgressChips(
                        options = listOf(0, 25, 50, 75, 100),
                        selected = progress,
                        onSelect = { progress = it },
                    )
                }
            }
            SpeedometerSection(title = "Speedometer3 — multi-segment polar path") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    com.example.composelearning.speedometer.Speedometer3(
                        redProgress = 25,
                        yellowProgress = 25,
                        greenProgress = 25,
                        blueProgress = 25,
                        progress = 75,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedometerSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SpeedometerProgressChips(
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text("$value") },
            )
        }
    }
}