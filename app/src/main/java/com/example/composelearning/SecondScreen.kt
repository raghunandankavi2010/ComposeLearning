package com.example.composelearning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun SecondScreen(
    redirectToLogin:(Boolean) -> Unit,
    secondViewModel: SecondViewModel = viewModel(),
) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Red)
                .clickable {
                    redirectToLogin(true)
                },

            contentAlignment = Alignment.Center
        ) {
            Text(text = "second screen")
        }

}

