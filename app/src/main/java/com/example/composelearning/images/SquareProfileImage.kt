package com.example.composelearning.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
fun SquareProfileImage(
    modifier: Modifier,
    drawable: Int,
    radii: Dp,
) {
    Image(
        painter = painterResource(id = drawable),
        contentDescription = null,
        modifier = modifier
            .clip(RoundedCornerShape(radii)),
    )
}