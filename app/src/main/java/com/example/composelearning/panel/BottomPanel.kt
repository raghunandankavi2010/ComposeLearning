package com.example.composelearning.panel

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.example.composelearning.speedometer.Speedometer

@Composable
fun BottomPanel() {

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints {
            Speedometer(progress = 100)
            val boxHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
            val boxWidth = with(LocalDensity.current) { constraints.maxWidth.toDp() }
            Content(
                boxWidth = boxWidth,
                boxHeight = boxHeight
            )
        }
    }
}

