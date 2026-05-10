package com.example.composelearning.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawScaleOnTouch(onBack: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { _, zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Scale & Gradient") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                // The transformable modifier handles pinch-to-zoom and dragging
                .transformable(state = state)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    // graphicsLayer applies the transformations efficiently
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2
                val centerX = width / 2

                val path = Path().apply {
                    moveTo(centerX, centerY - 150f) // Top point
                    lineTo(centerX - 150f, centerY + 150f) // Bottom left
                    lineTo(centerX + 150f, centerY + 150f) // Bottom right
                    close()
                }

                // Create a beautiful gradient brush for the stroke
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2196F3), // Blue
                        Color(0xFFE91E63), // Pink
                        Color(0xFFFFEB3B)  // Yellow
                    ),
                    start = Offset(centerX - 150f, centerY - 150f),
                    end = Offset(centerX + 150f, centerY + 150f)
                )

                drawPath(
                    path = path,
                    brush = gradientBrush,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawScaleOnTouchPreview() {
    DrawScaleOnTouch(onBack = {})
}
