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
fun FirstScreen(
    mainViewModel: MainViewModel,
    navigateToSecondScreen:() -> Unit
) {
    LogCompositions(tag = "First Screen", msg = "${mainViewModel.hashCode()}")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Green)
            .clickable {
                navigateToSecondScreen()
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "first screen")
    }
}