package com.example.composelearning.panel

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@Composable
fun BottomPanel() {

    Column(modifier = Modifier) {

        BoxWithConstraints {
            val boxHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
            Content(
                boxHeight = boxHeight
            )
        }
    }
}

