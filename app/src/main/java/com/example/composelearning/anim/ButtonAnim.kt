package com.example.composelearning.anim

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun ButtonAnimationTest() {
    var show by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        if (show) 1f else 0f,
        animationSpec = tween(1000),
        label = ""
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Button(onClick = { show = !show }) {
            Text(text = "Click")
        }

        Spacer(modifier = Modifier.padding(32.dp))

        ButtonAnimationLayout(
            modifier = Modifier.fillMaxWidth().border(2.dp, Color.Red),
            progress = progress
        ) {
            OutlinedButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = {}
            ) {
                Text("Button 2")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            ) {
                Text("Button 1", modifier = Modifier.animateContentSize())
            }
        }
    }
}

@Composable
fun ButtonAnimationLayout(
    modifier: Modifier,
    progress: Float,
    content: @Composable () -> Unit
) {

    val measurePolicy = remember(progress) {

        object : MeasurePolicy {
            override fun MeasureScope.measure(
                measurables: List<Measurable>,
                constraints: Constraints
            ): MeasureResult {

                require(measurables.size == 2)

                val mobileButtonPlaceable =
                    measurables.first().measure(constraints.copy(minWidth = 0))

                val stationaryButtonPlaceable = measurables.last().measure(
                    Constraints.fixedWidth((constraints.maxWidth - mobileButtonPlaceable.width * progress).toInt())
                )

                return layout(
                    constraints.maxWidth, stationaryButtonPlaceable.height
                ) {

                    val width = mobileButtonPlaceable.width
                    val leftPadding = 16.dp.roundToPx()

                    mobileButtonPlaceable.placeRelative(
                        x = (-(width + leftPadding) * (1 - progress)).toInt(),
                        y = 0
                    )

                    stationaryButtonPlaceable.placeRelative(
                        x = ((width) * progress).toInt(),
                        y = 0
                    )
                }
            }

        }
    }
    Layout(
        modifier = modifier,
        measurePolicy = measurePolicy,
        content = content
    )
}