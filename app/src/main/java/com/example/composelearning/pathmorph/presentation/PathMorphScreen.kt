package com.example.composelearning.pathmorph.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.pathmorph.domain.model.PhoneShape
import kotlin.math.floor

private val ViewBox = Size(100f, 300f)
private val Teal = Color(0xFF4CCADC)

@Composable
fun PathMorphScreen(
    viewModel: PathMorphViewModel = viewModel(factory = PathMorphViewModel.Factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PathMorphContent(state)
}

@Composable
private fun PathMorphContent(state: PathMorphState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Teal)
            .systemBarsPadding(),
    ) {
        if (state.isLoading || state.phones.size < 2) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            return
        }
        Morph(state.phones)
    }
}

@Composable
private fun Morph(phones: List<PhoneShape>) {
    // Parse each path once; lerp segments per slider change.
    val parsed = remember(phones) { phones.map { parseSvgPath(it.pathData) } }
    val lastIndex = phones.lastIndex
    var slider by remember { mutableFloatStateOf(0f) }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "SVG Path Morphing",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 32.dp, vertical = 8.dp),
        ) {
            val i = floor(slider).toInt().coerceIn(0, lastIndex - 1)
            val t = (slider - i).coerceIn(0f, 1f)
            val from = parsed[i]
            val to = parsed[i + 1]

            val fit = FitBox(ViewBox, Rect(0f, 0f, size.width, size.height))
            val morphed = lerpSegments(from, to, t).toPath { x, y -> fit.point(x, y) }
            drawPath(morphed, color = Color.Black)

            // Morph the screen cut-out too.
            val a = phones[i].screen
            val b = phones[i + 1].screen
            fun lerp(x: Float, y: Float) = x + (y - x) * t
            val screen = fit.rect(
                lerp(a.x, b.x), lerp(a.y, b.y), lerp(a.width, b.width), lerp(a.height, b.height),
            )
            drawRect(
                color = Color.White,
                topLeft = screen.topLeft,
                size = screen.size,
            )
        }

        val nearest = phones[slider.toInt().coerceIn(0, lastIndex)].label
        Text(
            text = nearest,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        )
        Slider(
            value = slider,
            onValueChange = { slider = it },
            valueRange = 0f..lastIndex.toFloat(),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Box(Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Drag the slider to morph between phone eras",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
