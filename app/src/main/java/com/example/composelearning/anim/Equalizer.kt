package com.example.composelearning.anim

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun RandomEqualizer(
    modifier: Modifier,
    barCount: Int = 3
) {
    val randomData = remember { mutableStateOf(List(barCount) { Random.nextFloat() }) }

    LaunchedEffect(Unit) {
        while (true) {
            randomData.value = List(barCount) { Random.nextFloat() }
            kotlinx.coroutines.delay(500)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(120.dp)
                .align(Alignment.Center)
                .rotate(180f),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val widthDp = 40.dp
            val heightDp = 100.dp
            val barWidthDp = widthDp / (barCount * 2)

            randomData.value.forEachIndexed { index, d ->
                val height by animateDpAsState(
                    targetValue = heightDp * d
                )


                Box(
                    Modifier
                        .width(barWidthDp)
                        .height(height)
                        .background(Color.Black)

                )
            }
        }
    }
}

@Preview
@Composable
fun RandomEqualizerPreview() {
    RandomEqualizer(
        Modifier
            .fillMaxSize()
            .background(Color.Gray),
    )
}