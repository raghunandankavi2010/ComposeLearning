package com.example.composelearning.sotry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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

fun getTracksLists() : List<Track> {

    val list = ArrayList<Track>()
    repeat(40) {
        list.add(Track(false,"name$it"))
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
                    .clickable {  }
                    .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            )


            Column(modifier= Modifier.wrapContentHeight().constrainAs(columnTexts) {
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom, margin = 16.dp,)
            }) {
                Text("Text1", modifier = Modifier.wrapContentWidth())
                Text("Text2", modifier = Modifier.wrapContentWidth())

            }
    }
}
