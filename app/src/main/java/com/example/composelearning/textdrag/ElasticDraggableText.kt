package com.example.composelearning.textdrag

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * An elastic, "jelly" draggable text — a Compose take on Shubham Singh's SwiftUI
 * demo (linkedin.com/posts/shubham0812_…-7487796171357192192).
 *
 * The word is laid out as a [androidx.compose.foundation.layout.Row] of individual
 * character cells. The whole component is dragged vertically; every character owns
 * its own [Animatable] Y offset and *chases* the finger through a spring whose
 * stiffness depends on how far that character sits from where you grabbed:
 *
 *  - Characters near the grab point are stiff → they lead the motion.
 *  - Characters far from it are soft → they lag, so the word bends into a wave
 *    while you move.
 *
 * Each per-character offset is also weighted by a bell curve of that distance, so
 * even when you hold still the word settles into an arc peaking under your finger.
 * The spring is under-damped, so on release every letter overshoots and bounces
 * back to rest — the far, softer letters ringing a beat behind the near ones.
 */
@Composable
fun ElasticDraggableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold
    )
) {
    val chars = remember(text) { text.toList() }
    val n = chars.size

    // Raw vertical finger displacement, in pixels. 0 = at rest.
    var dragY by remember { mutableFloatStateOf(0f) }
    // Fraction (0..1) along the word where the drag was started — the wave's peak.
    var anchorFraction by remember { mutableFloatStateOf(0.5f) }
    // Width of the whole word in pixels, for mapping touch-x → anchor fraction.
    var widthPx by remember { mutableFloatStateOf(1f) }

    // One independent spring-following offset per character.
    val offsets = remember(text) { List(n) { Animatable(0f) } }

    // Each character chases `dragY` with its own spring. collectLatest cancels the
    // in-flight animation and re-targets from the current position/velocity every
    // time the finger moves, so the motion stays continuous.
    offsets.forEachIndexed { index, anim ->
        val charFraction = if (n <= 1) 0.5f else index / (n - 1f)
        androidx.compose.runtime.LaunchedEffect(anim) {
            snapshotFlow { dragY to anchorFraction }.collectLatest { (target, anchor) ->
                val dist = abs(charFraction - anchor)          // 0 at finger, →1 at far end
                val nearness = 1f - dist                        // 1 at finger, →0 at far end

                // Nearer letters lead (stiff), farther letters trail (soft) → wave.
                val stiffness = lerp(28f, 650f, nearness)
                // Nearer letters take the full pull; farther ones lag into an arc.
                val weight = lerp(0.35f, 1f, nearness)

                anim.animateTo(
                    targetValue = target * weight,
                    animationSpec = spring(
                        dampingRatio = 0.32f,   // under-damped → bouncy overshoot
                        stiffness = stiffness
                    )
                )
            }
        }
    }

    val scope = rememberCoroutineScope()

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(text) {
                detectDragGestures(
                    onDragStart = { start ->
                        anchorFraction = (start.x / widthPx).coerceIn(0f, 1f)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragY += amount.y
                    },
                    onDragEnd = { dragY = 0f },
                    onDragCancel = { dragY = 0f }
                )
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        chars.forEachIndexed { index, c ->
            Text(
                text = if (c == ' ') " " else c.toString(),
                style = style,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    translationY = offsets[index].value
                }
            )
        }
    }

    // Guard: if the text changes while dragging, snap everything back.
    androidx.compose.runtime.DisposableEffect(text) {
        onDispose {
            scope.launch { offsets.forEach { it.snapTo(0f) } }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElasticDraggableTextScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Elastic Draggable Text") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ElasticDraggableText(text = "Draggable")

            Box(Modifier.padding(top = 48.dp)) {
                Text(
                    text = "Drag the word up or down and let go —\neach letter springs back with a bounce.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
