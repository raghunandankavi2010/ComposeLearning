package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Buttery-smooth 3D credit-card flip.
 *
 * Why this version is smooth where the previous wasn't:
 *  1. A single Animatable<Float> drives rotation — no LaunchedEffect re-arming
 *     a separate tween on every frame.
 *  2. All animated values are read INSIDE the graphicsLayer { } lambda. That
 *     lambda runs in the draw phase, so frame updates skip recomposition and
 *     only invalidate the layer — the cheapest possible path.
 *  3. Proper backface culling: each face is fully opaque while its side faces
 *     the camera and fully hidden once it's past 90°. No alpha crossfade, so
 *     there is never a "double-rendered" mushy zone near 90°.
 *  4. cameraDistance is generous (30 × density) so perspective looks real
 *     instead of fish-eyed.
 *  5. Spring is well-damped (dampingRatio 0.9, MediumLow stiffness) so the
 *     card settles cleanly with no visible oscillation.
 */
@Composable
fun CreditCardFlip(
    modifier: Modifier = Modifier,
    cardNumber: String = "4532  8912  3456  7890",
    cardHolder: String = "KYRIAKOS G.",
    expiryDate: String = "12/28",
    cvv: String = "847",
    cardType: CardType = CardType.VISA
) {
    // Single source of truth for the flip angle in degrees (0 .. 180).
    // Animatable gives us cancel-on-retap behaviour for free: a new launch
    // cancels any in-flight animation and continues from the current value,
    // which is what makes rapid taps feel responsive instead of janky.
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            // Outer horizontal padding so the card never touches the screen
            // edges — applied before .fillMaxWidth so the card itself sizes
            // to the available width *inside* the inset, and the click area
            // matches the visible card silhouette.
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(220.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Toggle target: if we're past the midpoint, snap back to 0,
                // otherwise go to 180. Reading .value here is fine — it only
                // happens on click, not every frame.
                val target = if (rotation.value < 90f) 180f else 0f
                scope.launch {
                    rotation.animateTo(
                        targetValue = target,
                        animationSpec = spring(
                            dampingRatio = 0.9f,                  // settles without bouncing
                            stiffness = Spring.StiffnessMediumLow // ~500ms feel
                        )
                    )
                }
            }
            .graphicsLayer {
                // Bigger cameraDistance = less perspective distortion.
                // The default in Compose is 8 × density which is extreme.
                cameraDistance = 30f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // Front face: rotates from 0° → 180°.
        CardFace(
            angleProvider = { rotation.value },
            isFront = true
        ) {
            CardFrontContent(
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cardType = cardType
            )
        }

        // Back face: pre-rotated by 180° so it starts facing AWAY from us and
        // ends facing us when rotation reaches 180°. This is the standard
        // two-plane flip trick — both faces share the same rotation source,
        // they're just offset by half a turn.
        CardFace(
            angleProvider = { rotation.value },
            isFront = false
        ) {
            CardBackContent(
                cvv = cvv,
                cardHolder = cardHolder,
                cardType = cardType
            )
        }
    }
}

/**
 * One side of the card. The angle is passed as a lambda so that reading it
 * happens inside graphicsLayer { }, deferring to the draw phase and skipping
 * recomposition on every animation tick.
 */
@Composable
private fun CardFace(
    angleProvider: () -> Float,
    isFront: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val angle = angleProvider()
                // The back face is mounted 180° behind the front. As `angle`
                // sweeps 0 → 180, the front goes 0 → 180 and the back goes
                // -180 → 0, so exactly one face is camera-facing at any time.
                val faceAngle = if (isFront) angle else angle - 180f
                rotationY = faceAngle
                transformOrigin = TransformOrigin(0.5f, 0.5f)

                // Backface culling. Instead of cross-fading alpha (which
                // produces a visible flash where both faces overlap), we
                // simply hide a face the moment it turns past 90°. The user
                // never sees both faces at once.
                alpha = if (faceAngle > -90f && faceAngle < 90f) 1f else 0f

                // Subtle "lift" at the midpoint of the flip. sin(angle) peaks
                // at 90° and is 0 at the endpoints, so the shadow swells
                // mid-flip and recedes at the start/end — gives the card a
                // sense of rising off the surface as it turns.
                // (GraphicsLayerScope has no translationZ; shadowElevation
                // carries the depth cue.)
                val radians = (angle * PI / 180f).toFloat()
                shadowElevation = 8f + sin(radians) * 20f

                // Shape + clip MUST live inside the graphicsLayer block.
                // shadowElevation is cast from the layer's shape, so if we
                // only `.clip(RoundedCornerShape(...))` afterwards the
                // content gets rounded but the shadow keeps a rectangular
                // outline — that's what produced the little corner
                // "stubs" sticking out past the rounded edges.
                shape = RoundedCornerShape(24.dp)
                clip = true

                // No extra scaleX/scaleY here. rotationY with cameraDistance
                // already produces the correct perspective foreshortening;
                // layering our own scale on top would leave the front and
                // back at different sizes (cos(0)=+1 vs cos(π)=-1), which is
                // what made the card visibly shrink after the flip.
            }
            .drawWithContent {
                drawContent()
                // Specular sweep: a thin highlight that travels across the
                // card as it rotates. Position is derived directly from the
                // rotation angle — no separate animator to drift out of sync.
                val angle = angleProvider()
                val faceAngle = if (isFront) angle else angle - 180f
                if (faceAngle > -90f && faceAngle < 90f) {
                    val sweep = faceAngle / 90f  // -1 .. 1 across the face
                    val glossX = size.width * (0.5f + sweep * 0.6f)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0f),
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0f)
                            ),
                            start = Offset(glossX - 120f, 0f),
                            end = Offset(glossX + 120f, size.height)
                        )
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
private fun CardFrontContent(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardType: CardType
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Deep midnight-to-indigo gradient — feels like a real metal/dark
            // premium card. Diagonal direction gives the surface a natural
            // sheen when combined with the gloss sweep in CardFace.
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F1F3D),
                        Color(0xFF1B3A6B),
                        Color(0xFF2A5298),
                        Color(0xFF1B3A6B)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Soft radial highlight in the top-right — looks like ambient light
        // bouncing off the card surface. Replaces the busy circle pattern.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(900f, 0f),
                        radius = 700f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Row 1: issuer brand + contactless ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NORTHWIND BANK",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = "Contactless",
                    tint = Color.White.copy(alpha = 0.85f),
                    // Rotated so the "waves" emit horizontally, like the real
                    // contactless symbol you see on cards.
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = 90f }
                )
            }

            // ── Row 2: EMV chip ──
            ChipElement()

            // ── Row 3: card number ──
            // Real cards group digits in fours. We render four groups in a
            // Row with even spacing so kerning stays consistent regardless
            // of input — `cardNumber` is just used as the source of digits.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                cardNumber
                    .replace(" ", "")
                    .chunked(4)
                    .forEach { group ->
                        Text(
                            text = group,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        )
                    }
            }

            // ── Row 4: holder + expiry + brand ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "CARD HOLDER",
                        style = TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cardHolder,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "VALID THRU",
                        style = TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expiryDate,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
                CardTypeLogo(type = cardType)
            }
        }
    }
}

@Composable
private fun CardBackContent(
    cvv: String,
    cardHolder: String,
    cardType: CardType = CardType.VISA
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Slightly darker palette than the front to imply depth and to
            // make the white signature panel pop.
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0B1530),
                        Color(0xFF132447),
                        Color(0xFF0B1530)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Magnetic strip ──
            // Real cards have the magstripe flush against the top, full-bleed
            // edge-to-edge. No horizontal padding here.
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF050505),
                                Color(0xFF1A1A1A),
                                Color(0xFF050505)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Signature panel + CVV ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signature strip. Real cards have diagonal "void if removed"
                // hatching behind the signature — we mimic that with a
                // repeating linear gradient.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEDEDED),
                                    Color(0xFFD8D8D8),
                                    Color(0xFFEDEDED),
                                    Color(0xFFD8D8D8),
                                    Color(0xFFEDEDED)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(40f, 40f),
                                tileMode = androidx.compose.ui.graphics.TileMode.Repeated
                            )
                        )
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = cardHolder,
                        style = TextStyle(
                            fontFamily = FontFamily.Cursive,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    )
                }

                // CVV box. Width matches the signature height so the two
                // elements feel visually balanced. The little "CVV" label
                // sits just above so it doesn't crowd the panel.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CVV",
                        style = TextStyle(
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(58.dp)
                            .height(26.dp)
                            .background(Color.White, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cvv,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E),
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Bottom row: small print + brand ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Property of issuing bank.\nIf found, return to nearest branch.",
                    style = TextStyle(
                        fontSize = 7.sp,
                        color = Color.White.copy(alpha = 0.45f),
                        lineHeight = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                CardTypeLogo(type = cardType)
            }
        }
    }
}

/**
 * EMV chip rendered as a 2×3 grid of gold contact pads. This is the most
 * recognisable visual element of a real card — the previous version had a
 * "+"-shape that didn't match what an actual chip looks like.
 */
@Composable
private fun ChipElement() {
    // The outer "frame" is a darker gold, the pads are bright gold. Thin
    // dark seams between pads come from the parent background showing
    // through the spacing between Boxes.
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B6914),
                        Color(0xFFB8860B),
                        Color(0xFF8B6914)
                    )
                )
            )
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            // Three rows of two pads each.
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFE6C547),
                                            Color(0xFFD4AF37),
                                            Color(0xFFA8821C)
                                        )
                                    ),
                                    shape = RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun CardTypeLogo(type: CardType, modifier: Modifier = Modifier) {
    when (type) {
        // VISA wordmark: italic, ExtraBold, no letter-spacing — same visual
        // recipe the real wordmark uses. Looks instantly recognisable.
        CardType.VISA -> {
            Text(
                text = "VISA",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.White
                ),
                modifier = modifier
            )
        }
        // Mastercard: two overlapping circles. Using clip+background gives a
        // perfect blend in the middle without alpha tricks.
        CardType.MASTERCARD -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color(0xFFEB001B), shape = RoundedCornerShape(11.dp))
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .offset(x = (-9).dp)
                        .background(Color(0xFFF79E1B), shape = RoundedCornerShape(11.dp))
                        .alpha(0.85f)
                )
            }
        }
        CardType.AMEX -> {
            Text(
                text = "AMEX",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                ),
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun CreditCardFlipPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CreditCardFlip()
    }
}
