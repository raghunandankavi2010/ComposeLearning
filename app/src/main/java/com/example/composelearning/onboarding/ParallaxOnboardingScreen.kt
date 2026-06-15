package com.example.composelearning.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.unit.lerp as lerpDp

/**
 * Parallax onboarding where **each page is its own self-contained sky scene**
 * and **all motion comes from the swipe** — nothing animates on its own. Every
 * page draws its own layers (stars, sun/moon, hills, clouds, birds); each layer
 * is shifted by `-pageOffset * width * speed`, so as you drag, the layers slide
 * at different paces (near layers fast, far layers slow) and that difference
 * reads as depth. At rest (`pageOffset == 0`) the whole scene is perfectly
 * still. Each page is clipped to its bounds, so objects stay within their page.
 */
private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val skyTop: Color,
    val skyBottom: Color,
    val moon: Boolean,
    val stars: Boolean,
    val birds: Boolean,
    val clouds: Boolean,
    val backHill: Color,
    val frontHill: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Daybreak",
        subtitle = "The sun climbs over distant hills as the world slowly wakes.",
        icon = Icons.Filled.WbTwilight,
        accent = Color(0xFFF2A65A),
        skyTop = Color(0xFF243A6B),
        skyBottom = Color(0xFFF0A368),
        moon = false, stars = false, birds = true, clouds = true,
        backHill = Color(0xFF6B6E9E), frontHill = Color(0xFF3B3A57)
    ),
    OnboardingPage(
        title = "Clear Skies",
        subtitle = "Birds drift past on a bright, wide-open afternoon.",
        icon = Icons.Filled.WbSunny,
        accent = Color(0xFF38A3D1),
        skyTop = Color(0xFF2E6FB0),
        skyBottom = Color(0xFFBFE0F5),
        moon = false, stars = false, birds = true, clouds = true,
        backHill = Color(0xFF6FA0C4), frontHill = Color(0xFF2F4A39)
    ),
    OnboardingPage(
        title = "Golden Hour",
        subtitle = "Warm light spills across the horizon as the sun dips low.",
        icon = Icons.Filled.WbCloudy,
        accent = Color(0xFFE5573F),
        skyTop = Color(0xFF3B2C5E),
        skyBottom = Color(0xFFF07A4B),
        moon = false, stars = false, birds = false, clouds = true,
        backHill = Color(0xFF7A4A66), frontHill = Color(0xFF2C1E34)
    ),
    OnboardingPage(
        title = "Starry Night",
        subtitle = "The moon rises and the stars come out — time to explore.",
        icon = Icons.Filled.DarkMode,
        accent = Color(0xFF7E8BE0),
        skyTop = Color(0xFF070B1E),
        skyBottom = Color(0xFF202C54),
        moon = true, stars = true, birds = false, clouds = false,
        backHill = Color(0xFF161E36), frontHill = Color(0xFF0B1018)
    )
)

// Parallax speeds per layer: fraction of the page width a layer shifts when the
// page travels one full page from center. Smaller = farther = drifts slower.
private const val STAR_SPEED = 0.06f
private const val SUN_SPEED = 0.10f
private const val BACK_HILL_SPEED = 0.22f
private const val CLOUD_SPEED = 0.40f
private const val FRONT_HILL_SPEED = 0.55f
private const val BIRD_SPEED = 0.78f

private class Star(val xf: Float, val yf: Float, val r: Float, val brightness: Float)
private class Cloud(val xf: Float, val yf: Float, val scale: Float)
private class Bird(val xf: Float, val yf: Float, val scale: Float, val lift: Float)

@Composable
fun ParallaxOnboardingScreen(onFinish: () -> Unit = {}) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    val position by remember {
        derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    }

    val accent = colorBetweenPages(position) { it.accent }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // This page's own distance from center: 0 centered, ±1 a page away.
            val pageOffset = { (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds() // keep this page's scene inside this page
            ) {
                WeatherScene(page = onboardingPages[page], seed = page, pageOffset = pageOffset)
                ForegroundContent(page = onboardingPages[page], pageOffset = pageOffset)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageIndicator(pageCount = onboardingPages.size, position = position, accent = accent)

            Spacer(Modifier.height(28.dp))

            val isLast by remember { derivedStateOf { pagerState.currentPage == onboardingPages.lastIndex } }
            val buttonColor by animateColorAsState(targetValue = accent, label = "buttonColor")

            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White)
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

/**
 * One page's sky on a single [Canvas]. Everything is static; the *only* motion
 * is the parallax shift `-pageOffset() * width * speed`, read at draw time so it
 * tracks the swipe per-frame. Each layer uses a different speed, so during a
 * swipe the layers visibly slide apart, then re-settle when the page centers.
 */
@Composable
private fun WeatherScene(
    page: OnboardingPage,
    seed: Int,
    pageOffset: () -> Float
) {
    val stars = remember(seed) {
        val rnd = Random(seed * 31 + 1)
        List(46) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.6f, rnd.nextFloat() * 3f + 1.5f, 0.4f + rnd.nextFloat() * 0.6f) }
    }
    val clouds = remember(seed) {
        val rnd = Random(seed * 17 + 5)
        List(4) { Cloud(0.1f + rnd.nextFloat() * 0.8f, 0.08f + rnd.nextFloat() * 0.30f, 0.7f + rnd.nextFloat() * 0.7f) }
    }
    val birds = remember(seed) {
        val rnd = Random(seed * 53 + 9)
        List(5) { Bird(0.1f + rnd.nextFloat() * 0.8f, 0.14f + rnd.nextFloat() * 0.26f, 0.5f + rnd.nextFloat() * 0.6f, 0.5f + rnd.nextFloat() * 0.5f) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val off = pageOffset()
        val w = size.width
        val h = size.height

        // The one source of motion: each layer's parallax shift for this page.
        fun shift(speed: Float) = -off * w * speed

        drawRect(Brush.verticalGradient(listOf(page.skyTop, page.skyBottom)))

        if (page.stars) {
            val sShift = shift(STAR_SPEED)
            stars.forEach { s ->
                drawCircle(
                    color = Color.White.copy(alpha = s.brightness),
                    radius = s.r,
                    center = Offset(s.xf * w + sShift, s.yf * h)
                )
            }
        }

        val celestial = Offset(0.72f * w + shift(SUN_SPEED), 0.22f * h)
        val radius = 0.12f * w
        if (page.moon) drawMoon(celestial, radius) else drawSun(celestial, radius)

        drawHill(off, BACK_HILL_SPEED, baseYf = 0.66f, amp = 0.16f, color = page.backHill, phaseShift = 0.6f + seed)

        if (page.clouds) {
            clouds.forEach { c ->
                drawCloud(Offset(c.xf * w + shift(CLOUD_SPEED), c.yf * h), c.scale * 0.16f * w)
            }
        }

        drawHill(off, FRONT_HILL_SPEED, baseYf = 0.80f, amp = 0.12f, color = page.frontHill, phaseShift = 2.1f + seed)

        if (page.birds) {
            birds.forEach { b ->
                drawBird(Offset(b.xf * w + shift(BIRD_SPEED), b.yf * h), b.scale * 0.07f * w, b.lift)
            }
        }
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float) {
    val sun = Color(0xFFFFE08A)
    drawCircle(
        brush = Brush.radialGradient(listOf(sun.copy(alpha = 0.45f), Color.Transparent), center = center, radius = radius * 3.4f),
        radius = radius * 3.4f, center = center
    )
    val rays = 12
    repeat(rays) { i ->
        val a = Math.toRadians(i * (360.0 / rays))
        val c = cos(a).toFloat()
        val s = sin(a).toFloat()
        drawLine(
            color = sun.copy(alpha = 0.55f),
            start = Offset(center.x + c * radius * 1.35f, center.y + s * radius * 1.35f),
            end = Offset(center.x + c * radius * 1.95f, center.y + s * radius * 1.95f),
            strokeWidth = radius * 0.10f, cap = StrokeCap.Round
        )
    }
    drawCircle(color = sun, radius = radius, center = center)
}

private fun DrawScope.drawMoon(center: Offset, radius: Float) {
    val moon = Color(0xFFE9EAF6)
    drawCircle(
        brush = Brush.radialGradient(listOf(moon.copy(alpha = 0.35f), Color.Transparent), center = center, radius = radius * 3f),
        radius = radius * 3f, center = center
    )
    drawCircle(color = moon, radius = radius, center = center)
    val crater = Color(0xFFC4C6DA)
    drawCircle(crater, radius * 0.20f, center + Offset(-radius * 0.3f, -radius * 0.25f))
    drawCircle(crater, radius * 0.13f, center + Offset(radius * 0.35f, radius * 0.1f))
    drawCircle(crater, radius * 0.10f, center + Offset(radius * 0.05f, radius * 0.45f))
}

private fun DrawScope.drawCloud(center: Offset, r: Float) {
    val c = Color.White.copy(alpha = 0.85f)
    drawCircle(c, r * 0.7f, center + Offset(-r * 1.1f, r * 0.2f))
    drawCircle(c, r * 0.95f, center + Offset(-r * 0.4f, 0f))
    drawCircle(c, r, center + Offset(r * 0.4f, -r * 0.1f))
    drawCircle(c, r * 0.75f, center + Offset(r * 1.2f, r * 0.2f))
    drawCircle(c, r * 0.6f, center + Offset(r * 0.3f, r * 0.4f))
}

private fun DrawScope.drawBird(center: Offset, s: Float, lift: Float) {
    val wing = s * lift
    val path = Path().apply {
        moveTo(center.x - s, center.y)
        quadraticTo(center.x - s * 0.4f, center.y - wing, center.x, center.y)
        quadraticTo(center.x + s * 0.4f, center.y - wing, center.x + s, center.y)
    }
    drawPath(path = path, color = Color(0xFF2B3147).copy(alpha = 0.8f), style = Stroke(width = s * 0.18f, cap = StrokeCap.Round))
}

/** A hill silhouette built from rounded bumps; slides only with the swipe (per-page parallax). */
private fun DrawScope.drawHill(
    pageOffset: Float,
    speed: Float,
    baseYf: Float,
    amp: Float,
    color: Color,
    phaseShift: Float
) {
    val w = size.width
    val h = size.height
    val shift = -pageOffset * w * speed
    val baseY = h * baseYf
    val startX = -0.8f * w + shift
    val endX = 1.8f * w + shift
    val segments = 6
    val step = (endX - startX) / segments
    val path = Path().apply {
        moveTo(startX, baseY)
        for (i in 0 until segments) {
            val cx = startX + step * (i + 0.5f)
            val nx = startX + step * (i + 1)
            val peak = baseY - amp * h * (0.5f + 0.5f * sin(i * 1.7f + phaseShift))
            quadraticTo(cx, peak, nx, baseY)
        }
        lineTo(endX, h)
        lineTo(startX, h)
        close()
    }
    drawPath(path, color)
}

@Composable
private fun ForegroundContent(
    page: OnboardingPage,
    pageOffset: () -> Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(bottom = 180.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .parallaxLayer(depth = 0.20f, fade = true, pageOffset = pageOffset)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(page.accent, page.accent.copy(alpha = 0.5f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(74.dp)
            )
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = page.title,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.parallaxLayer(depth = 0.10f, fade = true, pageOffset = pageOffset)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.parallaxLayer(depth = 0.05f, fade = true, pageOffset = pageOffset)
        )
    }
}

/**
 * Per-page horizontal shift for foreground elements, on top of the pager's own
 * slide. [pageOffset] is read inside graphicsLayer (draw time) so it tracks the
 * swipe every frame; [fade] dissolves the element as its page leaves.
 */
private fun Modifier.parallaxLayer(
    depth: Float,
    fade: Boolean = false,
    pageOffset: () -> Float
): Modifier =
    graphicsLayer {
        val offset = pageOffset()
        translationX = -offset * size.width * depth
        if (fade) alpha = (1f - offset.absoluteValue).coerceIn(0f, 1f)
    }

@Composable
private fun PageIndicator(
    pageCount: Int,
    position: Float,
    accent: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
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
                    .background(lerp(Color.White.copy(alpha = 0.30f), accent, selectedness))
            )
        }
    }
}

/** Interpolates a per-page color between the two pages [position] sits between. */
private fun colorBetweenPages(position: Float, select: (OnboardingPage) -> Color): Color {
    val lower = position.toInt().coerceIn(0, onboardingPages.lastIndex)
    val upper = (lower + 1).coerceAtMost(onboardingPages.lastIndex)
    return lerp(select(onboardingPages[lower]), select(onboardingPages[upper]), position - lower)
}
