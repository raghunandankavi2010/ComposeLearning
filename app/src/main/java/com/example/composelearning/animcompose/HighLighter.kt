package com.example.composelearning.animcompose

import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


class SpotlightRegistry {
    private val _targets = mutableStateMapOf<String, Rect>()
    val targets: Map<String, Rect> get() = _targets

    fun update(id: String, rect: Rect) {
        _targets[id] = rect
    }
}

@Composable
fun Modifier.spotlightTarget(
    id: String,
    registry: SpotlightRegistry,
    extraPadding: Dp = 12.dp
): Modifier {
    val density = LocalDensity.current
    return this.then(
        Modifier.onGloballyPositioned { coords ->
            val extraPaddingPx = with(density) { extraPadding.toPx() }

            val bounds = coords.boundsInWindow()
            val rect = Rect(
                offset = bounds.topLeft - Offset(extraPaddingPx, extraPaddingPx),
                size = Size(bounds.width + 2 * extraPaddingPx, bounds.height + 2 * extraPaddingPx)
            )
            registry.update(id, rect)
        }
    )
}

class SpotlightController {
    var current by mutableStateOf<Rect?>(null)
        private set

    fun highlight(rect: Rect?) {
        current = rect
    }

    fun clear() {
        current = null
    }
}

@Composable
fun SpotlightOverlay(
    controller: SpotlightController,
    registry: SpotlightRegistry,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    dimColor: Color = Color.Black.copy(alpha = 0.65f),
    cornerRadius: Dp = 16.dp
) {
    if (!visible) return

    val cornerPx = with(LocalDensity.current) { cornerRadius.toPx() }

    var overlayCoords by remember {
        mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(
            null
        )
    }
    val targetWindow = controller.current
    val targetLocal: Rect? = remember(targetWindow, overlayCoords) {
        targetWindow?.let { r ->
            overlayCoords?.let { coords ->
                val tl = coords.windowToLocal(r.topLeft)
                Rect(tl, r.size)
            }
        }
    }

    val animated by animateRectAsState(targetValue = targetLocal ?: Rect.Zero, label = "spotRect")
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (targetLocal != null) 1f else 1f, label = "overlayAlpha"
    )

    fun hitTest(posWin: Offset): Rect? =
        registry.targets.values
            .filter { it.contains(posWin) }
            .minByOrNull { it.width * it.height }

    val rPx = with(LocalDensity.current) { 96.dp.toPx() }

    Box(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayCoords = it }
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen, alpha = alpha)
            .pointerInput(registry, rPx) {
                detectTapGestures { posLocal ->
                    val posWin = overlayCoords?.localToWindow(posLocal) ?: posLocal
                    val hit = hitTest(posWin)
                    val fallback =
                        Rect(offset = posWin - Offset(rPx, rPx), size = Size(rPx * 2, rPx * 2))
                    controller.highlight(hit ?: fallback)
                }
            }
            .drawWithContent {
                drawContent()
                drawRect(dimColor)

                if (targetLocal != null && animated.width > 1f && animated.height > 1f) {
                    val glowRadius = animated.maxDimension * 0.75f
                    if (glowRadius > 1f) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                                center = animated.center,
                                radius = glowRadius
                            ),
                            topLeft = animated.topLeft,
                            size = animated.size,
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    }
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = animated.topLeft,
                        size = animated.size,
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        blendMode = BlendMode.Clear
                    )
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotlightDemoScreen(onFinish: () -> Unit = {}) {
    val registry = remember { SpotlightRegistry() }
    val controller = remember { SpotlightController() }
    var overlayVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Column(
                Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Walkthrough", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Tap any highlighted element to focus it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    overlayVisible = !overlayVisible

                    if (!overlayVisible) {
                        controller.clear()
                    } else {
                        controller.highlight(registry.targets["feature_security_cta"])
                    }
                },
                text = { Text(if (overlayVisible) "Got it" else "Show again") },
                icon = { Icon(Icons.Default.Check, contentDescription = null) }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spotlightTarget("search", registry)
                ) {
                    Text(
                        "Search settings, features…", Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip("Profile", "Edit", id = "qa_profile", registry)
                    AssistChip("Backup", "Sync", id = "qa_backup", registry)
                    AssistChip("Theme", "Dark", id = "qa_theme", registry)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FeatureCard(
                        title = "Analytics",
                        subtitle = "Understand your activity at a glance.",
                        primaryText = "Open",
                        idCard = "feature_analytics",
                        idCta = "feature_analytics_cta",
                        registry = registry
                    )
                    FeatureCard(
                        title = "Security",
                        subtitle = "Manage passkeys and 2FA.",
                        primaryText = "Manage",
                        idCard = "feature_security",
                        idCta = "feature_security_cta",
                        registry = registry,
                        outlined = true
                    )
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .spotlightTarget("settings_card", registry),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text("Notifications") },
                            supportingContent = { Text("Control alerts and badges") },
                            trailingContent = {
                                Switch(
                                    checked = true,
                                    onCheckedChange = {},
                                    modifier = Modifier.spotlightTarget("notif_switch", registry)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .spotlightTarget("notif_row", registry)
                        )
                        ListItem(
                            headlineContent = { Text("Privacy") },
                            supportingContent = { Text("Permissions, data controls") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .spotlightTarget("privacy_row", registry)
                        )
                        ListItem(
                            headlineContent = { Text("Downloads") },
                            supportingContent = { Text("Storage and offline access") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .spotlightTarget("downloads_row", registry)
                        )
                    }
                }

                Text("Recent activity", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityRow("Exported data report", "2 min ago", "activity_1", registry)
                    ActivityRow("Passkey added", "Yesterday", "activity_2", registry)
                    ActivityRow("Theme changed to Dark", "2 days ago", "activity_3", registry)
                }
            }

            SpotlightOverlay(
                controller = controller,
                registry = registry,
                modifier = Modifier.fillMaxSize(),
                visible = overlayVisible
            )
        }
    }
}

@Composable
fun RowScope.AssistChip(title: String, action: String, id: String, registry: SpotlightRegistry) {
    ElevatedCard(
        modifier = Modifier
            .weight(1f)
            .spotlightTarget(id, registry),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                action, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun RowScope.FeatureCard(
    title: String,
    subtitle: String,
    primaryText: String,
    idCard: String,
    idCta: String,
    registry: SpotlightRegistry,
    outlined: Boolean = false
) {
    val shape = RoundedCornerShape(20.dp)
    val modifier = Modifier
        .weight(1f)
        .spotlightTarget(idCard, registry)

    if (outlined) {
        OutlinedCard(modifier = modifier, shape = shape) {
            FeatureCardBody(title, subtitle, primaryText, idCta, registry)
        }
    } else {
        ElevatedCard(modifier = modifier, shape = shape) {
            FeatureCardBody(title, subtitle, primaryText, idCta, registry)
        }
    }
}


@Composable
private fun FeatureCardBody(
    title: String,
    subtitle: String,
    primaryText: String,
    idCta: String,
    registry: SpotlightRegistry
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = {},
                modifier = Modifier.spotlightTarget(idCta, registry)
            ) { Text(primaryText) }
        }
    }
}

@Composable
private fun ActivityRow(title: String, time: String, id: String, registry: SpotlightRegistry) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .spotlightTarget(id, registry),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(14.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    time, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}