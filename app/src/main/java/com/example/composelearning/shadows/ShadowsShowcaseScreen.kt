package com.example.composelearning.shadows

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val Neuro = Color(0xFFE6E9F0)
private val NeuroLight = Color(0xFFFFFFFF)
private val NeuroDark = Color(0xFFA3B1C6)

private val NeonPink = Color(0xFFFF4D9B)
private val NeonCyan = Color(0xFF22D3EE)
private val NeonAmber = Color(0xFFFACC15)
private val NeonViolet = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowsShowcaseScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shadow playground") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item { Section(title = "1. Elevation steps", caption = "Modifier.shadow with increasing elevation") { ElevationStepsDemo() } }
            item { Section(title = "2. Animated elevation", caption = "Drag the slider — graphicsLayer-backed Modifier.shadow") { AnimatedElevationDemo() } }
            item { Section(title = "3. Colored shadows", caption = "ambientColor and spotColor on Modifier.shadow (Android 9+)") { ColoredElevationDemo() } }
            item { Section(title = "4. Static drop shadow", caption = "Modifier.dropShadow with Shadow(radius, color, offset, spread)") { StaticDropShadowDemo() } }
            item { Section(title = "5. Stacked drop shadows", caption = "Layered shadows for a richer Material 3 lift") { StackedDropShadowsDemo() } }
            item { Section(title = "6. Gradient brush shadow", caption = "Shadow accepts a Brush instead of a Color") { BrushDropShadowDemo() } }
            item { Section(title = "7. Animated drop shadow (block scope)", caption = "DropShadowScope lambda — animates without recomposition") { AnimatedDropShadowDemo() } }
            item { Section(title = "8. Inner shadow", caption = "Modifier.innerShadow — inset / pressed look") { InnerShadowDemo() } }
            item { Section(title = "9. Neumorphism", caption = "Raised vs inset combining drop + inner shadows") { NeumorphismDemo() } }
            item { Section(title = "10. Press to lift", caption = "Dynamic shadow that responds to touch") { PressToLiftDemo() } }
            item { Section(title = "11. Shape variants", caption = "Circle, rounded, custom GenericShape — all shadow-aware") { ShapeVariantsDemo() } }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun Section(title: String, caption: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        content()
    }
}

@Composable
private fun ElevationStepsDemo() {
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOf(1.dp, 4.dp, 10.dp, 18.dp, 28.dp).forEach { elevation ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(elevation = elevation, shape = shape)
                        .background(surface, shape),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${elevation.value.toInt()}dp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AnimatedElevationDemo() {
    var elevation by remember { mutableFloatStateOf(12f) }
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(24.dp)
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 24.dp)
                .shadow(elevation.dp, shape)
                .background(surface, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${elevation.toInt()} dp",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(12.dp))
        Slider(
            value = elevation,
            onValueChange = { elevation = it },
            valueRange = 0f..40f,
        )
    }
}

@Composable
private fun ColoredElevationDemo() {
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(22.dp)
    val swatches = listOf(NeonPink, NeonCyan, NeonAmber, NeonViolet)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        swatches.forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = shape,
                        ambientColor = color,
                        spotColor = color,
                    )
                    .background(surface, shape),
            )
        }
    }
}

@Composable
private fun StaticDropShadowDemo() {
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShadowedTile(
            label = "soft",
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .dropShadow(
                    shape = shape,
                    shadow = Shadow(
                        radius = 30.dp,
                        color = Color.Black.copy(alpha = 0.22f),
                        offset = DpOffset(0.dp, 10.dp),
                    ),
                )
                .background(surface, shape),
        )
        ShadowedTile(
            label = "long",
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .dropShadow(
                    shape = shape,
                    shadow = Shadow(
                        radius = 16.dp,
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = DpOffset(14.dp, 18.dp),
                        spread = 0.dp,
                    ),
                )
                .background(surface, shape),
        )
    }
}

@Composable
private fun StackedDropShadowsDemo() {
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 18.dp)
            .dropShadow(shape, Shadow(radius = 48.dp, color = Color.Black.copy(alpha = 0.20f), offset = DpOffset(0.dp, 24.dp)))
            .dropShadow(shape, Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.16f), offset = DpOffset(0.dp, 12.dp)))
            .dropShadow(shape, Shadow(radius = 6.dp, color = Color.Black.copy(alpha = 0.12f), offset = DpOffset(0.dp, 3.dp)))
            .background(surface, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text("3 stacked shadows", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BrushDropShadowDemo() {
    val brush = Brush.linearGradient(
        colors = listOf(NeonPink, NeonViolet, NeonCyan),
    )
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 18.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 36.dp,
                    brush = brush,
                    offset = DpOffset(0.dp, 18.dp),
                    alpha = 0.85f,
                ),
            )
            .background(surface, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text("brush shadow", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AnimatedDropShadowDemo() {
    val transition = rememberInfiniteTransition(label = "shadowOrbit")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 24.dp)
            // Block-scope: only the lambda re-evaluates on each frame, not the composable.
            .dropShadow(shape) {
                val r = 22.dp.toPx()
                this.offset = Offset(cos(angle) * r, sin(angle) * r)
                this.radius = 28.dp.toPx()
                this.spread = 2.dp.toPx()
                this.color = primary
                this.alpha = 0.55f
            }
            .background(surface, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = primary,
        )
    }
}

@Composable
private fun InnerShadowDemo() {
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .background(surface, shape)
                .innerShadow(
                    shape = shape,
                    shadow = Shadow(
                        radius = 22.dp,
                        color = Color.Black.copy(alpha = 0.45f),
                        offset = DpOffset(0.dp, 6.dp),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("inset", color = MaterialTheme.colorScheme.onSurface)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .background(surface, shape)
                .innerShadow(
                    shape = shape,
                    shadow = Shadow(
                        radius = 18.dp,
                        brush = Brush.linearGradient(listOf(NeonViolet, NeonPink)),
                        offset = DpOffset(0.dp, 8.dp),
                        alpha = 0.7f,
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("brush inset", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun NeumorphismDemo() {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Neuro)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Raised
        Box(
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .dropShadow(shape, Shadow(radius = 18.dp, color = NeuroLight, offset = DpOffset((-8).dp, (-8).dp)))
                .dropShadow(shape, Shadow(radius = 18.dp, color = NeuroDark, offset = DpOffset(8.dp, 8.dp), alpha = 0.6f))
                .background(Neuro, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text("raised", color = Color(0xFF334155))
        }
        // Inset
        Box(
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
                .background(Neuro, shape)
                .innerShadow(shape, Shadow(radius = 14.dp, color = NeuroDark, offset = DpOffset(6.dp, 6.dp), alpha = 0.6f))
                .innerShadow(shape, Shadow(radius = 14.dp, color = NeuroLight, offset = DpOffset((-6).dp, (-6).dp))),
            contentAlignment = Alignment.Center,
        ) {
            Text("inset", color = Color(0xFF334155))
        }
    }
}

@Composable
private fun PressToLiftDemo() {
    var pressed by remember { mutableStateOf(false) }
    val radius by animateFloatAsState(if (pressed) 6f else 28f, animationSpec = tween(220), label = "radius")
    val offsetY by animateDpAsState(if (pressed) 2.dp else 14.dp, animationSpec = tween(220), label = "offsetY")
    val alpha by animateFloatAsState(if (pressed) 0.18f else 0.42f, animationSpec = tween(220), label = "alpha")
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, animationSpec = tween(220), label = "scale")

    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 24.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            }
            .dropShadow(shape) {
                this.radius = radius.dp.toPx()
                this.offset = Offset(0f, offsetY.toPx())
                this.color = primary
                this.alpha = alpha
            }
            .background(surface, shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (pressed) "pressed" else "press & hold",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ShapeVariantsDemo() {
    val surface = MaterialTheme.colorScheme.surface
    val accent = NeonViolet
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .dropShadow(CircleShape, Shadow(radius = 26.dp, color = accent, offset = DpOffset(0.dp, 12.dp), alpha = 0.55f))
                .background(surface, CircleShape),
        )
        Spacer(Modifier.width(4.dp))
        // Pill
        Box(
            modifier = Modifier
                .height(96.dp)
                .weight(1f)
                .dropShadow(RoundedCornerShape(percent = 50), Shadow(radius = 22.dp, color = NeonCyan, offset = DpOffset(0.dp, 10.dp), alpha = 0.5f))
                .background(surface, RoundedCornerShape(percent = 50)),
            contentAlignment = Alignment.Center,
        ) {
            Text("pill", style = MaterialTheme.typography.titleMedium)
        }
        // Custom shape
        val blobShape = remember {
            GenericShape { size, _ ->
                val w = size.width
                val h = size.height
                moveTo(w * 0.20f, h * 0.05f)
                cubicTo(w * 0.85f, 0f, w, h * 0.30f, w * 0.92f, h * 0.55f)
                cubicTo(w * 0.85f, h * 0.95f, w * 0.30f, h, w * 0.10f, h * 0.80f)
                cubicTo(0f, h * 0.55f, 0f, h * 0.20f, w * 0.20f, h * 0.05f)
                close()
            }
        }
        Box(
            modifier = Modifier
                .size(96.dp)
                .dropShadow(blobShape, Shadow(radius = 26.dp, color = NeonAmber, offset = DpOffset(0.dp, 12.dp), alpha = 0.6f))
                .background(surface, blobShape),
        )
    }
}

@Composable
private fun ShadowedTile(label: String, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}