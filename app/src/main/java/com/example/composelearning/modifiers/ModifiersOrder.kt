package com.example.composelearning.modifiers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.R

@Composable
fun AvatarPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .background(Color.Black)
        )
    }
}

@Preview
@Composable
fun AvatarPreview2() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .border(2.dp, Color.Black)
                .background(Color.Black)
        )
    }
}

@Preview
@Composable
fun AvatarPreview3() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .padding(20.dp)
                .size(70.dp)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun AvatarPreview4() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize()
                .background(Color.Black)
        )
    }
}

@Preview
@Composable
fun AvatarPreview5() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .padding(20.dp)
                .align(Alignment.BottomEnd)
        )
    }
}


@Preview
@Composable
fun AvatarPreview6() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .offset(20.dp,20.dp)

        )
    }
}


@Preview
@Composable
fun AvatarPreview7() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .offset(20.dp,20.dp)
                .fillMaxSize()
                .background(Color.Blue)

        )
    }
}

@Preview
@Composable
fun AvatarPreview8() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .padding(10.dp)
                .background(Color.Blue)
                .padding(20.dp)

        )
    }
}

@Preview
@Composable
fun AvatarPreview9() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .border(5.dp, Color.Black)
                .clip(CircleShape)
                .background(Color.Blue)

        )
    }
}


@Preview
@Composable
fun AvatarPreview10() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .background(Color.Blue)
                .padding(10.dp)
                .padding(20.dp)

        )
    }
}

@Preview
@Composable
fun AvatarPreview11() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.LightGray)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .border(3.dp, Color.Blue)
                .border(3.dp, Color.Black)
                .clip(CircleShape)


        )
    }
}