package com.example.composelearning.graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PI_F = PI.toFloat()

/**
 * A physically-inspired 3D coin flip.
 *
 * Two motions run at once, both derived from one flight progress `p` (0..1):
 *  - Vertical toss: `height = peak * 4p(1-p)` — the parabola a constant-gravity
 *    projectile traces, so the coin rises, slows at the apex, and falls back
 *    symmetrically.
 *  - Spin: the coin rotates about a horizontal axis. As the angle sweeps we see
 *    each face turn edge-on (a thin ellipse) then flip to the other side, giving
 *    the 3D read.
 *
 * Both the launch velocity and the spin rate are randomized per toss, and the
 * result is emergent: the coin settles onto whichever face is up when it lands,
 * rather than the outcome being chosen in advance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipToss(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()

    // Whole-flight clock; 1f == landed and at rest.
    val flight = remember { Animatable(1f) }
    // Springy settle applied after landing.
    val bounce = remember { Animatable(0f) }

    // Spin, in radians. restRotation is the resting angle between tosses; a toss
    // interpolates from rotStart -> rotTarget while [flight] runs.
    var restRotation by remember { mutableFloatStateOf(0f) }
    var rotStart by remember { mutableFloatStateOf(0f) }
    var rotTarget by remember { mutableFloatStateOf(0f) }
    var peakFactor by remember { mutableFloatStateOf(0f) }

    var isFlipping by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }

    fun flip() {
        if (isFlipping) return
        scope.launch {
            isFlipping = true
            result = null

            val start = restRotation
            // A real toss: random upward velocity (how high) and a random spin
            // rate (how fast it tumbles). Nothing about the outcome is decided in
            // advance — whichever face is up when it lands is the result.
            peakFactor = 0.8f + Random.nextFloat() * 0.2f
            // Angular travel over the flight: roughly 3..6 full turns, continuous.
            val angularTravel = (6f + Random.nextFloat() * 6f) * PI_F
            val rawTarget = start + angularTravel
            // The coin has to come to rest flat on a face, so it settles to the
            // nearest half-turn. This snap is what decides heads vs tails, exactly
            // like the last fraction of a spin settling a real coin.
            val halfTurns = Math.round(rawTarget / PI_F)
            val landing = halfTurns * PI_F

            rotStart = start
            rotTarget = landing

            flight.snapTo(0f)
            flight.animateTo(1f, animationSpec = tween(durationMillis = 1500, easing = LinearEasing))
            restRotation = landing
            // cos(theta) > 0 (even half-turns) shows heads; odd shows tails.
            result = if (halfTurns % 2 == 0) "HEADS" else "TAILS"

            // Land with a little bounce.
            bounce.snapTo(1f)
            bounce.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow)
            )
            isFlipping = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("3D Coin Flip") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141018),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF201826), Color(0xFF0E0A12))
                    )
                )
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    // Tapping the arena also tosses the coin.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { flip() }
            ) {
                val p = flight.value
                // Ease-out on the spin so it decelerates into the landing.
                val spinEase = 1f - (1f - p) * (1f - p)
                val theta = rotStart + (rotTarget - rotStart) * spinEase

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val groundY = size.height * 0.78f
                    val radius = size.width.coerceAtMost(size.height) * 0.16f

                    val peak = size.height * 0.48f * peakFactor
                    val mainHeight = peak * 4f * p * (1f - p)
                    val bounceHeight = bounce.value * peak * 0.10f
                    val height = mainHeight + bounceHeight
                    // Normalized altitude for shadow scaling.
                    val altitude = (height / (peak + 0.0001f)).coerceIn(0f, 1f)

                    val coinCy = groundY - radius - height

                    drawGroundShadow(cx, groundY, radius, altitude)
                    drawCoin(cx, coinCy, radius, theta, textMeasurer)
                }

                result?.let { r ->
                    Text(
                        text = r,
                        color = if (r == "HEADS") Color(0xFFF6D877) else Color(0xFFCFC3E8),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 24.dp)
                    )
                }
            }

            Button(
                onClick = { flip() },
                enabled = !isFlipping,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9A81F),
                    contentColor = Color(0xFF1A1206)
                ),
                modifier = Modifier.padding(bottom = 32.dp, top = 8.dp)
            ) {
                Text(
                    text = if (isFlipping) "Flipping…" else "Flip Coin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/** Soft contact shadow that shrinks and fades as the coin rises. */
private fun DrawScope.drawGroundShadow(cx: Float, groundY: Float, radius: Float, altitude: Float) {
    val shadowW = radius * (1.6f - altitude * 0.9f)
    val shadowH = shadowW * 0.28f
    val alpha = 0.35f * (1f - altitude * 0.75f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = alpha), Color.Transparent),
            center = Offset(cx, groundY),
            radius = shadowW
        ),
        topLeft = Offset(cx - shadowW, groundY - shadowH),
        size = Size(shadowW * 2f, shadowH * 2f)
    )
}

/**
 * Draws the coin as a thin disc rotated by [theta] about the screen's horizontal
 * axis. The two faces are ellipses of half-height `R*|cos(theta)|`, separated
 * vertically by the projected thickness; the rim silhouette joins them.
 */
private fun DrawScope.drawCoin(
    cx: Float,
    cy: Float,
    radius: Float,
    theta: Float,
    textMeasurer: TextMeasurer
) {
    val cosT = cos(theta)
    val sinT = sin(theta)
    val cosMag = abs(cosT)
    val faceRy = radius * cosMag
    val thickness = radius * 0.16f

    // Face-center vertical offsets from the coin's thickness projecting onto Y.
    val dyHeads = -(thickness / 2f) * sinT
    val dyTails = (thickness / 2f) * sinT

    // The face nearer the camera (larger depth) is the one whose cos sign matches.
    val headsInFront = cosT >= 0f
    val frontDy = if (headsInFront) dyHeads else dyTails

    val topDy = minOf(dyHeads, dyTails)
    val botDy = maxOf(dyHeads, dyTails)
    val topCy = cy + topDy
    val botCy = cy + botDy

    // ---- Rim silhouette: top arc of the upper ellipse + bottom arc of the lower.
    val rim = Path().apply {
        moveTo(cx - radius, topCy)
        arcTo(Rect(cx - radius, topCy - faceRy, cx + radius, topCy + faceRy), 180f, 180f, false)
        lineTo(cx + radius, botCy)
        arcTo(Rect(cx - radius, botCy - faceRy, cx + radius, botCy + faceRy), 0f, 180f, false)
        close()
    }
    drawPath(
        path = rim,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6E4E10),
                Color(0xFFB9891C),
                Color(0xFFEFC24A),
                Color(0xFFB9891C),
                Color(0xFF6E4E10)
            ),
            startX = cx - radius,
            endX = cx + radius
        )
    )

    // Reeded edge: vertical ticks clipped to the rim shape.
    clipPath(rim) {
        val ticks = 48
        for (i in 0..ticks) {
            val x = cx - radius + (2f * radius) * (i.toFloat() / ticks)
            drawLine(
                color = Color(0xFF3C2A08).copy(alpha = 0.35f),
                start = Offset(x, topCy - faceRy),
                end = Offset(x, botCy + faceRy),
                strokeWidth = 1.2f
            )
        }
    }

    // ---- Front face on top of the rim (skip when nearly edge-on).
    if (faceRy < 0.6f) return

    val isHeads = headsInFront
    val faceCy = cy + frontDy
    val faceColors = if (isHeads) {
        listOf(Color(0xFFFCE9A6), Color(0xFFE8BE4E), Color(0xFFB98A1E))
    } else {
        listOf(Color(0xFFEFE6C9), Color(0xFFCDA84A), Color(0xFFA07D24))
    }
    drawOval(
        brush = Brush.radialGradient(
            colors = faceColors,
            center = Offset(cx - radius * 0.3f, faceCy - faceRy * 0.4f),
            radius = radius * 1.4f
        ),
        topLeft = Offset(cx - radius, faceCy - faceRy),
        size = Size(radius * 2f, faceRy * 2f)
    )

    // Everything on the face is squashed vertically by cos(theta) to sit on the tilt.
    withTransform({
        translate(cx, faceCy)
        scale(1f, cosMag, pivot = Offset.Zero)
    }) {
        // Beaded inner ring.
        drawCircle(
            color = Color(0xFF8A6413).copy(alpha = 0.7f),
            radius = radius * 0.82f,
            center = Offset.Zero,
            style = Stroke(width = radius * 0.05f)
        )
        drawCircle(
            color = Color(0xFF8A6413).copy(alpha = 0.4f),
            radius = radius * 0.9f,
            center = Offset.Zero,
            style = Stroke(width = radius * 0.02f)
        )

        val letter = if (isHeads) "H" else "T"
        val emblem = if (isHeads) "★" else "✦"
        val letterLayout = textMeasurer.measure(
            text = letter,
            style = TextStyle(
                color = Color(0xFF5C400A),
                fontSize = (radius * 0.95f / density).sp,
                fontWeight = FontWeight.Black
            )
        )
        drawText(
            textLayoutResult = letterLayout,
            topLeft = Offset(-letterLayout.size.width / 2f, -letterLayout.size.height / 2f)
        )

        val emblemLayout = textMeasurer.measure(
            text = emblem,
            style = TextStyle(
                color = Color(0xFF7A560F),
                fontSize = (radius * 0.34f / density).sp
            )
        )
        drawText(
            textLayoutResult = emblemLayout,
            topLeft = Offset(-emblemLayout.size.width / 2f, radius * 0.48f - emblemLayout.size.height / 2f)
        )
    }

    // Rim light along the top edge for a glassy pop.
    drawArc(
        color = Color.White.copy(alpha = 0.35f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.94f, faceCy - faceRy * 0.94f),
        size = Size(radius * 1.88f, faceRy * 1.88f),
        style = Stroke(width = radius * 0.04f)
    )
}
