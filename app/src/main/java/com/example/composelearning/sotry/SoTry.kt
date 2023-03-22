package com.example.composelearning.sotry

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.composelearning.R


@Composable
fun SOTry() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "John Doe John Doe John Doe",
            modifier = Modifier
                .weight(1.0f, fill = true)
                .background(Color.Green)
        )
        Icon(
            imageVector = Icons.Default.Done,
            contentDescription = "",
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
        modifier = modifier.wrapContentWidth().wrapContentHeight().padding(10.dp),
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
        modifier =  Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
    modifier: Modifier = Modifier.width(300.dp).height(300.dp),
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current
) {

    val mergedStyle = style.merge(
        TextStyle(
            color = color,
            fontSize = 40.sp,
            fontWeight = fontWeight,
            textAlign = textAlign,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
        )
    )

    val measurer = rememberTextMeasurer()

    val result = measurer.measure(
        AnnotatedString( number.toString()),
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