package com.example.composelearning.graphics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composelearning.speedometer.Speedometer3
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials


@Composable
fun BlurSample(modifier: Modifier = Modifier) {

    Box {
        Speedometer3(modifier.blur(10.dp, BlurredEdgeTreatment.Unbounded), 30, 30, 30, 10, 50)
        Text("Testing Blur in compose", Modifier.align(Alignment.Center))
    }
}


@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BlurSampleWithHaze(modifier: Modifier = Modifier) {
    val hazeState = remember { HazeState() }
    Box {
        Speedometer3(
            modifier
                .hazeSource(state = hazeState)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            30,
            30,
            30,
            10,
            50
        )

       val style=  HazeStyle(backgroundColor = Color.Transparent, tints =  emptyList<HazeTint>(), blurRadius = 10.dp)
        Box(
            modifier = modifier
                .hazeEffect(state = hazeState, style = HazeMaterials.regular())
                .height(136.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Testing Blur in compose",
                modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center

            )
        }
    }
}