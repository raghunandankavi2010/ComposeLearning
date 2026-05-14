package com.example.composelearning.tutorial.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composelearning.tutorial.domain.model.FeatureCard
import com.example.composelearning.tutorial.ui.overlay.tutorialTarget

@Composable
fun FeatureCardItem(
    card: FeatureCard,
    modifier: Modifier = Modifier,
) {
    when (card.variant) {
        FeatureCard.Variant.Hero -> HeroCard(card, modifier)
        FeatureCard.Variant.Stat -> StatCard(card, modifier)
        FeatureCard.Variant.Action -> ActionCard(card, modifier)
        FeatureCard.Variant.Profile -> ProfileCard(card, modifier)
        FeatureCard.Variant.TextOnly -> TextOnlyCard(card, modifier)
        FeatureCard.Variant.Chart -> ChartCard(card, modifier)
    }
}

@Composable
private fun HeroCard(card: FeatureCard, modifier: Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(primary, secondary),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    )
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                )
            }
        }
    }
}

@Composable
private fun StatCard(card: FeatureCard, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ActionCard(card: FeatureCard, modifier: Modifier) {
    val isShare = card.id == "card_share"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = {},
                    modifier = if (isShare) Modifier else Modifier
                        .tutorialTarget("card_quick_action_button"),
                ) {
                    Icon(
                        imageVector = if (isShare) Icons.Default.Share else Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isShare) "Invite" else "Run")
                }
                OutlinedButton(onClick = {}) {
                    Text(if (isShare) "Copy link" else "Schedule")
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(card: FeatureCard, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun TextOnlyCard(card: FeatureCard, modifier: Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChartCard(card: FeatureCard, modifier: Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    val barColorMuted = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .tutorialTarget(card.id),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            FakeBarChart(
                values = listOf(0.4f, 0.7f, 0.5f, 0.85f, 0.6f, 0.95f, 0.7f),
                barColor = barColor,
                mutedColor = barColorMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            )
        }
    }
}

@Composable
private fun FakeBarChart(
    values: List<Float>,
    barColor: Color,
    mutedColor: Color,
    modifier: Modifier,
) {
    Canvas(modifier = modifier) {
        val gap = 8.dp.toPx()
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { index, ratio ->
            val barHeight = size.height * ratio
            val left = index * (barWidth + gap)
            val top = size.height - barHeight
            drawRoundRect(
                color = if (index == values.lastIndex - 1) barColor else mutedColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            )
        }
    }
}