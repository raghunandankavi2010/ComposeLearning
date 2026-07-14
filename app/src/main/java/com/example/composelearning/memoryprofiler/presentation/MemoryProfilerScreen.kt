package com.example.composelearning.memoryprofiler.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.memoryprofiler.domain.MemorySnapshot
import com.example.composelearning.memoryprofiler.domain.ProfilingArtifact
import kotlin.math.roundToInt

/* ─────────────────────────── Gauge palette ─────────────────────────────────
 * Declared once as top-level constants so the draw phase never allocates a
 * Color. (Color is an inline value class, but keeping them here also keeps the
 * thresholds documented in one place.)
 */
private val GaugeSafe = Color(0xFF2E7D32)
private val GaugeWarn = Color(0xFFF9A825)
private val GaugeDanger = Color(0xFFC62828)

/**
 * Stateful entry point (Route). Owns the ViewModel, lifts UI state with
 * lifecycle awareness, and funnels one-shot effects into the snackbar. All the
 * actual layout lives in the stateless [MemoryProfilerScreen] below.
 */
@Composable
fun MemoryProfilerRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: MemoryProfilerViewModel = viewModel(
        factory = remember(context) { MemoryProfilerViewModel.Factory(context) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Effects are collected only while STARTED and never leak past the screen.
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is MemoryProfilerEffect.ShowMessage -> {
                        snackbarHostState.showSnackbar(effect.text)
                    }
                }
            }
        }
    }

    MemoryProfilerScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryProfilerScreen(
    state: MemoryProfilerUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (MemoryProfilerIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Memory Profiler (API 36 · Baklava)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "guard") { GuardBanner(state = state) }
            item(key = "metrics") { MemoryMetricsCard(snapshot = state.snapshot) }
            item(key = "actions") {
                DiagnosticActionsCard(state = state, onIntent = onIntent)
            }
            item(key = "log-header") {
                LogHeader(count = state.artifacts.size, onClear = { onIntent(MemoryProfilerIntent.ClearLog) })
            }
            if (state.artifacts.isEmpty()) {
                item(key = "log-empty") { EmptyLog() }
            } else {
                items(items = state.artifacts, key = { it.id }) { artifact ->
                    ArtifactRow(artifact = artifact)
                }
            }
        }
    }
}

/* ───────────────────────────── Guard banner ───────────────────────────────*/

@Composable
private fun GuardBanner(state: MemoryProfilerUiState) {
    val active = state.isGuardServiceActive
    val (container, content, title, subtitle) = when (val guard = state.guard) {
        GuardStatus.Checking -> BannerData(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Resolving platform capability…",
            "Checking ProfilingManager availability"
        )
        is GuardStatus.Unsupported -> BannerData(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Guard service unavailable",
            guard.reason
        )
        GuardStatus.Ready -> if (active) {
            BannerData(
                Color(0xFF1B5E20),
                Color.White,
                "Guard service active",
                "ANR-anomaly & app-fully-drawn triggers bound to ProfilingManager"
            )
        } else {
            BannerData(
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer,
                "Guard service ready — not bound",
                "Tap “Register guard service” to arm the event-driven triggers"
            )
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = container)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (active) Color(0xFF69F0AE) else content.copy(alpha = 0.6f))
            )
            Icon(Icons.Filled.Shield, contentDescription = null, tint = content)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = content)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.9f))
            }
        }
    }
}

private data class BannerData(
    val container: Color,
    val content: Color,
    val title: String,
    val subtitle: String
)

/* ───────────────────────────── Metrics card ───────────────────────────────*/

@Composable
private fun MemoryMetricsCard(snapshot: MemorySnapshot) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text("Managed heap pressure", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))

            // Smoothly animate toward the newest reading. The gauge reads this
            // value inside its draw lambda (a deferred read of animation state),
            // so a moving number never recomposes the gauge — it only redraws.
            val animatedFraction = animateFloatAsState(
                targetValue = snapshot.heapUsedFraction,
                label = "heapFraction"
            )

            // derivedStateOf: the percentage label recomputes only when the
            // rounded integer changes, not on every animation frame.
            val percentText by remember {
                derivedStateOf { "${(animatedFraction.value * 100).roundToInt()}%" }
            }

            Text(
                text = percentText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            MemoryGauge(
                fraction = { animatedFraction.value },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            )
            Spacer(Modifier.height(16.dp))

            MetricRow("Total allocations", snapshot.heapAllocatedBytes.toReadableSize())
            MetricRow("Runtime max memory", snapshot.heapMaxBytes.toReadableSize())
            MetricRow("App free memory", snapshot.heapFreeBytes.toReadableSize())
            MetricRow("Native heap allocated", snapshot.nativeAllocatedBytes.toReadableSize())
            MetricRow("System available RAM", snapshot.systemAvailableBytes.toReadableSize())
            MetricRow(
                "System low-memory",
                if (snapshot.systemLowMemory) "YES" else "no",
                valueColor = if (snapshot.systemLowMemory) GaugeDanger else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

/**
 * A minimal, allocation-free horizontal gauge. [fraction] is a lambda so its
 * read is deferred into the draw phase; the track brush is built once per size
 * via [drawWithCache], and the fill color is chosen from cheap value-class
 * constants — nothing is allocated per frame.
 */
@Composable
private fun MemoryGauge(
    fraction: () -> Float,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .drawWithCache {
                onDrawBehind {
                    val f = fraction().coerceIn(0f, 1f)
                    drawRect(color = trackColor)
                    val fillColor = when {
                        f < 0.60f -> GaugeSafe
                        f < 0.85f -> GaugeWarn
                        else -> GaugeDanger
                    }
                    drawRect(color = fillColor, size = Size(size.width * f, size.height))
                }
            }
    )
}

/* ─────────────────────────── Diagnostic actions ───────────────────────────*/

@Composable
private fun DiagnosticActionsCard(
    state: MemoryProfilerUiState,
    onIntent: (MemoryProfilerIntent) -> Unit
) {
    val triggersEnabled = state.guard is GuardStatus.Ready
    // On-demand heap dumps only need the base ProfilingManager (API 35+),
    // independent of whether the Baklava trigger API is present.
    val serviceAvailable = state.profilingServiceAvailable

    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Diagnostic actions", style = MaterialTheme.typography.titleMedium)

            // Register / unregister the event-driven guard triggers.
            if (state.triggersRegistered) {
                OutlinedButton(
                    onClick = { onIntent(MemoryProfilerIntent.UnregisterGuardService) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Unregister guard service")
                }
            } else {
                Button(
                    onClick = { onIntent(MemoryProfilerIntent.RegisterGuardService) },
                    enabled = triggersEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Register guard service (ANR + fully-drawn)")
                }
            }

            // Controlled memory spike.
            if (state.isSimulatingSpike) {
                FilledTonalButton(
                    onClick = { onIntent(MemoryProfilerIntent.ReleaseMemorySpike) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Release ballast (${state.spikeHeldBytes.toReadableSize()})")
                }
            } else {
                FilledTonalButton(
                    onClick = { onIntent(MemoryProfilerIntent.SimulateMemorySpike) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Simulate memory spike / anomaly")
                }
            }

            // On-demand heap dump.
            Button(
                onClick = { onIntent(MemoryProfilerIntent.TriggerOnDemandDump) },
                enabled = serviceAvailable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Camera, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Trigger on-demand heap dump")
            }

            state.lastMemoryPressure?.let { pressure ->
                Text(
                    "Last memory-pressure signal: $pressure",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ───────────────────────────── Artifact log ───────────────────────────────*/

@Composable
private fun LogHeader(count: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Intercepted artifacts ($count)", style = MaterialTheme.typography.titleMedium)
        if (count > 0) {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear log")
            }
        }
    }
}

@Composable
private fun EmptyLog() {
    Card {
        Text(
            "No profiling artifacts yet. Register the guard service, fire an on-demand dump, " +
                "or push memory until the OS trims the process.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ArtifactRow(artifact: ProfilingArtifact) {
    val accent by animateColorAsState(
        targetValue = if (artifact.isSuccess) GaugeSafe else GaugeDanger,
        label = "artifactAccent"
    )
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(artifact.origin.label, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = artifact.filePath ?: (artifact.errorMessage ?: "no file path"),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            if (artifact.isSuccess) {
                Text(
                    "${artifact.fileSizeBytes / 1024} KB",
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    "ERR ${artifact.errorCode}",
                    style = MaterialTheme.typography.labelLarge,
                    color = GaugeDanger
                )
            }
        }
    }
}

/* ─────────────────────────────── Helpers ──────────────────────────────────*/

/** Formats a byte count as a human-readable size. Never called from the draw phase. */
private fun Long.toReadableSize(): String {
    if (this < 1024) return "$this B"
    val kb = this / 1024.0
    if (kb < 1024) return "${kb.roundToInt()} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    return String.format("%.2f GB", mb / 1024.0)
}

/* ─────────────────────────────── Previews ─────────────────────────────────*/

@Preview(showBackground = true)
@Composable
private fun MemoryProfilerScreenPreview() {
    MaterialTheme {
        MemoryProfilerScreen(
            state = MemoryProfilerUiState(
                guard = GuardStatus.Ready,
                profilingServiceAvailable = true,
                triggersRegistered = true,
                snapshot = MemorySnapshot(
                    capturedAtMs = 0L,
                    heapAllocatedBytes = 180L * 1024 * 1024,
                    heapMaxBytes = 256L * 1024 * 1024,
                    heapFreeBytes = 76L * 1024 * 1024,
                    nativeAllocatedBytes = 42L * 1024 * 1024,
                    systemAvailableBytes = 900L * 1024 * 1024,
                    systemThresholdBytes = 128L * 1024 * 1024,
                    systemLowMemory = false
                ),
                artifacts = listOf(
                    ProfilingArtifact(
                        id = 2,
                        filePath = "/data/user/0/com.example/cache/profiling/heap_2.perfetto-trace",
                        fileSizeBytes = 5_242_880,
                        errorCode = 0,
                        errorMessage = null,
                        receivedAtMs = 0L,
                        origin = ProfilingArtifact.Origin.ON_DEMAND_HEAP_DUMP
                    ),
                    ProfilingArtifact(
                        id = 1,
                        filePath = null,
                        fileSizeBytes = 0,
                        errorCode = 3,
                        errorMessage = "Rate limited",
                        receivedAtMs = 0L,
                        origin = ProfilingArtifact.Origin.TRIGGER_ANR
                    )
                ),
                lastMemoryPressure = "RUNNING_LOW"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onBack = {}
        )
    }
}
