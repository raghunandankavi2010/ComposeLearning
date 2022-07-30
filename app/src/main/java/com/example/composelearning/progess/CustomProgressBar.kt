package com.example.composelearning.progess

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// on below line we are creating a function for custom progress bar.
@Composable
fun CustomProgressBar() {
    // in this method we are creating a column
    Column(
        // in this column we are specifying modifier to
        // align the contet within the column
        // to center of the screen.
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .fillMaxHeight(),
        // on below line we are specifying horizontal
        // and vertical alignment for the content of our column
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // in this column we are creating a variable
        // for the progress of our progress bar.
        val progressGreen = 40
        val progressYellow = 20
        val progressRed = 15
        val progressGray = 5
        // on the below line we are creating a box.
        Box(
            // inside this box we are adding a modifier
            // to add rounded clip for our box with
            // rounded radius at 15
            modifier = Modifier
                // on below line we are specifying
                // height for the box
                .height(8.dp)
                // on below line we are specifying
                // background color for box.
                .background(Color.Gray)
                // on below line we are
                // specifying width for the box.
                .width(284.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // in this box we are creating one more box.
                Box(
                    // on below line we are adding modifier to this box.
                    modifier = Modifier
                        // on below line we are
                        // specifying height as 8 dp
                        .height(8.dp)
                        .background(color = Color(0xFF0F9D58))
                        // on below line we are specifying width for the inner box
                        .width(284.dp * progressGreen / 100)
                )
                // yellow progress
                Box(
                    // on below line we are adding modifier to this box.
                    modifier = Modifier
                        // specifying height as 8 dp
                        .height(8.dp)
                        // on below line we are adding background
                        // color for our box as brush
                        .background(color = Color(0xFFFFEB3B))
                        // on below line we are specifying width for the inner box
                        .width(284.dp * progressYellow / 100)
                )
                // progress red
                Box(
                    // on below line we are adding modifier to this box.
                    modifier = Modifier
                        // on below line we are
                        // specifying height as 8 dp
                        .height(8.dp)
                        // on below line we are adding background
                        // color for our box as brush
                        .background(color = Color(0xFFE91E63))
                        // on below line we are specifying width for the inner box
                        .width(284.dp * progressRed / 100)
                )
                // progress grey
                Box(
                    // on below line we are adding modifier to this box.
                    modifier = Modifier
                        // on below line we are
                        // specifying height as 8 dp
                        .height(8.dp)
                        // on below line we are adding background
                        // color for our box as brush
                        .background(color = Color(0xFF5E5A5B))
                        // on below line we are specifying width for the inner box
                        .width(284.dp * progressGray / 100)
                )
            }
        }
    }
}

@Composable
fun MultiColorProgressCanvas(
    modifier: Modifier,
    heightOfProgress: Dp,
    cornerRadii: Dp
    ) {
    val greenAnimate = remember { Animatable(0f) }
    val yellowAnimate = remember { Animatable(0f) }
    val redAnimate = remember { Animatable(0f) }
    val greyAnimate = remember { Animatable(0f) }

    val animationDuration = 300

    LaunchedEffect(greyAnimate,yellowAnimate,redAnimate,greenAnimate,redAnimate) {
        launch {
            greenAnimate.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration))
            yellowAnimate.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration))
            redAnimate.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration))
            greyAnimate.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration))
        }
    }

    Canvas(
        modifier = modifier
    ) {
        //draw shapes here
        // get canvas width in dp
        val canvasWidth = size.width.toDp()
        // calculate the progress for each color
        val progressGreen = 40
        val greenSize = (canvasWidth * progressGreen) / 100
        val progressYellow = 20
        val yellowSize = (canvasWidth * progressYellow) / 100
        val progressRed = 15
        val redSize = (canvasWidth * progressRed) / 100
        val progressGray = 5
        val graySize = (canvasWidth * progressGray) / 100

        val cornerRadius = CornerRadius(cornerRadii.toPx(),cornerRadii.toPx())

        // draw the inactive part
        val progressPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(0f, 0f),
                        size = Size(canvasWidth.toPx(), heightOfProgress.toPx()),
                    ),
                    topLeft = cornerRadius,
                    bottomLeft = cornerRadius,
                    topRight = cornerRadius,
                    bottomRight = cornerRadius
                )
            )
        }
        drawPath(
            path = progressPath,
            color = Color(0xFFE0E0E0),
        )

        // draw the active progress
        // draw green progress
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(0f, 0f),
                        size = Size(greenSize.toPx() * greenAnimate.value, 8.dp.toPx()),
                    ),
                    topLeft = cornerRadius,
                    bottomLeft = cornerRadius,
                )
            )
        }
        drawPath(
            path = path,
            color = Color(0xFF69BA6E)
        )
        //draw yellow progress with offset =  green progress offset
        drawRect(
            color = Color(0xFFFEC93E),
            topLeft = Offset(greenSize.toPx(), 0f),
            size = Size(yellowSize.toPx() * yellowAnimate.value, 8.dp.toPx())
        )
        //draw red progress with offset =  yellow progress + green progress
        drawRect(
            color = Color(0xFFED5554),
            topLeft = Offset(greenSize.toPx() + yellowSize.toPx() , 0f),
            size = Size(redSize.toPx()  * redAnimate.value , 8.dp.toPx())
        )
        //draw grey progress with offset =  red progress + yellow progress + green progress
        drawRect(
            color = Color(0xFFBDBDBD),
            topLeft = Offset(greenSize.toPx() + yellowSize.toPx() + redSize.toPx() ,
                0f),
            size = Size(graySize.toPx()* greyAnimate.value , 8.dp.toPx())
        )
    }
}
