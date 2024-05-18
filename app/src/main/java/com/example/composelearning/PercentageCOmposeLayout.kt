package com.example.composelearning

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun PercentageBaseLayout() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val imageHeight = 100.dp // Fixed height for your image
        val percentage = 1.0f // Percentage value between 0.0f and 1.0f

        val y =
            LocalDensity.current.run {
                (((constraints.maxHeight * percentage).toInt().toDp()) - (imageHeight * percentage)) // Offset considering image height
            }

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Shadow Stencil",
            modifier = Modifier
                .height(height = imageHeight)
                .fillMaxWidth()
                .offset(y = y)
        )

        HorizontalDivider(
            color = Color.Red,
            modifier = Modifier
                .height(2.dp)
                .align(Alignment.Center)
        )
    }
}


