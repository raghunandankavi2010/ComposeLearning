package com.example.composelearning.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.composelearning.R
import kotlin.math.abs

private data class CarouselItem(
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val gradient: List<Color>,
)

private val carouselItems = listOf(
    CarouselItem(
        title = "Crisp Tomato",
        subtitle = "Fresh from the garden",
        drawableRes = R.drawable.tomato,
        gradient = listOf(Color(0xFFFF6B6B), Color(0xFFB3261E)),
    ),
    CarouselItem(
        title = "Sweet Grapes",
        subtitle = "Bunches of summer",
        drawableRes = R.drawable.ic_grapes,
        gradient = listOf(Color(0xFF7B2CBF), Color(0xFF3C096C)),
    ),
    CarouselItem(
        title = "Mr. Android",
        subtitle = "Hello, Droid",
        drawableRes = R.drawable.droid,
        gradient = listOf(Color(0xFF3DDC84), Color(0xFF0F9D58)),
    ),
    CarouselItem(
        title = "Thumbs Up",
        subtitle = "Approved aesthetic",
        drawableRes = R.drawable.thumb,
        gradient = listOf(Color(0xFF4FC3F7), Color(0xFF0277BD)),
    ),
    CarouselItem(
        title = "Background Bliss",
        subtitle = "Soft & subtle",
        drawableRes = R.drawable.bkg,
        gradient = listOf(Color(0xFFFFB74D), Color(0xFFE65100)),
    ),
    CarouselItem(
        title = "Ping",
        subtitle = "A small wonder",
        drawableRes = R.drawable.ping,
        gradient = listOf(Color(0xFFEC407A), Color(0xFFAD1457)),
    ),
    CarouselItem(
        title = "Test Image",
        subtitle = "Pixel perfect",
        drawableRes = R.drawable.test,
        gradient = listOf(Color(0xFF26C6DA), Color(0xFF00838F)),
    ),
)

@Composable
fun ThreeDCarousel(
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = carouselItems.size / 2,
        pageCount = { carouselItems.size },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 72.dp),
                pageSpacing = (-24).dp,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val pageOffset = remember(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                }
                val absOffset = abs(pageOffset).coerceIn(0f, 1f)

                CarouselCard(
                    item = carouselItems[page],
                    modifier = Modifier
                        .aspectRatio(0.72f)
                        .graphicsLayer {
                            cameraDistance = 12f * density

                            val scale = lerp(0.78f, 1f, 1f - absOffset)
                            scaleX = scale
                            scaleY = scale

                            rotationY = pageOffset * -45f

                            translationX = pageOffset * size.width * 0.08f

                            alpha = lerp(0.55f, 1f, 1f - absOffset)
                        },
                    elevationFactor = 1f - absOffset,
                )
            }

            PageIndicator(
                pageCount = carouselItems.size,
                currentPage = pagerState.currentPage,
                offsetFraction = pagerState.currentPageOffsetFraction,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CarouselCard(
    item: CarouselItem,
    modifier: Modifier = Modifier,
    elevationFactor: Float = 1f,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = (24f * elevationFactor).dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = item.gradient.last(),
                spotColor = item.gradient.last(),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(item.gradient)),
    ) {
        Image(
            painter = painterResource(id = item.drawableRes),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.85f },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = item.subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    offsetFraction: Float,
) {
    val activeColor = Color.White
    val inactiveColor = Color.White.copy(alpha = 0.35f)

    androidx.compose.foundation.layout.Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val distance = abs((currentPage + offsetFraction) - index).coerceIn(0f, 1f)
            val size = lerp(6f, 10f, 1f - distance)
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(
                        if (distance < 0.5f) activeColor else inactiveColor,
                    ),
            )
        }
    }
}