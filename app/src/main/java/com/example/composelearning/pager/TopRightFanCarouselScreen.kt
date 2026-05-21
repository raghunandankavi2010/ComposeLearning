package com.example.composelearning.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DemoCards = listOf(
    CarouselCard(
        "Tokyo",
        "Neon-lit alleys, ramen counters, and quiet shrines tucked between skyscrapers.",
        "Asia",
        Brush.linearGradient(listOf(Color(0xFFEE0979), Color(0xFFFF6A00))),
    ),
    CarouselCard(
        "Reykjavík",
        "Northern-lights season — glaciers, geysers and aurora chasing.",
        "Europe",
        Brush.linearGradient(listOf(Color(0xFF134E5E), Color(0xFF71B280))),
    ),
    CarouselCard(
        "Bali",
        "Surf in the morning, temples by afternoon, sunsets you can taste.",
        "Indonesia",
        Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFF09819))),
    ),
    CarouselCard(
        "Paris",
        "Croissants, cobblestones, and a river that knows everyone's secrets.",
        "Europe",
        Brush.linearGradient(listOf(Color(0xFF614385), Color(0xFF516395))),
    ),
    CarouselCard(
        "Cape Town",
        "Where mountains meet the sea, with wine country an hour away.",
        "Africa",
        Brush.linearGradient(listOf(Color(0xFF2980B9), Color(0xFF6DD5FA))),
    ),
    CarouselCard(
        "Patagonia",
        "Wild peaks at the southern edge of the world — go in their summer.",
        "South America",
        Brush.linearGradient(listOf(Color(0xFF0F2027), Color(0xFF2C5364))),
    ),
)

@Composable
fun TopRightFanCarouselScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7F8FB), Color(0xFFE7ECF3)),
                ),
            )
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Destinations",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2530),
            )
            Text(
                text = "Drag the front card in any direction to dismiss.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6C7480),
            )
        }

        val cards = remember { DemoCards }
        TopRightFanCarousel(
            cards = cards,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
        )

        // Bottom hint
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF1F2530).copy(alpha = 0.08f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Swipe to explore  ›  ${cards.size} places",
                    color = Color(0xFF1F2530),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}