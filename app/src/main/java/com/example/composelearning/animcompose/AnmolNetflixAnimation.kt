package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Faithful Compose port of Anmol Verma's Netflix intro animation from
 * mutualmobile/compose-animation-examples, which itself ports Claudio Bonfati's
 * pure-CSS recreation (https://dev.to/claudiobonfati/netflix-intro-animation-pure-css-1m0c).
 *
 * Decomposition:
 *  - The "N" is three plain Boxes with `Modifier.rotate(...)` — no Path, no clip.
 *    Two upright = left and right verticals. One rotated -19.5° = diagonal.
 *  - Each Box is painted by "brush fur": ~31 narrow gradient strips
 *    (red → transparent linear gradients) stacked horizontally inside the stroke.
 *    Translating their Y across time mimics a paintbrush sweeping across the stroke.
 *  - On top we overlay "lumières" (French for lights): ~28 tiny coloured Boxes that
 *    translate diagonally and scale up while blurred, mimicking colour sparkles in
 *    the original Netflix sting.
 *  - A `graphicsLayer.scaleX/scaleY` on the whole N drives the camera punch-in.
 *  - All six animation tracks run in parallel via independent `launch { ... }` blocks
 *    inside a single `LaunchedEffect`.
 */

private val baseColor = Color(0xffe40913)

@Composable
fun AnmolNetflixIntroAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NetflixIntroN(Modifier)
        }
    }
}

@Composable
private fun NetflixIntroN(modifier: Modifier) {
    val zoomInNetflixBox = remember { Animatable(1f) }
    // Color Animatable lives in `androidx.compose.animation` (not `.core`); use FQN to
    // avoid a name clash with the Float Animatable we use everywhere else.
    val fadingLumieresBox =
        remember { androidx.compose.animation.Animatable(baseColor.copy(alpha = 0.5f)) }
    val showingLumieres = remember { Animatable(0f) }
    val brushMovingBrush1 = remember { Animatable(0f) }
    val brushMovingBrush2 = remember { Animatable(0f) }
    val brushMovingBrush3 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            zoomInNetflixBox.animateTo(
                targetValue = 16f,
                animationSpec = keyframes {
                    durationMillis = 3500
                    delayMillis = 500
                    1f at 0 using LinearEasing
                    5f at 750 using LinearEasing
                    10f at 1750 using LinearEasing
                    16f at 3500 using LinearEasing
                },
            )
        }
        launch {
            fadingLumieresBox.animateTo(
                targetValue = baseColor.copy(alpha = 0f),
                animationSpec = keyframes {
                    durationMillis = 2000
                    delayMillis = 600
                    baseColor.copy(alpha = 0.5f) at 0 using LinearEasing
                    baseColor.copy(alpha = 0f) at 2500 using LinearEasing
                },
            )
        }
        launch {
            brushMovingBrush1.animateTo(
                targetValue = -100f,
                animationSpec = keyframes {
                    durationMillis = 3500
                    delayMillis = 1200
                    0f at 0 using LinearEasing
                    -100f at 3500 using LinearEasing
                },
            )
        }
        launch {
            brushMovingBrush3.animateTo(
                targetValue = -100f,
                animationSpec = keyframes {
                    durationMillis = 2500
                    delayMillis = 800
                    0f at 0 using LinearEasing
                    -100f at 2500 using LinearEasing
                },
            )
        }
        launch {
            brushMovingBrush2.animateTo(
                targetValue = -100f,
                animationSpec = keyframes {
                    durationMillis = 2500
                    delayMillis = 500
                    0f at 0 using LinearEasing
                    -100f at 2500 using LinearEasing
                },
            )
        }
        launch {
            showingLumieres.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 2500
                    delayMillis = 1600
                    0f at 0 using LinearEasing
                    1f at 2500 using LinearEasing
                },
            )
        }
    }

    val nWidth = with(LocalDensity.current) { 300f.toDp() }
    val nHeight = with(LocalDensity.current) { 300f.toDp() }

    Box(
        modifier = modifier
            .width(nWidth)
            .height(nHeight)
            .graphicsLayer(
                scaleX = zoomInNetflixBox.value,
                scaleY = zoomInNetflixBox.value,
            ),
    ) {
        // Left vertical (with lumières overlay)
        EffectBrushOne(
            modifier = Modifier
                .fillMaxWidth(0.195f)
                .fillMaxHeight()
                .offset(x = (22.4 / 100).times(nWidth), y = 0.dp)
                .rotate(180f)
                .background(fadingLumieresBox.value),
            brushMoving = brushMovingBrush1,
            showingLumieres = showingLumieres,
            nWidth = nWidth,
        )

        // Right vertical
        EffectBrush(
            brushMoving = brushMovingBrush2,
            modifier = Modifier
                .fillMaxWidth(0.19f)
                .fillMaxHeight()
                .offset(x = (57.8 / 100).times(nWidth), y = 0.dp)
                .rotate(180f),
            nWidth = nWidth,
        )

        // Diagonal stroke = same brush, just rotated -19.5°
        EffectBrush(
            brushMoving = brushMovingBrush3,
            modifier = Modifier
                .fillMaxWidth(0.19f)
                .fillMaxHeight(1.5f)
                .offset(x = (40.5 / 100).times(nWidth), y = (-25 / 100).times(nHeight))
                .rotate(-19.5f),
            nWidth = nWidth,
        )
    }
}

@Composable
private fun EffectBrushOne(
    modifier: Modifier = Modifier,
    brushMoving: Animatable<Float, AnimationVector1D>,
    showingLumieres: Animatable<Float, AnimationVector1D>,
    nWidth: Dp,
) {
    EffectBrush(brushMoving, modifier, nWidth)

    EffectLumieres(
        showingLumieres = showingLumieres,
        modifier = Modifier
            .fillMaxWidth(0.195f)
            .fillMaxHeight()
            .offset(x = (22.4 / 100).times(nWidth), y = 0.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Brush "fur" — the painterly texture of each stroke
// ─────────────────────────────────────────────────────────────────────────────

private data class BrushFurModel(val left: Float, val width: Float, val background: Brush)

@Composable
private fun EffectBrush(
    brushMoving: Animatable<Float, AnimationVector1D>,
    modifier: Modifier,
    nWidth: Dp,
) {
    val brushList by remember { mutableStateOf(brushFurList.reversed()) }
    val height = LocalDensity.current.run { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    Box(
        modifier = modifier.graphicsLayer(translationY = brushMoving.value),
    ) {
        repeat(brushList.size) { i ->
            val brushFur = brushList[i]
            val xOffset =
                (brushFur.left / 100) *
                    LocalDensity.current.run { nWidth.toPx() * 0.19f }
            Box(
                modifier = Modifier
                    .width(LocalDensity.current.run { brushFur.width.toDp() })
                    .offset { IntOffset(xOffset.toInt(), 0) }
                    .fillMaxHeight()
                    .graphicsLayer(translationY = brushMoving.value * (height / 100))
                    .background(brushFur.background),
            )
        }
    }
}

private val brushFurList: List<BrushFurModel> = mutableListOf<BrushFurModel>().apply {
    add(BrushFurModel(0f, 3.8f, Brush.linearGradient(0f to baseColor, 0.15f to baseColor, 0.81f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(3.8f, 2.8f, Brush.linearGradient(0f to baseColor, 0.10f to baseColor, 0.62f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(6.6f, 4.8f, Brush.linearGradient(0f to baseColor, 0.37f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(11.4f, 4f, Brush.linearGradient(0f to baseColor, 0.23f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(15.4f, 4f, Brush.linearGradient(0f to baseColor, 0.15f to baseColor, 0.86f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(19.4f, 2.5f, Brush.linearGradient(0f to baseColor, 0.27f to baseColor, 0.89f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(21.9f, 4f, Brush.linearGradient(0f to baseColor, 0.20f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(25.9f, 2f, Brush.linearGradient(0f to baseColor, 0.30f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(27.9f, 4f, Brush.linearGradient(0f to baseColor, 0.35f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(31.9f, 3.5f, Brush.linearGradient(0f to baseColor, 0.39f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(35.4f, 2f, Brush.linearGradient(0f to baseColor, 0.34f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(37.4f, 2.6f, Brush.linearGradient(0f to baseColor, 0.22f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(40f, 6f, Brush.linearGradient(0f to baseColor, 0.47f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(46f, 2f, Brush.linearGradient(0f to baseColor, 0.36f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(48f, 5.5f, Brush.linearGradient(0f to baseColor, 0.29f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(53.5f, 3f, Brush.linearGradient(0f to baseColor, 0.39f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(56.5f, 4.1f, Brush.linearGradient(0f to baseColor, 0.45f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(60.6f, 2.4f, Brush.linearGradient(0f to baseColor, 0.34f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(63f, 4f, Brush.linearGradient(0f to baseColor, 0.47f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(67f, 1.5f, Brush.linearGradient(0f to baseColor, 0.27f to baseColor, 0.95f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(68.5f, 2.8f, Brush.linearGradient(0f to baseColor, 0.37f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(71.3f, 2.3f, Brush.linearGradient(0f to baseColor, 0.09f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(73.6f, 2.2f, Brush.linearGradient(0f to baseColor, 0.28f to baseColor, 0.92f to Color(0, 0, 0, 0), 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(75.8f, 1f, Brush.linearGradient(0f to baseColor, 0.37f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(76.8f, 2.1f, Brush.linearGradient(0f to baseColor, 0.28f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(78.9f, 4.1f, Brush.linearGradient(0f to baseColor, 0.21f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(83f, 2.5f, Brush.linearGradient(0f to baseColor, 0.21f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(85.5f, 4.5f, Brush.linearGradient(0f to baseColor, 0.39f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(90f, 2.8f, Brush.linearGradient(0f to baseColor, 0.30f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(92.8f, 3.5f, Brush.linearGradient(0f to baseColor, 0.19f to baseColor, 1f to Color(0, 0, 0, 0))))
    add(BrushFurModel(96.3f, 3.7f, Brush.linearGradient(0f to baseColor, 0.37f to baseColor, 1f to Color(0, 0, 0, 0))))
}

// ─────────────────────────────────────────────────────────────────────────────
// Lumières — coloured light sparkles overlaid on the left vertical
// ─────────────────────────────────────────────────────────────────────────────

private data class Lamp(
    val color: Color,
    val z: Float = 1f,
    val left: Float,
    val width: Float,
    val animDelay: Float,
)

private const val LUMIERE_LEFT = "left"
private const val LUMIERE_RIGHT = "right"

@Composable
private fun EffectLumieres(
    showingLumieres: Animatable<Float, AnimationVector1D>,
    modifier: Modifier,
) {
    val width = LocalConfiguration.current.screenWidthDp
    Box(modifier = modifier.graphicsLayer(alpha = showingLumieres.value)) {
        repeat(lamps.size) { i ->
            val lamp = lamps[i]
            val animName = if (i % 2 == 0) LUMIERE_RIGHT else LUMIERE_LEFT
            val offsetX = LocalDensity.current.run { width.times(lamp.left.div(150)) }
            LampComposable(
                modifier = Modifier
                    .offset { IntOffset(x = offsetX.toInt(), y = 0) }
                    .width(LocalDensity.current.run { lamp.width.toDp() })
                    .fillMaxHeight()
                    .background(lamp.color)
                    .zIndex(lamp.z),
                animName = animName,
                lamp = lamp,
            )
        }
    }
}

@Composable
private fun LampComposable(
    modifier: Modifier,
    animName: String,
    lamp: Lamp,
) {
    val translate = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(lamp) {
        if (animName == LUMIERE_LEFT) {
            launch {
                translate.animateTo(
                    targetValue = 120f,
                    animationSpec = keyframes {
                        durationMillis = 5000
                        delayMillis = lamp.animDelay.toInt()
                        0f at 0 using LinearEasing
                        10f at 1000 using LinearEasing
                        60f at 1250 using LinearEasing
                        120f at 5000 using LinearEasing
                    },
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 3f,
                    animationSpec = keyframes {
                        durationMillis = 5000
                        delayMillis = lamp.animDelay.toInt()
                        1f at 1000 using LinearEasing
                        3f at 5000 using LinearEasing
                    },
                )
            }
        } else {
            launch {
                translate.animateTo(
                    targetValue = -120f,
                    animationSpec = keyframes {
                        durationMillis = 5000
                        delayMillis = lamp.animDelay.toInt()
                        0f at 0 using LinearEasing
                        -10f at 1000 using LinearEasing
                        -60f at 1250 using LinearEasing
                        -120f at 5000 using LinearEasing
                    },
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 3f,
                    animationSpec = keyframes {
                        durationMillis = 5000
                        delayMillis = lamp.animDelay.toInt()
                        1f at 1000 using LinearEasing
                        3f at 5000 using LinearEasing
                    },
                )
            }
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer(
                translationX = translate.value,
                translationY = translate.value,
                scaleY = scale.value,
                scaleX = scale.value,
            )
            .blur(4.dp),
    )
}

private val random = Random(500)

private val lamps: List<Lamp> = mutableListOf<Lamp>().apply {
    add(Lamp(Color(0xffffff0100), z = 6f, left = 0.7f, width = 1f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffffde01), left = 2.2f, width = 1.4f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff00cc), left = 5.8f, width = 2.1f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff04fd8f), left = 10.1f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff0100), left = 12.9f, width = 1.4f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff9600), left = 15.3f, width = 2.8f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff0084ff), left = 21.2f, width = 2.5f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfff84006), left = 25f, width = 2.5f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfffffc601), left = 30.5f, width = 3f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfffff4800), left = 36.3f, width = 3f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffffd0100), left = 41f, width = 2.2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff01ffff), left = 44.2f, width = 2.6f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfffffc601), left = 51.7f, width = 0.5f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfffffc601), z = 1f, left = 52.1f, width = 1.8f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff0078fe), z = 1f, left = 53.5f, width = 2.3f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff0080ff), z = 1f, left = 57.2f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffffae01), z = 1f, left = 62.3f, width = 2.9f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff00bf), z = 1f, left = 65.8f, width = 1.7f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffa601f4), z = 1f, left = 72.8f, width = 0.8f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xfff30b34), z = 1f, left = 74.3f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff00bf), z = 1f, left = 79.8f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff04fd8f), z = 1f, left = 78.2f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff01ffff), z = 1f, left = 78.5f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffa201f4), z = 1f, left = 85.3f, width = 1.1f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffec0014), z = 1f, left = 86.9f, width = 1.1f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff0078fe), z = 1f, left = 88.8f, width = 2f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xffff0036), z = 1f, left = 92.4f, width = 2.4f, animDelay = random.nextFloat() / 100))
    add(Lamp(Color(0xff06f98c), z = 1f, left = 96.2f, width = 2.1f, animDelay = random.nextFloat() / 100))
}