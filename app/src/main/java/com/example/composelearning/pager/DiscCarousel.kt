package com.example.composelearning.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.composelearning.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// =================================================================================================
// Compose reimagining of the 2011 Android RenderScript CarouselView. The original drew textured
// quads tangent to a y-axis-aligned cylinder using OpenGL ES; cards farther from the camera were
// drawn smaller and dimmer, drag rotated the cylinder, and a fling decayed and snapped to the
// nearest slot.
//
// This version achieves the same look entirely with Modifier.graphicsLayer — no RenderScript, no
// 3D matrices in app code. We compute the per-card angle on the cylinder, then express the 3D
// projection through Compose's existing transform pipeline:
//   • translationX  = R · sin(theta)          → horizontal position on the cylinder
//   • rotationY     = -theta (degrees)        → card stays tangent to the cylinder face
//   • scale / alpha = function of cos(theta)  → depth cue
//   • cameraDistance forces the rotation to read as 3D rather than 2D skew
//
// The drag → fling → snap behaviour uses an Animatable<Float> in radians driven by a velocity
// tracker and exponentialDecay; on release we let the decay run and then animateTo the nearest
// slot with a spring, the same two-stage motion as the original.
// =================================================================================================

private data class DiscItem(
    val title: String,
    val subtitle: String,
    val drawableRes: Int,
    val accent: Color,
)

private val discItems = listOf(
    DiscItem("Tomato", "Card 01", R.drawable.tomato, Color(0xFFE53935)),
    DiscItem("Grapes", "Card 02", R.drawable.ic_grapes, Color(0xFF8E24AA)),
    DiscItem("Droid", "Card 03", R.drawable.droid, Color(0xFF43A047)),
    DiscItem("Thumb", "Card 04", R.drawable.thumb, Color(0xFF1E88E5)),
    DiscItem("Bkg", "Card 05", R.drawable.bkg, Color(0xFFFB8C00)),
    DiscItem("Ping", "Card 06", R.drawable.ping, Color(0xFFD81B60)),
    DiscItem("Test", "Card 07", R.drawable.test, Color(0xFF00ACC1)),
)

@Composable
fun DiscCarousel(modifier: Modifier = Modifier) {
    val itemCount = discItems.size
    // Spacing the cards every (2π / slotCount) means more slots than items leaves visible gaps
    // between cards on the back of the cylinder, matching the original sample where most slots
    // are empty unless 100 cards are loaded.
    val slotCount = itemCount * 2
    val anglePerSlot = (2.0 * PI / slotCount).toFloat()

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val decay = remember { exponentialDecay<Float>(frictionMultiplier = 1.2f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B1A2D), Color(0xFF101630), Color(0xFF1B0A2A)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            // Radius of the cylinder in pixels — chosen so the centre card sits at full size and
            // the side cards are visibly receding without falling off-screen.
            val radiusPx = widthPx * 0.42f
            // Pixels-per-radian for the drag gesture. The cylinder's circumference at our radius
            // is 2π·R, but mapping 1:1 makes the disc feel sluggish — divide by 2 for a snappier
            // response that still feels physically connected to the finger.
            val pxPerRadian = radiusPx / 2f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(itemCount) {
                        val tracker = VelocityTracker()
                        detectHorizontalDragGestures(
                            onDragStart = {
                                tracker.resetTracking()
                                scope.launch { rotation.stop() }
                            },
                            onDragEnd = {
                                val vxPx = tracker.calculateVelocity().x
                                val velocityRadPerSec = -vxPx / pxPerRadian
                                scope.launch {
                                    // Stage 1: decay from the release velocity. The decay returns
                                    // when motion settles; we use its final value to find the
                                    // nearest slot.
                                    val result = rotation.animateDecay(
                                        initialVelocity = velocityRadPerSec,
                                        animationSpec = decay,
                                    )
                                    val landing = result.endState.value
                                    val nearestSlot = (landing / anglePerSlot).roundToInt()
                                    rotation.animateTo(
                                        targetValue = nearestSlot * anglePerSlot,
                                        animationSpec = spring(
                                            dampingRatio = 0.75f,
                                            stiffness = 240f,
                                        ),
                                    )
                                }
                            },
                            onDragCancel = { tracker.resetTracking() },
                        ) { change, dragAmount ->
                            tracker.addPosition(change.uptimeMillis, change.position)
                            scope.launch {
                                rotation.snapTo(rotation.value - dragAmount / pxPerRadian)
                            }
                        }
                    },
            ) {
                // Render cards back-to-front. Sorting by cos(theta) ascending means the smallest
                // cosine (farthest from camera) draws first; this matches the painter's algorithm
                // the original RS shader achieved via depth test.
                val sortedIndices = remember(itemCount) { (0 until itemCount).toMutableList() }
                val angles = FloatArray(itemCount)
                for (i in 0 until itemCount) {
                    val raw = i * anglePerSlot - rotation.value
                    // Wrap to (-π, π] so the discontinuity is behind the cylinder, not in front.
                    angles[i] = wrapPi(raw)
                }
                sortedIndices.sortBy { cos(angles[it]) }

                sortedIndices.forEach { i ->
                    val theta = angles[i]
                    val cosT = cos(theta)
                    // Cull cards on the far side of the cylinder. cos < -0.05 means past 92°-ish;
                    // those would be drawn back-to-front behind everything else but at near-zero
                    // alpha anyway, so save the layer cost.
                    if (cosT < -0.05f) return@forEach

                    val sinT = sin(theta)
                    // Depth-based size and opacity. cos = 1 at the centre, 0 at 90°.
                    val depth = ((cosT + 1f) / 2f).coerceIn(0f, 1f)
                    val scale = lerp(0.45f, 1f, depth)
                    val alphaValue = lerp(0.0f, 1f, depth.coerceAtLeast(0.0f))

                    DiscCard(
                        item = discItems[i],
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                cameraDistance = 16f * density.density
                                translationX = radiusPx * sinT
                                // Subtle vertical bob so the disc reads as a turntable seen
                                // slightly from above; cards on the sides dip a few pixels.
                                translationY = (1f - depth) * 18f * density.density
                                rotationY = -Math.toDegrees(theta.toDouble()).toFloat()
                                scaleX = scale
                                scaleY = scale
                                alpha = alphaValue
                            },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Index of the slot currently nearest the camera, wrapped into the item range.
            val nearestSlot = (rotation.value / anglePerSlot).roundToInt()
            val centerIndex = ((nearestSlot % itemCount) + itemCount) % itemCount
            val centerItem = discItems[centerIndex]
            Text(
                text = centerItem.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${centerIndex + 1} / $itemCount — drag to spin",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DiscCard(item: DiscItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(item.accent.copy(alpha = 0.18f))
            .padding(2.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp)),
        ) {
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 168.dp, height = 224.dp),
            )
            // Glossy diagonal sheen — the equivalent of the glossy_overlay.png that the 2011
            // sample drew on top of every card texture.
            Box(
                modifier = Modifier
                    .size(width = 168.dp, height = 224.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.18f),
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite,
                        ),
                    ),
            )
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

private fun wrapPi(value: Float): Float {
    var v = value
    val twoPi = (2f * PI).toFloat()
    while (v > PI) v -= twoPi
    while (v <= -PI) v += twoPi
    return v
}