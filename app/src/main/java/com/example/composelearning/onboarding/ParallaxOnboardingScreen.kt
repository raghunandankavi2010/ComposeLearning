package com.example.composelearning.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * A parallax onboarding flow. Each [OnboardingPage] is composed of several
 * depth layers that translate at different rates as the user swipes — far
 * layers drift slowly, the foreground icon leads — which reads as depth. The
 * whole screen background and the page indicator both interpolate their color
 * between the adjacent pages' accents, driven by the pager's fractional offset.
 */
private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val background: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Discover",
        subtitle = "Explore a universe of hand-crafted Compose animations, all in one place.",
        icon = Icons.Filled.Explore,
        accent = Color(0xFF6C5CE7),
        background = Color(0xFF1B1340)
    ),
    OnboardingPage(
        title = "Feel the depth",
        subtitle = "Layers drift at their own pace as you swipe, giving every screen a sense of space.",
        icon = Icons.Filled.Bolt,
        accent = Color(0xFF00B8A9),
        background = Color(0xFF06292B)
    ),
    OnboardingPage(
        title = "Made with love",
        subtitle = "Springy, fluid motion that responds to every gesture you make.",
        icon = Icons.Filled.Favorite,
        accent = Color(0xFFFF6B81),
        background = Color(0xFF3A1220)
    ),
    OnboardingPage(
        title = "Ready to launch",
        subtitle = "You're all set — dive in and start exploring the demos.",
        icon = Icons.Filled.Rocket,
        accent = Color(0xFFFFA502),
        background = Color(0xFF3A2705)
    )
)

@Composable
fun ParallaxOnboardingScreen(onFinish: () -> Unit = {}) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    // Continuous position across pages, e.g. 1.6 when 60% between page 1 and 2.
    val position by remember {
        derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    }

    // Interpolate the screen background between the two pages we're between.
    val backgroundColor = remember {
        derivedStateOf {
            val lower = position.toInt().coerceIn(0, onboardingPages.lastIndex)
            val upper = (lower + 1).coerceAtMost(onboardingPages.lastIndex)
            lerp(
                onboardingPages[lower].background,
                onboardingPages[upper].background,
                position - lower
            )
        }
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Signed distance of this page from the settled center: 0 when
            // centered, +/-1 when one page away. Drives every parallax layer.
            val pageOffset = position - page
            OnboardingPageContent(
                page = onboardingPages[page],
                pageOffset = pageOffset
            )
        }

        // Bottom controls: indicator + action button, pinned over the pager.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(
                pageCount = onboardingPages.size,
                position = position
            )

            Spacer(Modifier.height(28.dp))

            val isLast by remember { derivedStateOf { pagerState.currentPage == onboardingPages.lastIndex } }
            val buttonColor by animateColorAsState(
                targetValue = interpolatedAccent(position),
                label = "buttonColor"
            )

            Button(
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isLast) "Get started" else "Next",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float
) {
    // Fade the page out as it leaves the center.
    val contentAlpha = (1f - pageOffset.absoluteValue).coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Layer 1 (deepest): a large soft blob, barely moves → reads as far.
        DepthBlob(
            color = page.accent.copy(alpha = 0.18f),
            sizeDp = 460,
            alignment = Alignment.TopEnd,
            depth = 0.10f,
            pageOffset = pageOffset
        )
        // ── Layer 2: mid-ground blob, moves a touch more than layer 1.
        DepthBlob(
            color = page.accent.copy(alpha = 0.22f),
            sizeDp = 320,
            alignment = Alignment.BottomStart,
            depth = 0.28f,
            pageOffset = pageOffset
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp)
                .padding(bottom = 180.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Layer 3: the hero icon — the foreground, leads the swipe most.
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .parallax(depth = 0.65f, pageOffset = pageOffset)
                    .alpha(contentAlpha)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(page.accent, page.accent.copy(alpha = 0.55f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── Layer 4: title — foreground text, slightly less lead than icon.
            Text(
                text = page.title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .parallax(depth = 0.45f, pageOffset = pageOffset)
                    .alpha(contentAlpha)
            )

            Spacer(Modifier.height(16.dp))

            // ── Layer 5: subtitle — moves least of the text, sits behind title.
            Text(
                text = page.subtitle,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .parallax(depth = 0.30f, pageOffset = pageOffset)
                    .alpha(contentAlpha)
            )
        }
    }
}

/** A blurred-looking radial blob used as a far/mid parallax background layer. */
@Composable
private fun DepthBlob(
    color: Color,
    sizeDp: Int,
    alignment: Alignment,
    depth: Float,
    pageOffset: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(alignment)
                .parallax(depth = depth, pageOffset = pageOffset)
                .size(sizeDp.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Translates a layer horizontally by a fraction of its width proportional to
 * how far its page is from center. Larger [depth] = moves more = feels closer.
 */
private fun Modifier.parallax(depth: Float, pageOffset: Float): Modifier =
    graphicsLayer {
        translationX = -pageOffset * size.width * depth
    }

@Composable
private fun PageIndicator(
    pageCount: Int,
    position: Float
) {
    val accent = interpolatedAccent(position)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            // How "selected" this dot is: 1 when centered on it, fading to 0.
            val selectedness = (1f - (position - index).absoluteValue).coerceIn(0f, 1f)
            val width by animateDpAsState(
                targetValue = lerpDp(8.dp, 28.dp, selectedness),
                animationSpec = spring(),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        lerp(
                            Color.White.copy(alpha = 0.30f),
                            accent,
                            selectedness
                        )
                    )
            )
        }
    }
}

/** Accent color interpolated between the two pages the pager sits between. */
private fun interpolatedAccent(position: Float): Color {
    val lower = position.toInt().coerceIn(0, onboardingPages.lastIndex)
    val upper = (lower + 1).coerceAtMost(onboardingPages.lastIndex)
    return lerp(onboardingPages[lower].accent, onboardingPages[upper].accent, position - lower)
}
