package com.example.composelearning.animcompose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * The original credit goes to the author of the content
 * https://gist.githubusercontent.com/Kyriakos-Georgiopoulos/d6052264889ecd99b0b57645360c4fb2/raw/acd76475380bcda4ca34848fa34ad1416ae49075/Lesson6.kt
 */
@Composable
fun StaggeredGridDemo() {
    val t = remember { Animatable(0f) }
    var trigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(trigger) {
        t.snapTo(0f)
        t.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 10000, easing = FastOutSlowInEasing)
        )
    }

    val rows = 4
    val cols = 4
    val delayPerItem = 0.08f

    val maxIndex = rows * cols - 1
    val totalSpan = 1 +  maxIndex * delayPerItem
    val scaledGlobal = t.value * totalSpan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .clickable { trigger++ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(rows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(cols) { c ->
                        val index = r * cols + c
                        val start = index * delayPerItem
                        val localT = ((scaledGlobal - start) / 1f).coerceIn(0f, 1f)

                        val scale = lerp(0.5f, 1f, localT)
                        val alpha = localT

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                //.scale(scale)
                                .background(
                                    color = Color(0xFF5B86E5).copy(alpha = alpha),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStaggeredGridDemo() {
    StaggeredGridDemo()
}