package com.example.composelearning.sotry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


@Composable
fun SOTry() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(

            "John Doe John Doe John Doe",
            modifier = Modifier.weight(1.0f, fill = true).background(Color.Green)
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

