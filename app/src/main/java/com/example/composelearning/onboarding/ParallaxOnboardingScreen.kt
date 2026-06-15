package com.example.composelearning.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.lerp as lerpDp
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * A parallax onboarding "sky diorama" that walks through a day: dawn → noon →
 * sunset → night. Every element lives on its own depth layer with a different
 * parallax [speed], *and* animates on its own: the sun radiates and its rays
 * rotate, clouds drift, birds flap and fly across, stars twinkle, and a moon
 * rises at night. Swiping scrolls each layer by `position × screenWidth × speed`
 * (far layers barely move, near layers rush past); the sky color, button and
 * page indicator all interpolate between the adjacent pages.
 */
private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val skyTop: Color,
    val skyBottom: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Daybreak",
        subtitle = "The sun climbs over distant hills as the world slowly wakes.",
        icon = Icons.Filled.WbTwilight,
        accent = Color(0xFFF2A65A),
        skyTop = Color(0xFF243A6B),
        skyBottom = Color(0xFFF0A368)
    ),
    OnboardingPage(
        title = "Clear Skies",
        subtitle = "Birds drift past on a bright, wide-open afternoon.",
        icon = Icons.Filled.WbSunny,
        accent = Color(0xFF38A3D1),
        skyTop = Color(0xFF2E6FB0),
        skyBottom = Color(0xFFBFE0F5)
    ),
    OnboardingPage(
        title = "Golden Hour",
        subtitle = "Warm light spills across the horizon as the sun dips low.",
        icon = Icons.Filled.WbCloudy,
        accent = Color(0xFFE5573F),
        skyTop = Color(0xFF3B2C5E),
        skyBottom = Color(0xFFF07A4B)
    ),
    OnboardingPage(
        title = "Starry Night",
        subtitle = "The moon rises and the stars come out — time to explore.",
        icon = Icons.Filled.DarkMode,
        accent = Color(0xFF7E8BE0),
        skyTop = Color(0xFF070B1E),
        skyBottom = Color(0xFF202C54)
    )
)

// Parallax speeds per layer (fraction of a screen width scrolled per page).
// Smaller = farther away = drifts slower.
private const val STAR_SPEED = 0.05f
private const val SUN_SPEED = 0.08f
private const val BACK_HILL_SPEED = 0.15f
private const val CLOUD_SPEED = 0.30f
private const val FRONT_HILL_SPEED = 0.45f
private const val BIRD_SPEED = 0.60f

private class Star(val xf: Float, val yf: Float, val r: Float, val phase: Float)
private class Cloud(val xf: Float, val yf: Float, val scale: Float)
private class Bird(val xf: Float, val yf: Float, val scale: Float, val phase: Float)

@Composable
fun ParallaxOnboardingScreen(onFinish: () -> Unit = {}) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    val position by remember {
        derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    }
    // Lambda form for draw-time (per-frame) reads inside Canvas / graphicsLayer.
    val positionProvider = remember(pagerState) {
        { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    }

    val accent = colorBetweenPages(position) { it.accent }
    val skyTop = colorBetweenPages(position) { it.skyTop }
    val skyBottom = colorBetweenPages(position) { it.skyBottom }

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherScene(
            position = positionProvider,
            skyTop = skyTop,
            skyBottom = skyBottom
        )

        // Foreground: the page text/icon, riding the pager (the closest layer).
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ForegroundContent(
                page = onboardingPages[page],
                pageOffset = { (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction }
            )
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

/**
 * The whole animated sky on a single [Canvas]. A [rememberInfiniteTransition]
 * drives the continuous motion (drift / flap / ray spin / twinkle) and forces a
 * redraw every frame; `position()` is read inside the draw block so parallax
 * tracks the swipe per-frame too. Each element is offset by its layer's
 * parallax shift `-position × width × speed`.
 */
@Composable
private fun WeatherScene(
    position: () -> Float,
    skyTop: Color,
    skyBottom: Color
) {
    val stars = remember {
        val rnd = Random(42)
        List(46) { Star(rnd.nextFloat() * 1.2f, rnd.nextFloat() * 0.6f, rnd.nextFloat() * 3f + 1.5f, rnd.nextFloat()) }
    }
    val clouds = remember {
        val rnd = Random(7)
        List(7) { Cloud(rnd.nextFloat() * 2.4f, 0.10f + rnd.nextFloat() * 0.35f, 0.7f + rnd.nextFloat() * 0.8f) }
    }
    val birds = remember {
        val rnd = Random(99)
        List(6) { Bird(rnd.nextFloat(), 0.15f + rnd.nextFloat() * 0.28f, 0.5f + rnd.nextFloat() * 0.6f, rnd.nextFloat()) }
    }

    val transition = rememberInfiniteTransition(label = "sky")
    val drift by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(60000, easing = LinearEasing)), label = "drift"
    )
    val fly by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "fly"
    )
    val flap by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(450, easing = LinearEasing)), label = "flap"
    )
    val rayPhase by transition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(40000, easing = LinearEasing)), label = "rays"
    )
    val twinkle by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse), label = "twinkle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val pos = position()
        val w = size.width
        val h = size.height
        // 0 during the day, ramping to 1 across the last page (page 2 → 3).
        val night = (pos - 2f).coerceIn(0f, 1f)

        // ── Sky gradient (drawn per-frame so it tracks the swipe exactly).
        drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))

        // ── Stars (night only), far + twinkling.
        if (night > 0.01f) {
            val starShift = -pos * w * STAR_SPEED
            stars.forEach { s ->
                val a = night * (0.4f + 0.6f * sinWave(twinkle + s.phase))
                drawCircle(
                    color = Color.White.copy(alpha = a.coerceIn(0f, 1f)),
                    radius = s.r,
                    center = Offset(wrap(s.xf * w + starShift, w), s.yf * h)
                )
            }
        }

        // ── Sun (day) / Moon (night) cross-fade, just below the far layer.
        val celestial = Offset(0.72f * w - pos * w * SUN_SPEED, 0.22f * h)
        val radius = 0.12f * w
        if (night < 0.99f) drawSun(celestial, radius, rayPhase, alpha = 1f - night)
        if (night > 0.01f) drawMoon(celestial, radius, alpha = night)

        // ── Distant hills.
        drawHill(pos, BACK_HILL_SPEED, baseYf = 0.66f, amp = 0.16f,
            color = lerp(Color(0xFF5C7CA8), Color(0xFF161E36), night), phaseShift = 0.6f)

        // ── Clouds (fade out at night), drifting left.
        val cloudAlpha = (1f - 0.85f * night)
        if (cloudAlpha > 0.02f) {
            clouds.forEach { c ->
                val x = wrap(c.xf * w - drift * 1.4f * w - pos * w * CLOUD_SPEED, 2.4f * w) - 0.2f * w
                drawCloud(Offset(x, c.yf * h), c.scale * 0.16f * w, cloudAlpha)
            }
        }

        // ── Foreground hill.
        drawHill(pos, FRONT_HILL_SPEED, baseYf = 0.80f, amp = 0.12f,
            color = lerp(Color(0xFF2F4A39), Color(0xFF0B1018), night), phaseShift = 2.1f)

        // ── Birds (day), flapping + flying across, the closest scene layer.
        val birdAlpha = (1f - night)
        if (birdAlpha > 0.02f) {
            birds.forEach { b ->
                val x = wrap(b.xf * w + fly * 1.6f * w - pos * w * BIRD_SPEED, 1.6f * w) - 0.3f * w
                drawBird(Offset(x, b.yf * h), b.scale * 0.07f * w, flap, b.phase, birdAlpha)
            }
        }
    }
}

private fun sinWave(t: Float) = (sin(t * 2f * Math.PI.toFloat()) + 1f) / 2f

/** Wraps [x] into [0, span) so scrolling content reappears on the other side. */
private fun wrap(x: Float, span: Float): Float {
    var v = x % span
    if (v < 0) v += span
    return v
}

private fun DrawScope.drawSun(center: Offset, radius: Float, rayDeg: Float, alpha: Float) {
    val sun = Color(0xFFFFE08A)
    // Soft glow.
    drawCircle(
        brush = Brush.radialGradient(
            listOf(sun.copy(alpha = 0.45f * alpha), Color.Transparent),
            center = center, radius = radius * 3.4f
        ),
        radius = radius * 3.4f, center = center
    )
    // Rotating rays.
    val rays = 12
    repeat(rays) { i ->
        val a = Math.toRadians((i * (360.0 / rays) + rayDeg))
        val c = cos(a).toFloat(); val s = sin(a).toFloat()
        drawLine(
            color = sun.copy(alpha = 0.55f * alpha),
            start = Offset(center.x + c * radius * 1.35f, center.y + s * radius * 1.35f),
            end = Offset(center.x + c * radius * 1.95f, center.y + s * radius * 1.95f),
            strokeWidth = radius * 0.10f, cap = StrokeCap.Round
        )
    }
    drawCircle(color = sun.copy(alpha = alpha), radius = radius, center = center)
}

private fun DrawScope.drawMoon(center: Offset, radius: Float, alpha: Float) {
    val moon = Color(0xFFE9EAF6)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(moon.copy(alpha = 0.35f * alpha), Color.Transparent),
            center = center, radius = radius * 3f
        ),
        radius = radius * 3f, center = center
    )
    drawCircle(color = moon.copy(alpha = alpha), radius = radius, center = center)
    // A few craters.
    val crater = Color(0xFFC4C6DA).copy(alpha = alpha)
    drawCircle(crater, radius * 0.20f, center + Offset(-radius * 0.3f, -radius * 0.25f))
    drawCircle(crater, radius * 0.13f, center + Offset(radius * 0.35f, radius * 0.1f))
    drawCircle(crater, radius * 0.10f, center + Offset(radius * 0.05f, radius * 0.45f))
}

private fun DrawScope.drawCloud(center: Offset, r: Float, alpha: Float) {
    val c = Color.White.copy(alpha = 0.85f * alpha)
    drawCircle(c, r * 0.7f, center + Offset(-r * 1.1f, r * 0.2f))
    drawCircle(c, r * 0.95f, center + Offset(-r * 0.4f, 0f))
    drawCircle(c, r, center + Offset(r * 0.4f, -r * 0.1f))
    drawCircle(c, r * 0.75f, center + Offset(r * 1.2f, r * 0.2f))
    drawCircle(c, r * 0.6f, center + Offset(r * 0.3f, r * 0.4f))
}

private fun DrawScope.drawBird(center: Offset, s: Float, flap: Float, phase: Float, alpha: Float) {
    // Wing tips rise/fall with the flap cycle.
    val lift = s * (0.4f + 0.9f * sinWave(flap + phase))
    val path = Path().apply {
        moveTo(center.x - s, center.y)
        quadraticBezierTo(center.x - s * 0.4f, center.y - lift, center.x, center.y)
        quadraticBezierTo(center.x + s * 0.4f, center.y - lift, center.x + s, center.y)
    }
    drawPath(
        path = path,
        color = Color(0xFF2B3147).copy(alpha = 0.8f * alpha),
        style = Stroke(width = s * 0.18f, cap = StrokeCap.Round)
    )
}

/** A scrolling hill silhouette built from a row of rounded bumps. */
private fun DrawScope.drawHill(
    pos: Float,
    speed: Float,
    baseYf: Float,
    amp: Float,
    color: Color,
    phaseShift: Float
) {
    val w = size.width
    val h = size.height
    val shift = -pos * w * speed
    val baseY = h * baseYf
    val startX = -0.6f * w + shift
    val endX = 2.4f * w + shift
    val segments = 7
    val step = (endX - startX) / segments
    val path = Path().apply {
        moveTo(startX, baseY)
        for (i in 0 until segments) {
            val cx = startX + step * (i + 0.5f)
            val nx = startX + step * (i + 1)
            val peak = baseY - amp * h * (0.5f + 0.5f * sin(i * 1.7f + phaseShift))
            quadraticBezierTo(cx, peak, nx, baseY)
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
                .parallaxLayer(depth = 0.18f, fade = true, pageOffset = pageOffset)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(page.accent, page.accent.copy(alpha = 0.5f)))
                ),
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
            modifier = Modifier.parallaxLayer(depth = 0.08f, fade = true, pageOffset = pageOffset)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.parallaxLayer(depth = 0.04f, fade = true, pageOffset = pageOffset)
        )
    }
}

/**
 * Small extra horizontal shift for foreground elements, on top of the pager's
 * own slide. [pageOffset] is read inside graphicsLayer (draw time) so it tracks
 * the swipe every frame; [fade] dissolves the element as its page leaves.
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
