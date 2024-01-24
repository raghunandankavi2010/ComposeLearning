package com.example.composelearning.lists


import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
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
        Color.Red,
        Color.Magenta,
        Color.Black,
        Color.Red
    )

    val imageBitmap = ImageBitmap.imageResource(R.drawable.test)
    val image = remember {
        imageBitmap
    }

    val borderColor = remember {
        mutableStateOf(Color.Transparent)
    }


    val first = remember {
        mutableStateOf(true)
    }

    val index = remember {
        mutableIntStateOf(4)
    }

    val scrollState = rememberScrollState()

    val radius = with(LocalDensity.current) { 40.dp.toPx() }
    val initialX = if (index.value == 0) {
        with(LocalDensity.current) { 27.dp.toPx() }
    } else {
        with(LocalDensity.current) { ((index.value * 54.dp.toPx()) + (14.dp.toPx() * index.value) + (27.dp.toPx())) }
    }

    val initialY = with(LocalDensity.current) { 30.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(initialX) }
    var offsetY by remember { mutableFloatStateOf(initialY) }
    val offsetAnim = remember { Animatable(0f) }

    val scrollToPosition by remember { mutableFloatStateOf(initialX) }

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
            .height(60.dp)
            .padding(start = 16.dp, end = 16.dp)
            .drawBehind {
               val colorFilter = if(selectedIndex.value == 3) {
                   ColorFilter.tint(Color.Blue)
                } else {
                    ColorFilter.tint(Color.Red)
                }
                drawArc(
                color = Color.Gray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(animValue - 37.dp.toPx()  , offsetY  - 37.dp.toPx() ),
                size = Size(74.dp.toPx(), 74.dp.toPx())
                )

//                drawImage(
//                    image,
//                    dstOffset = IntOffset(
//                        animValue.roundToInt() - image.width / 2, offsetY.toInt() - image.height / 2
//                    ),
//                    colorFilter = colorFilter
//                )

//                drawCircle(
//                    color = Color.LightGray,
//                    radius = radius,
//                    center =
//                    Offset(animValue, offsetY)
//                )
            },
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // scroll row only first time initially to the selected index
        LaunchedEffect(key1 = scrollToPosition) {
            scrollState.animateScrollTo(scrollToPosition.roundToInt())
        }

        colors.forEachIndexed { index, color ->

            LogCompositions(tag = "For Loop", msg = "Running")
            if(index == 3) {
                borderColor.value = Color.Red
            } else {
                borderColor.value = Color.Blue
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(54.dp)
                    .height(54.dp)
                    .border(

                        BorderStroke(2.dp, borderColor.value),
                        CircleShape
                    )
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
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://i.imgur.com/tGbaZCY.jpg")
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.droid),
                    contentDescription = "Crop Image",
                    contentScale = ContentScale.Crop,
                    modifier  = Modifier
                        .clip(CircleShape)
                        .height(54.dp)
                )
            }

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

