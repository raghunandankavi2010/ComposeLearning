package com.example.composelearning.textfields

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    var borderColor by remember { mutableStateOf(Color.Red) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        borderColor = if ( password.length >= 8) Color.Green else Color.Red

        Text("Password:")

        Box(
            modifier = Modifier.height(100.dp)
                .drawBehind {
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
                        color = Color.Black // Use black for the complete path
                    )

                    drawPath(
                        path = pathWithProgress,
                        style = Stroke(4.dp.toPx()),
                        color = borderColor
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.Transparent)
                ,
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                    textAlign = TextAlign.Start,
                    color = Color.Black // Adjust text color as needed
                ),
                cursorBrush = SolidColor(Color.Black) // Adjust cursor color as needed
            )
        }

        Button(onClick = {
            val isPasswordValid = password.length >= 8
            coroutineScope.launch {
                animatable.animateTo(if (isPasswordValid) 100f else 0f, animationSpec = tween(500))
            }
        }) {
            Text("Validate")
        }

        Text("Password is ${if ( password.length >= 8) "valid" else "invalid"}")
    }
}
