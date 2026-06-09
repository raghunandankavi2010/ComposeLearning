package com.example.composelearning.riveo.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.composelearning.riveo.domain.model.Project
import kotlinx.coroutines.launch

private val CardHeight = 150.dp
private val CardPadding = 16.dp
private val CardCorner = 16.dp

/**
 * One Riveo project row.
 *
 * Layers, back to front:
 *  1. a red "delete" background with a trash icon — revealed as the page curls away;
 *  2. the card content (photo + labels) carrying the [Modifier.pageCurl] render effect.
 *
 * Dragging left peels the page; releasing springs it flat (`pointer` animates back to
 * `origin`). The drag values are held in an [Animatable]/state and read only inside the
 * `graphicsLayer` block, so dragging invalidates the draw layer without recomposing.
 */
@Composable
fun PageCurlCard(
    project: Project,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    // pointer == origin  ⇒  dx = 0  ⇒  flat page (the resting state).
    val pointer = remember { Animatable(0f) }
    var origin by remember { mutableFloatStateOf(0f) }

    // A fresh shader per card — it carries this card's uniforms.
    val shader = remember { createPageCurlShader() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CardHeight)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        origin = offset.x
                        scope.launch { pointer.snapTo(offset.x) }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        scope.launch { pointer.snapTo(change.position.x) }
                    },
                    onDragEnd = { scope.launch { pointer.springBack(origin) } },
                    onDragCancel = { scope.launch { pointer.springBack(origin) } },
                )
            },
    ) {
        // 1. Delete background (revealed by the curl).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardPadding)
                .clip(RoundedCornerShape(CardCorner))
                .background(Color(0xFFE53935)),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp)
                    .size(32.dp),
            )
        }

        // 2. Curling page.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pageCurl(
                    shader = shader,
                    pointer = { pointer.value },
                    origin = { origin },
                    padding = CardPadding,
                    cornerRadius = CardCorner,
                ),
        ) {
            CardContent(project)
        }
    }
}

/** Animates the curl flat over ~450 ms, matching the original demo's release timing. */
private suspend fun Animatable<Float, *>.springBack(target: Float) {
    animateTo(target, animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing))
}

@Composable
private fun CardContent(project: Project) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CardPadding)
            .clip(RoundedCornerShape(CardCorner)),
    ) {
        AsyncImage(
            model = project.imageUrl,
            contentDescription = project.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Scrim for text legibility.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.55f),
                    ),
                ),
        )

        Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
            Text(
                text = project.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
            )
            // Accent label strip.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(project.accentColor).copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Label(Icons.Default.Storage, project.size)
                Label(Icons.Default.CalendarMonth, "Just now")
                Label(Icons.Default.Schedule, project.duration)
            }
        }
    }
}

@Composable
private fun Label(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}
