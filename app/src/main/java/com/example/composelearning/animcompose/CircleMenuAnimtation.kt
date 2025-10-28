package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CircularMenuScreenWithFullAnimation() {
    val showMenu = remember { mutableStateOf(false) }
    val menuVisible = remember { mutableStateOf(false) }

    val animationDuration = 5000

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(showMenu.value) {
        if (showMenu.value) {
            menuVisible.value = true
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration)
            )
        } else {
            animationProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = animationDuration)
            )
            menuVisible.value = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Center Button
        Button(
            onClick = { showMenu.value = !showMenu.value },
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        ) {
            Text(if (showMenu.value) "ON" else "OFF")
        }

        if (menuVisible.value) {
            val numberOfButtons = 6
            val radius = 120.dp
            val buttonSize = 50.dp
            val arcStartDegrees = 0f
            // for half arc make this 180
            val arcTotalDegrees = 360f
            // for half arc make this  arcTotalDegrees / (numberOfButtons - 1)
            val angleIncrement = arcTotalDegrees / (numberOfButtons)

            // Get the current progress of the animation
            val currentProgress = animationProgress.value

            for (i in 0 until numberOfButtons) {
                val currentButtonAngleDegrees = arcStartDegrees + (angleIncrement * i)
                val angleRad = Math.toRadians(currentButtonAngleDegrees.toDouble())

                // Calculate target positions based on the full radius
                val finalCircumferenceX = (radius.value * cos(angleRad)).toFloat()
                val finalCircumferenceY = (radius.value * sin(angleRad)).toFloat()

                val animatedX = finalCircumferenceX * currentProgress
                val animatedY = finalCircumferenceY * currentProgress
                val animatedScale = currentProgress
                val animatedAlpha = currentProgress

                PeripheralButton(
                    modifier = Modifier
                        .size(buttonSize)
                        .graphicsLayer {
                            translationX = animatedX.dp.toPx()
                            translationY = animatedY.dp.toPx()
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                        .alpha(animatedAlpha),
                    text = "${i + 1}"
                )
            }
        }
    }
}

@Composable
fun PeripheralButton(modifier: Modifier = Modifier, text: String) {
    Button(
        onClick = { },
        modifier = modifier.clip(CircleShape)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCircularMenuScreenWithFullAnimation() {
    MaterialTheme {
        CircularMenuScreenWithFullAnimation()
    }
}