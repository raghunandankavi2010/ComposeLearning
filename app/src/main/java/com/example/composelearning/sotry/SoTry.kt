package com.example.composelearning.sotry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


enum class ButtonTypes {
    EXTRA_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

val ButtonShape = RoundedCornerShape(250.dp)

@Preview
@Composable
fun ButtonSandbox() {
    Button(
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        onClick = {},
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
}


@Composable
fun JKButton2(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 0.dp,
    buttonTypes: ButtonTypes = ButtonTypes.MEDIUM,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val newModifier = if (borderColor != null) {
        modifier.border(1.dp, borderColor, shape = ButtonShape)
    } else {
        modifier
    }

    val (finalModifier, paddingValues) = when (buttonTypes) {

        ButtonTypes.EXTRA_SMALL -> {
            newModifier.height(24.dp) to PaddingValues(vertical = 4.dp, horizontal = 12.dp)
        }

        ButtonTypes.SMALL -> {
            newModifier.height(32.dp) to PaddingValues(vertical = 4.dp, horizontal = 12.dp)
        }

        ButtonTypes.MEDIUM -> {
            newModifier.height(40.dp) to PaddingValues(vertical = 8.dp, horizontal = 16.dp)

        }

        ButtonTypes.LARGE -> {
            newModifier.height(48.dp) to PaddingValues(vertical = 12.dp, horizontal = 24.dp)
        }

        ButtonTypes.EXTRA_LARGE -> {
            newModifier.height(64.dp) to PaddingValues(vertical = 16.dp, horizontal = 32.dp)
        }
    }

    Surface(
        modifier = finalModifier
            .semantics { role = Role.Button }
            .clip(ButtonShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                onClick()
            },
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        Row(
            Modifier
                .padding(paddingValues),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
fun CustomRoundedButton(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val newModifier = if (borderColor != null) {
        modifier.border(1.dp, borderColor, shape = ButtonShape)
    } else {
        modifier
    }

    val finalModifier = newModifier.height(48.dp)

    Surface(
        modifier = finalModifier
            .semantics { role = Role.Button }
            .clip(ButtonShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                onClick()
            },
        color = backgroundColor,
        shadowElevation = elevation
    ) {
        Row(
            Modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
fun SOTry() {
//
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        val superscript = SpanStyle(
//            baselineShift = BaselineShift.Superscript,
//            fontSize = 16.sp,
//            color = Color.Red
//        )
//        Text(
//            fontSize = 20.sp,
//            text = buildAnnotatedString {
//                append("22")
//                withStyle(superscript) {
//                    append("2")
//                }
//            }
//        )
//    }
//}

    val scope = rememberCoroutineScope()

    var isMessageReceived by remember {
        mutableStateOf(false)
    }

    var checkedState by remember {
        mutableStateOf(false)
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isMessageReceived) Color.Green else Color.Gray,
        label = "color"
    )

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = {
                checkedState = !checkedState
                scope.launch {
                    isMessageReceived = true
                    delay(800)
                    isMessageReceived = false
                }
            },
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = "Your text goes here",
            modifier = Modifier,
            color = animatedColor
        )
    }
}

@Composable
fun Tracks(
    tracks: List<Track>?,
) {
    if (tracks.isNullOrEmpty()) return
    var screenWidthSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    var playingTrackSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val listState = rememberLazyListState()
    val indexOfPlayingTrack by remember(tracks) {
        derivedStateOf {
            tracks.indexOfFirst {
                it.isCurrentlyPlaying
            }
        }
    }

    LaunchedEffect(indexOfPlayingTrack, tracks, playingTrackSize, screenWidthSize) {
        if (indexOfPlayingTrack != -1) {
            // scroll and centre
            listState.scrollToItem(
                indexOfPlayingTrack,
                (playingTrackSize.width - screenWidthSize.width) / 2
            )
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
                screenWidthSize = it
            },
        state = listState
    ) {
        items(tracks) { track ->
            Text(
                modifier = Modifier
                    .width(80.dp)
                    .onSizeChanged {
                        if (track.isCurrentlyPlaying) {
                            playingTrackSize = it
                        }
                    },
                text = track.name,
            )
        }
    }
}

data class Track(val isCurrentlyPlaying: Boolean, val name: String)

fun getTracksLists(): List<Track> {
    val list = ArrayList<Track>()
    repeat(40) {
        list.add(Track(false, "name$it"))
    }
    return list
}

@Composable
fun UI() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (image, columnTexts) = createRefs()

        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(300.dp)
                .clickable { }
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
        )


        Column(modifier = Modifier
            .wrapContentHeight()
            .constrainAs(columnTexts) {
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom, margin = 16.dp)
            }) {
            Text("Text1", modifier = Modifier.wrapContentWidth())
            Text("Text2", modifier = Modifier.wrapContentWidth())
        }
    }
}

@Composable
fun AlternateUI() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(300.dp)
                    .clickable { })
        }
        BottomText(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BottomText(modifier: Modifier) {
    Column(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(10.dp),
    ) {
        Text(text = "line1", textAlign = TextAlign.Center)
        Text(text = "line2", textAlign = TextAlign.Center)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollpasingToolbar() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Medium TopAppBar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val list = (0..75).map { it.toString() }
                items(count = list.size) {
                    Text(
                        text = list[it],
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun NumberBox(
    number: Int,
    modifier: Modifier = Modifier
        .width(300.dp)
        .height(300.dp),
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current
) {

    val mergedStyle = style.merge(
        textAlign?.let {
            TextStyle(
                color = color,
                fontSize = 40.sp,
                fontWeight = fontWeight,
                textAlign = it,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                fontStyle = fontStyle,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            )
        }
    )

    val measurer = rememberTextMeasurer()

    val result = measurer.measure(
        AnnotatedString(number.toString()),
        style = mergedStyle,
        maxLines = 1,
    )

    Canvas(modifier = modifier, onDraw = {
        //drawText(measurer, text = size.toString())
        //drawText(measurer, text = result.size.height.toString())
        translate(
            left = center.x - result.size.width / 2,
            top = center.y - result.size.height / 2
        ) {
            drawText(
                textLayoutResult = result,
                color = Color.Blue,
            )
            drawLine(
                color = Color.Green,
                start = Offset(0f, result.firstBaseline),
                end = Offset(size.width, result.firstBaseline),
            )
            drawRect(
                color = Color.Red,
                size = result.size.toSize(),
                style = Stroke()
            )
        }


    })
}

@Composable
fun Avatar(avatarSize: Int = 200) {

    val imageBitmapDst = ImageBitmap.imageResource(R.drawable.thumb)

    val imageBitmapSrc = ImageBitmap.imageResource(R.drawable.droid)


    Canvas(modifier = Modifier.size(avatarSize.dp)) {
        val dimension = size.height.coerceAtMost(size.width) / 2f
        val xPos = (size.width - dimension) / 2f
        val yPos = (size.height - dimension) / 2f

        drawImage(
            image = imageBitmapDst,
        )

        drawImage(
            image = imageBitmapSrc,
            dstOffset = IntOffset(xPos.toInt(), yPos.toInt()),
            dstSize = IntSize(dimension.toInt(), dimension.toInt()),
            blendMode = BlendMode.SrcIn
        )
    }
}

@Composable
fun ButtonWithProgress() {
    Button(modifier = Modifier.size(200.dp, 100.dp),
        onClick = { }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = Color.White)
            Text("Some action")
        }
    }
}

/**
 * Credit Source: https://www.jetpackcompose.app/articles/donut-hole-skipping-in-jetpack-compose
 * Use layout inspector to check recomposition count
 */
@Composable
fun MyComponent() {
    var counter by remember { mutableStateOf(0) }

    LogCompositions("JetpackCompose.app", "MyComposable function")
    CustomButton(onClick = { counter++ }) {
        LogCompositions("JetpackCompose.app", "CustomButton scope")
        CustomText(
            text = "Counter: $counter",
            modifier = Modifier
                .clickable {
                    counter++
                },
        )
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    LogCompositions("JetpackCompose.app", "CustomButton function")
    Button(onClick = onClick, modifier = Modifier.padding(16.dp)) {
        LogCompositions("JetpackCompose.app", "Button function")
        content()
    }
}

@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier,
) {
    LogCompositions("JetpackCompose.app", "CustomText function")

    Text(
        text = text,
        modifier = modifier.padding(32.dp),
        style = TextStyle(
            fontSize = 20.sp,
            textDecoration = TextDecoration.Underline,
            fontFamily = FontFamily.Monospace
        )
    )
}

@Composable
fun StandardButton(modifier: Modifier, onClicked: () -> Unit) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .topBorder(6.dp, Color.White, 50.dp)
            .background(Color.Red),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 50.dp, bottomEnd = 50.dp),
        onClick = onClicked
    ) {
        Text(text = "Button")
    }
}

@Composable
fun Modifier.topBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp): Modifier {

    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }
    val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

    return this then Modifier.drawBehind {
        val width = size.width
        val height = size.height

        drawLine(
            color = color,
            start = Offset(x = 0f, y = height),
            end = Offset(x = 0f, y = cornerRadiusPx),
            strokeWidth = strokeWidthPx
        )

        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset.Zero,
            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
            style = Stroke(width = strokeWidthPx)
        )

        drawLine(
            color = color,
            start = Offset(x = cornerRadiusPx, y = 0f),
            end = Offset(x = width - cornerRadiusPx, y = 0f),
            strokeWidth = strokeWidthPx
        )

        drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(x = width - cornerRadiusPx * 2, y = 0f),
            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
            style = Stroke(width = strokeWidthPx)
        )

        drawLine(
            color = color,
            start = Offset(x = width, y = height),
            end = Offset(x = width, y = cornerRadiusPx),
            strokeWidth = strokeWidthPx
        )
    }
}

@Composable
fun Modifier.bottomBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp): Modifier {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }
    val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

    return this then Modifier.drawBehind {
        val width = size.width
        val height = size.height

        drawLine(
            color = color,
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = 0f, y = height - cornerRadiusPx),
            strokeWidth = strokeWidthPx
        )

        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(x = 0f, y = height - cornerRadiusPx * 2),
            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
            style = Stroke(width = strokeWidthPx)
        )

        drawLine(
            color = color,
            start = Offset(x = cornerRadiusPx, y = height),
            end = Offset(x = width - cornerRadiusPx, y = height),
            strokeWidth = strokeWidthPx
        )

        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(x = width - cornerRadiusPx * 2, y = height - cornerRadiusPx * 2),
            size = Size(cornerRadiusPx * 2, cornerRadiusPx * 2),
            style = Stroke(width = strokeWidthPx)
        )

        drawLine(
            color = color,
            start = Offset(x = width, y = 0f),
            end = Offset(x = width, y = height - cornerRadiusPx),
            strokeWidth = strokeWidthPx
        )
    }
}

@Composable
fun BoxAnim() {
    var middleBoxVisible by remember { mutableStateOf(true) }

    Column {
        BoxWithConstraints {
            val middleBoxWidth = if (middleBoxVisible) maxWidth / 3 else 0.dp
            val sideBoxWidth = (maxWidth - middleBoxWidth) / 2

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(sideBoxWidth)
                        .height(100.dp)
                        .background(Color.Red)
                )

                AnimatedVisibility(
                    visible = middleBoxVisible,
                    enter = slideInHorizontally() + expandHorizontally(),
                    exit = shrinkHorizontally() + slideOutHorizontally()
                ) {
                    Box(
                        modifier = Modifier
                            .width(middleBoxWidth)
                            .height(100.dp)
                            .background(Color.Green)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(sideBoxWidth)
                        .height(100.dp)
                        .background(Color.Blue)
                )
            }
        }

        Button(
            modifier = Modifier
                .width(200.dp)
                .height(80.dp),
            onClick = { middleBoxVisible = !middleBoxVisible }) {
            Text(text = "Toggle Middle Box")
        }
    }

}

@Composable
fun Modifier.circleLayout() =
    layout { measurable, constraints ->
        // Measure the composable
        val placeable = measurable.measure(constraints)

        //get the current max dimension to assign width=height
        val currentHeight = placeable.height
        val currentWidth = placeable.width
        val newDiameter = maxOf(currentHeight, currentWidth)

        //assign the dimension and the center position
        layout(newDiameter, newDiameter) {
            // Where the composable gets placed
            placeable.placeRelative(
                (newDiameter - currentWidth) / 2,
                (newDiameter - currentHeight) / 2
            )
        }
    }


@Composable
fun CircleRowWithTextAndImage() {

    Row(
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()

            .padding(PaddingValues(horizontal = 8.dp))
            .border(
                border = BorderStroke(2.dp, Color.Green),
                shape = RoundedCornerShape(25.dp)
            )
            .clip(RoundedCornerShape(25.dp))
            .background(Color.Blue),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Text(
            text = "E",
            textAlign = TextAlign.Center,
            color = Color.Red,
            modifier = Modifier
                .defaultMinSize(32.dp)
                .background(Color.Black, shape = CircleShape)
                .circleLayout()

        )
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Text(
            text = "Middle Text",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )

        Image(
            modifier = Modifier
                .size(32.dp)
                .padding(end = 16.dp),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "A call icon for calling"
        )


    }
}


@Composable
fun OverlappingBoxes(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val largeBox = measurables[0]
        val smallBox = measurables[1]
        val looseConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
        val largePlaceable = largeBox.measure(looseConstraints)
        val smallPlaceable = smallBox.measure(looseConstraints)
        layout(
            width = constraints.maxWidth,
            height = largePlaceable.height + smallPlaceable.height / 2,
        ) {
            largePlaceable.placeRelative(
                x = 0,
                y = 0,
            )
            val percentageFromTop = 0.3f // 30% from the top
            val yOffset = (largePlaceable.height * percentageFromTop).roundToInt()
            smallPlaceable.placeRelative(
                x = -smallPlaceable.width / 2,
                y = yOffset - smallPlaceable.height / 2
            )
        }
    }
}

@Composable
fun BoxOverlap(
    modifier: Modifier = Modifier,
) {
    OverlappingBoxes(modifier = modifier.size(300.dp)) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Green)
        ) {
        }

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Blue)
        ) {

        }
    }
}


@Preview()
@Composable
fun CircleRowWithTextAndImagePreview() {
    CircleRowWithTextAndImage()
}

@Composable
fun BoxAnim2(clicked: (Boolean) -> Unit) {
    var middleBoxVisible by remember { mutableStateOf(true) }

    var middleBoxText by remember { mutableStateOf("Hello, World! Raghunandan") } // Initial text

    val textStyle = LocalTextStyle.current

    val textMeasurer = rememberTextMeasurer()
    val textMeasured = remember(textStyle, textMeasurer) {
        textMeasurer.measure(
            text = middleBoxText,
            style = textStyle.copy(textAlign = TextAlign.Center)
        ).size.width
    }

    val newWidth = with(LocalDensity.current) { textMeasured.toDp() + 8.dp }

    Column {
        BoxWithConstraints(modifier = Modifier.clickable {
            clicked(true)
        }) {

            val middleBoxWidth = if (middleBoxVisible) newWidth else 0.dp
            val sideBoxWidth = ((maxWidth) - middleBoxWidth) / 2

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(sideBoxWidth)
                        .height(100.dp)
                        .background(Color.Red)
                )

                AnimatedVisibility(
                    visible = middleBoxVisible,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(100.dp)
                            .background(Color.Green)
                    ) {
                        Text(
                            maxLines = 1,
                            text = middleBoxText,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(sideBoxWidth)
                        .height(100.dp)
                        .background(Color.Blue)
                )
            }
        }

        Button(
            modifier = Modifier
                .width(200.dp)
                .height(80.dp),
            onClick = { middleBoxVisible = !middleBoxVisible }) {
            Text(text = "Toggle Middle Box")
        }
    }

}

@Composable
fun SOBlur() {

    var parent: Offset by remember { mutableStateOf(Offset.Zero) }
    var positionText by remember { mutableStateOf("") }
    var blur by remember { mutableStateOf(0.dp) }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().blur(blur).clickable {
            if (blur > 0.dp) {
                blur = 0.dp
            } else {
                blur = 4.dp
            }
        }) {


            Text(text = "Hi")
            Text(text = "Hi")
            Text(text = "Hi")
            Text(text = "Hi")
            Text(text = "Bye", modifier = Modifier.onGloballyPositioned {
                parent = Offset(
                    it.positionInParent().x,
                    it.positionInParent().y
                )
                positionText =
                    "positionInParent: $parent"
            })
            Text(text = "Hi")
            Text(text = "Hi")
            Text(text = "Hi")
            Text(text = "Hi")

            Text(text = positionText)
        }
            Box(Modifier.offset(x = parent.x.pxToDp(), y = parent.y.pxToDp())) {
                Text(
                    modifier = Modifier,
                    text = "Byeeeeeee",
                    color = Color.Green
                )

            }
    }
}

@Composable
fun Float.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }


