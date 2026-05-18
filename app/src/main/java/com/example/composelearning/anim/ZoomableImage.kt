package com.example.composelearning.anim

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
fun ZoomableImage(
    imagePainter: Painter,
    modifier: Modifier = Modifier,
    maxScale: Float = 5f,
    minScale: Float = 1f
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Updated to use the non-deprecated signature (centroid as first parameter)
    val state = rememberTransformableState { _, zoomChange, offsetChange, _ ->
        // 1. Limit scale to a reasonable range
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)

        // 2. Only allow panning if we are zoomed in
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .size(200.dp) // Apply a default size if not overridden
            .clipToBounds() // Prevents the image from bleeding outside its container
            .pointerInput(Unit) {
                // 3. Add double tap to reset
                detectTapGestures(
                    onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    }
                )
            }
            .transformable(state = state)
    ) {
        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // 4. Use lambda version of graphicsLayer to avoid recomposition
                    // when scale/offset change. This is better for performance.
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoomableImageScreen(onBack: () -> Unit = {}) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text("Zoomable Image") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            ZoomableImage(
                imagePainter = painterResource(R.drawable.tomato),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun TransformableSample() {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Updated to use the non-deprecated signature (centroid as first parameter)
    val state = rememberTransformableState { _, zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            // The listener is on the background/container
            .transformable(state = state)
    ) {
        Box(
            Modifier
                .size(200.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    // Performance optimization:
                    // Changes here don't trigger Recomposition
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    translationX = offset.x
                    translationY = offset.y
                }
                .background(Color.Blue)
        )
    }
}
