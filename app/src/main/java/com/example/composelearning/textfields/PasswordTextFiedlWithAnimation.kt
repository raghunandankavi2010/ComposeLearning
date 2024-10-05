package com.example.composelearning.textfields

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Preview
@Composable
fun PasswordFieldWithAnimatedBorder() {
    var password by remember { mutableStateOf("") }
    val pathMeasure by remember { mutableStateOf(PathMeasure()) }
    val path = remember { Path() }
    val pathWithProgress by remember { mutableStateOf(Path()) }
    val animatable = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val isPasswordValid = password.length >= 8
        val borderColor = if (isPasswordValid) Color.Green else Color.Red

        Text("Password:")
        TextField(
            value = password,
            onValueChange = {
                password = it
                coroutineScope.launch {
                    animatable.animateTo(if (isPasswordValid) 100f else 0f)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp).drawBehind {
                if (path.isEmpty) {
                    path.addRoundRect(
                        RoundRect(
                            Rect(offset = Offset.Zero, size),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    )
                    pathMeasure.setPath(path, forceClosed = false)
                }

                pathWithProgress.reset()
                pathMeasure.setPath(path, forceClosed = false)
                pathMeasure.getSegment(
                    0f,
                    pathMeasure.length * animatable.value / 100f,
                    pathWithProgress,
                    startWithMoveTo = true
                )

                drawPath(
                    path = path,
                    style = Stroke(4.dp.toPx()),
                    color = Color.Black
                )

                drawPath(
                    path = pathWithProgress,
                    style = Stroke(4.dp.toPx()),
                    color = borderColor
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Text("Password is ${if (isPasswordValid) "valid" else "invalid"}")
    }
}
