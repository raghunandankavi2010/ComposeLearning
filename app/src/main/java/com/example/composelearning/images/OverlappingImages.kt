package com.example.composelearning.images

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.example.composelearning.R
import com.example.composelearning.customshapes.dpToPx

/**
 *  Credits to https://proandroiddev.com/custom-layouts-with-jetpack-compose-bc1bdf10f5fd
 */

fun overlappingRowMeasurePolicy(overlapFactor: Float) = MeasurePolicy { measurables, constraints ->
    val placeables = measurables.map { measurable -> measurable.measure(constraints) }
    val height = placeables.maxOf { it.height }
    val width = (placeables.subList(1, placeables.size)
        .sumOf { it.width } * overlapFactor + placeables[0].width).toInt()
    layout(width, height) {
        var xPos = 0
        for (placeable in placeables) {
            placeable.placeRelative(xPos, 0, 0f)
            xPos += (placeable.width * overlapFactor).toInt()
        }
    }
}

@Composable
fun OverlappingRow(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.1, to = 1.0) overlapFactor: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    val measurePolicy = overlappingRowMeasurePolicy(overlapFactor)
    Layout(
        measurePolicy = measurePolicy,
        content = content,
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 66, heightDp = 66, backgroundColor = 0xFFFFFF)
@Composable
fun CropImage() {
    BoxWithConstraints(
        modifier = Modifier
            .width(66.dp)
            .height(66.00014.dp)
    ) {
        val maxWidthParent = constraints.maxWidth
        Box(
            modifier = Modifier
                .width(maxWidth)
                .height(maxHeight)
                .border(
                    width = 2.dp,
                    color = Color(0xFF03753C),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(size = 16.dp))
        )
        {
            Image(
               modifier =  Modifier.size(66.dp),
                painter = painterResource(id = R.drawable.tomato),
                contentDescription = "image description",
                contentScale = ContentScale.Crop
            )
        }

        val offSetX = maxWidthParent - 24.dp.dpToPx()

        Image(
            modifier = Modifier
                .graphicsLayer {
                    translationX = offSetX
                }
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White,CircleShape),
            painter = painterResource(R.drawable.ic_remove),
            contentDescription = stringResource(id = R.string.remove)
        )
    }
}
