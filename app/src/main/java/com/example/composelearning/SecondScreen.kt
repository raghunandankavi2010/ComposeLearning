package com.example.composelearning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun SecondScreen(
    showIcon:() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red).clickable {
                                showIcon()
            },

        contentAlignment = Alignment.Center
    ) {
        Text(text = "second screen")
    }
}