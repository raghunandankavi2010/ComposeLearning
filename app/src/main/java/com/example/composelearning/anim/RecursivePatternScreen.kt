package com.example.composelearning.anim

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlin.math.roundToInt

private val RecursiveColors = listOf(
    Color.White,
    Color(0xFFC64335), // Red
    Color(0xFF1026C1), // Blue
    Color(0xFFA2F18E), // Green
    Color(0xFFE5DE44), // Yellow
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF9800), // Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B)  // Blue Grey
)

@Composable
fun RecursivePatternScreen(onBack: () -> Unit) {
    var n by remember { mutableFloatStateOf(1f) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Recursive Subdivision",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.LightGray) // Outer border
            ) {
                RecursiveBlock(
                    n = n.roundToInt(),
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = "n = ${n.roundToInt()}",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text("Recursion Depth")
                Slider(
                    value = n,
                    onValueChange = { n = it },
                    valueRange = 1f..9f,
                    steps = 7
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RecursiveBlock(
    n: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRecursiveSubdivision(0, n, size, Offset.Zero)
    }
}

private fun DrawScope.drawRecursiveSubdivision(
    level: Int,
    n: Int,
    currentSize: Size,
    offset: Offset
) {
    val color = RecursiveColors.getOrElse(level) { Color.Gray }

    if (level >= n) {
        drawRect(color = color, topLeft = offset, size = currentSize)
    } else {
        val halfWidth = currentSize.width / 2f
        val halfHeight = currentSize.height / 2f
        val quadrantSize = Size(halfWidth, halfHeight)

        // Top-Left: Recursively subdivide
        drawRecursiveSubdivision(
            level = level + 1,
            n = n,
            currentSize = quadrantSize,
            offset = offset
        )

        // Top-Right: Fill with current level color
        drawRect(
            color = color,
            topLeft = offset.copy(x = offset.x + halfWidth),
            size = quadrantSize
        )

        // Bottom-Left: Fill with current level color
        drawRect(
            color = color,
            topLeft = offset.copy(y = offset.y + halfHeight),
            size = quadrantSize
        )

        // Bottom-Right: Recursively subdivide
        drawRecursiveSubdivision(
            level = level + 1,
            n = n,
            currentSize = quadrantSize,
            offset = offset.copy(x = offset.x + halfWidth, y = offset.y + halfHeight)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecursivePatternScreenPreview() {
    ComposeLearningTheme {
        RecursivePatternScreen(onBack = {})
    }
}
