package com.example.composelearning.ipodwheel.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.ipodwheel.domain.model.Song
import kotlin.math.atan2

private val ItemHeight = 46.dp
private const val TWO_PI = (2.0 * Math.PI).toFloat()
private const val PI_F = Math.PI.toFloat()
// One list step per this much wheel rotation (≈ 36° → ten items per full turn).
private val ANGLE_PER_ITEM = PI_F / 5f

@Composable
fun IpodScreen(
    viewModel: IpodViewModel = viewModel(factory = IpodViewModel.Factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFEDEDED)).systemBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Ipod(
                songs = state.songs,
                nowPlaying = state.nowPlaying,
                onSelect = { viewModel.onIntent(IpodIntent.Select(it)) },
            )
        }
    }
}

@Composable
private fun Ipod(songs: List<Song>, nowPlaying: Int?, onSelect: (Int) -> Unit) {
    // Accumulated wheel rotation (radians). Highlight index is derived from it.
    var rotation by remember { mutableFloatStateOf(0f) }
    val highlight = (rotation / ANGLE_PER_ITEM).toInt().coerceIn(0, songs.lastIndex)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White),
        ) {
            SongList(songs, highlight, nowPlaying)
            Text(
                "iPod",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFB0B0B0))
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                fontSize = 14.sp,
            )
        }

        Box(Modifier.height(28.dp))

        ClickWheel(
            onRotate = { delta -> rotation = (rotation + delta).coerceAtLeast(0f) },
            onCenter = { onSelect(highlight) },
        )
    }
}

@Composable
private fun SongList(songs: List<Song>, highlight: Int, nowPlaying: Int?) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(top = 28.dp)) {
        // Slide the column so the highlighted row centers in the viewport.
        val centerOffset = maxHeight / 2 - ItemHeight / 2
        val target by animateDpAsState(centerOffset - ItemHeight * highlight, label = "scroll")
        Column(Modifier.fillMaxWidth().offset(y = target)) {
            songs.forEachIndexed { index, song ->
                SongRow(song, active = index == highlight, playing = index == nowPlaying)
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, active: Boolean, playing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ItemHeight)
            .background(if (active) Color(0xFF2980B9) else Color.Transparent)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column {
            Text(
                (if (playing) "▶ " else "") + song.title,
                color = if (active) Color.White else Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist,
                color = if (active) Color.White.copy(alpha = 0.85f) else Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The rotary click wheel: dragging a finger around the ring produces an angular delta each
 * frame (with wrap-around handled), which is reported via [onRotate]. The centre button
 * fires [onCenter].
 */
@Composable
private fun ClickWheel(onRotate: (Float) -> Unit, onCenter: () -> Unit) {
    var prevAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(CircleShape)
            .background(Color(0xFF323232))
            .pointerInput(Unit) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                detectDragGestures(
                    onDragStart = { offset ->
                        prevAngle = atan2(offset.y - cy, offset.x - cx)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val a = atan2(change.position.y - cy, change.position.x - cx)
                        var d = a - prevAngle
                        if (d > PI_F) d -= TWO_PI
                        if (d < -PI_F) d += TWO_PI
                        prevAngle = a
                        onRotate(d)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("MENU", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 22.dp))
        Text("⏮", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 26.dp))
        Text("⏭", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 26.dp))
        Text("⏯", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp))

        // Centre button (select).
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .clickable { onCenter() },
        )
    }
}
