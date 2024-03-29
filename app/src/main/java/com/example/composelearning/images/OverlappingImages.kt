package com.example.composelearning.images

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import com.example.composelearning.R
import com.example.composelearning.customshapes.dpToPx
import com.example.composelearning.ui.theme.ComposeLearningTheme

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
        measurePolicy = measurePolicy, content = content, modifier = modifier
    )
}

@Composable
fun CropImage(
    icon: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        val imageSize = 66.dp
        Image(
            modifier = Modifier
                .size(imageSize)
                .background(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF03753C),
                    shape = RoundedCornerShape(size = 16.dp)
                ),
            painter = painterResource(id = R.drawable.tomato),
            contentDescription = "image description",
            contentScale = ContentScale.Crop
        )
        val offSetX = 66.dp.dpToPx() - 24.dp.dpToPx()

        Image(modifier = Modifier
            .graphicsLayer {
                translationX = offSetX
            }
            .size(24.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable {
               onClick()
            },
            painter = painterResource(icon),
            contentDescription = stringResource(id = R.string.remove)
        )
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100, backgroundColor = 0xFFFFFF)
@Composable
private fun BannerPreview() {
    ComposeLearningTheme {
        Surface {
            CropImage(icon = R.drawable.ic_select) {

            }
        }
    }
}
