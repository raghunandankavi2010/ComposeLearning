package com.example.composelearning.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

private data class AvatarItem(val drawable: Int, val tint: Color)

private val AVATARS = listOf(
    AvatarItem(R.drawable.tomato, Color(0xFFFCA5A5)),
    AvatarItem(R.drawable.droid, Color(0xFFA7F3D0)),
    AvatarItem(R.drawable.ic_grapes, Color(0xFFC4B5FD)),
    AvatarItem(R.drawable.ping, Color(0xFFFDE68A)),
    AvatarItem(R.drawable.thumb, Color(0xFF93C5FD)),
)

@Composable
fun OverlappingImagesScreen(modifier: Modifier = Modifier) {
    var overlap by remember { mutableFloatStateOf(0.55f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Header()

        AvatarStack(
            overlapFactor = overlap,
            modifier = Modifier.fillMaxWidth(),
        )

        ExtendedAvatarStack(
            overlapFactor = overlap,
            modifier = Modifier.fillMaxWidth(),
        )

        ContextRow(overlap)

        Slider(
            value = overlap,
            onValueChange = { overlap = it },
            valueRange = 0.15f..1f,
        )
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Overlapping images",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Custom Layout that horizontally stacks children with a configurable overlap. " +
                "Drag the slider to change the overlap factor.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AvatarStack(overlapFactor: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OverlappingRow(overlapFactor = overlapFactor) {
            AVATARS.forEach { item ->
                Avatar(drawable = item.drawable, ring = item.tint)
            }
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = "+12",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExtendedAvatarStack(overlapFactor: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Today's reviewers",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OverlappingRow(overlapFactor = overlapFactor) {
            (AVATARS + AVATARS.take(2)).forEach { item ->
                Avatar(drawable = item.drawable, ring = item.tint, size = 44)
            }
        }
        Text(
            text = "Liked by Tomato, Droid and 5 others",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContextRow(overlap: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Overlap factor",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${"%.0f".format(overlap * 100)}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Avatar(drawable: Int, ring: Color, size: Int = 56) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 3.dp, color = ring, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = Modifier
                .size((size - 6).dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    }
}
