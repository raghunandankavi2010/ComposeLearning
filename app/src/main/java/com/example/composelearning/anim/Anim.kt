package com.example.composelearning.anim

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.composelearning.R


@Composable
fun OffsetAnim() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Black)
    ) {
        val state = remember {
            mutableStateOf(State.Start)
        }

        val density = LocalDensity.current
        val configuration = LocalConfiguration.current

        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
        val offset: Dp by animateDpAsState(
            targetValue = if (state.value == State.Start) 20.dp else screenWidthPx.dp - 60.dp
        )

        OffsetAnim(offset)

        LaunchButton(modifier = Modifier.align(Alignment.BottomCenter), state.value) {
            state.value = it
        }

    }
}

enum class State {
    Start,
    End
}

@Composable
fun OffsetAnim(offset: Dp) {
    val painter: Painter = painterResource(id = R.drawable.ic_edit)

    Image(
        painter = painter,
        contentDescription = "icon offset",
        modifier = Modifier
            .height(25.dp)
            .width(25.dp)
            .offset { IntOffset(x = offset.value.toInt(), y = 0) }

    )

}

@Composable
fun LaunchButton(
    modifier: Modifier,
    animationState: State,
    onToggleAnimationState: (State) -> Unit
) {
    Row(
        modifier = modifier
            .wrapContentHeight()
            .wrapContentWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (animationState == State.Start) {
            Button(
                onClick = { onToggleAnimationState(State.End) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Launch")
            }
        } else {
            Button(
                onClick = { onToggleAnimationState(State.Start) },
            ) {
                Text(text = "STOP")
            }
        }
    }
}