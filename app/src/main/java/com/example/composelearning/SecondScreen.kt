package com.example.composelearning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun SecondScreen(
    mainViewModel: MainViewModel
) {
    mainViewModel.updateSearchWidgetVisibility(false)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red),

        contentAlignment = Alignment.Center
    ) {
        Text(text = "second screen")
    }
}