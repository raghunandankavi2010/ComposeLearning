package com.example.composelearning.lists


import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.composelearning.LogCompositions
import kotlin.math.roundToInt


@Composable
fun EquiRow() {
    val selectedIndex = remember { mutableStateOf(0) }
    val colors = listOf(
        Color.Magenta,
        Color.Red,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Black,
        Color.Red
    )

    val first = remember {
        mutableStateOf(true)
    }

    val index = remember {
        mutableStateOf(4)
    }

    val scrollState = rememberScrollState()

    val radius = with(LocalDensity.current) { 40.dp.toPx() }
    val initialX = if (index.value == 0) {
        with(LocalDensity.current) { 27.dp.toPx() }
    } else {
        with(LocalDensity.current) { ((index.value * 54.dp.toPx()) + (14.dp.toPx() * index.value) + (27.dp.toPx())) }
    }

    val initialY = with(LocalDensity.current) { 75.dp.toPx() }

    var offsetX by remember { mutableStateOf(initialX) }
    var offsetY by remember { mutableStateOf(initialY) }
    val offsetAnim = remember { Animatable(0f) }

    val scrollToPosition by remember { mutableStateOf(initialX) }

    val mapRemem = remember { mutableMapOf<Int, Offset>() }

    LaunchedEffect(key1 = offsetX) {
        offsetAnim.animateTo(
            targetValue = offsetX, animationSpec = tween(
                durationMillis = 500,
                easing = LinearEasing
            )
        )
    }

    val animValue = if (first.value) {
        Log.d("Debug", "Once")
        first.value = false
        offsetX
    } else {
        offsetAnim.value
    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth()
            .height(150.dp)
            .padding(start = 16.dp, end = 16.dp)
            .drawBehind {

                drawCircle(
                    color = Color.LightGray,
                    radius = radius,
                    center =
                    Offset(animValue, offsetY)
                )
            },
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // scroll row only first time initially to the selected index
        LaunchedEffect(key1 = scrollToPosition) {
            scrollState.animateScrollTo(scrollToPosition.roundToInt())
        }

        colors.forEachIndexed { index, color ->

            LogCompositions(tag = "For Loop", msg = "Running")
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(54.dp)
                    .height(54.dp)
                    .clip(CircleShape)
                    .background(colors[index])
                    .onGloballyPositioned { layoutCoordinates ->
                        println("🔥🔥 XXXXXXXX : ${layoutCoordinates.positionInParent().x}")
                        mapRemem[index] = Offset(
                            layoutCoordinates.boundsInParent().center.x,
                            layoutCoordinates.boundsInParent().center.y
                        )
                    }
                    .clickable {
                        offsetX = mapRemem[index]?.x!!
                        offsetY = mapRemem[index]?.y!!
                        selectedIndex.value = index
                        println("🔥🔥 POSITION : $offsetX")
                    }
            )

        }

    }
}


//            BoxWithConstraints(
//                modifier = Modifier
//                    .height(150.dp)
//                    .align(Alignment.CenterVertically)
//                    .drawBehind {
//                        if (selectedIndex.value == index) {
//                            drawCircle(
//                                color = Color.Black,
//                                radius = 140f,
//                                center =
//                                Offset(offsetX , offsetY)
//                            )
//                            println("🔥🔥 POSITION OF Background: $offsetX")
//                        }
//                    }
//            ) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .width(54.dp)
//                        .height(54.dp)
//                        .clip(CircleShape)
//                        .background(colors[index])
//                        .onGloballyPositioned { layoutCoordinates ->
//                            println("🔥🔥 XXXXXXXX : ${layoutCoordinates.positionInParent().x}")
//                            mapRemem[index] = Offset(
//                                layoutCoordinates.boundsInParent().center.x,
//                                layoutCoordinates.boundsInParent().center.y
//                            )
//                            if (index == 0) {
//                                offsetX = mapRemem[index]?.x!!
//                                offsetY = mapRemem[index]?.y!!
//                            }
//
//                        }
//                        .pointerInput(Unit) {
//                            detectTapGestures { offset ->
//                                offsetX = mapRemem[index]?.x!!
//                                offsetY = mapRemem[index]?.y!!
//                                selectedIndex.value = index
//                                println("🔥🔥 POSITION : $offsetX")
//                            }
//                        })

