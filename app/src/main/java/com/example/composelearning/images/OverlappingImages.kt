package com.example.composelearning.images

import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
    currentIndex: Int = -1,
    isRemoveIconShow: Boolean = false,
    cropId: Int,
    selected: Boolean,
    cropImage: Int,
    onClick: ((Boolean, Int) -> Unit)? = null,
    onRemove: ((Int, Int) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
    ) {
        val imageSize = 66.dp
        val modifier = if (selected || isRemoveIconShow) {
            Modifier.border(
                width = 2.dp,
                color = Color(0xFF03753C),
                shape = RoundedCornerShape(size = 16.dp)

            )
        } else {
            Modifier.border(
                width = 2.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(size = 16.dp)
            )
        }

        val iconModifier = if (isRemoveIconShow) {
            Modifier
                .clickable {
                    if (onRemove != null && currentIndex != -1)
                        onRemove(cropId, currentIndex)
                }
        } else {
            Modifier
        }
        Image(
            modifier = Modifier
                .size(imageSize)
                .background(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .clip(RoundedCornerShape(size = 16.dp)) //
                .then(modifier)
                .clickable {
                    if (onClick != null)
                        onClick(!selected, cropId)
                },
            painter = painterResource(id = cropImage),
            contentDescription = "image description",
            contentScale = ContentScale.Crop
        )
        //  val offSetX = 66.dp.dpToPx() - 24.dp.dpToPx()

        if (isRemoveIconShow) {
            Image(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .then(iconModifier),
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = stringResource(id = R.string.remove)
            )
        } else if (selected) {
            Image(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                painter = painterResource(R.drawable.ic_select),
                contentDescription = stringResource(id = R.string.selected)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 100, backgroundColor = 0xFFFFFF)
@Composable
private fun BannerPreview() {
    ComposeLearningTheme {
        Surface {
            CropImage(
                currentIndex = -1,
                isRemoveIconShow = true,
                cropId = 0,
                selected = true,
                cropImage = R.drawable.ic_remove,
                { selected, index ->

                },
                { id, index ->
                })
        }
    }
}


@Composable
fun ImageWithAction(
    currentIndex: Int = -1,
    isRemoveIconShow: Boolean = false,
    cropId: Int,
    selected: Boolean = false,
    cropImage: Int,
    onClick: ((Boolean, Int) -> Unit)? = null,
    onRemove: ((Int, Int) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    val vector = if (isRemoveIconShow) {
        ImageVector.vectorResource(id = R.drawable.ic_remove_border)
    } else {
        ImageVector.vectorResource(id = R.drawable.ic_select_border)
    }
    val painter = rememberVectorPainter(image = vector)

    val context = LocalContext.current

    val selectUnselect = remember { mutableStateOf(false) }

    // When the user taps on the Canvas, you can
    // check if the tap offset is in one of the
    // tracked Rects.
    val vectorImageBounds = rememberImageBounds()

    val borderModifier = if (selectUnselect.value || isRemoveIconShow) {
        Modifier.border(
            width = 2.dp,
            color = Color(0xFF03753C),
            shape = RoundedCornerShape(size = 16.dp)
        )
    } else {
        Modifier
    }

    Image(
        painter = painterResource(cropImage),
        contentDescription = "Tomato",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(66.dp)
            .aspectRatio(1f)
            .drawWithContent {
                // draw the content
                drawContent()
                // draw the action icon over the content
                if (isRemoveIconShow || selectUnselect.value) {
                    translate(left = size.width - 24.dp.toPx(), top = 0.dp.toPx()) {
                        with(painter) {
                            draw(
                                size = Size(24.dp.toPx(), 24.dp.toPx())
                            )
                        }
                    }
                }
            }
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .clip(RoundedCornerShape(size = 16.dp))
            .then(borderModifier)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        // ... wait for release
                        interactionSource.emit(PressInteraction.Release(press))

                        // action icon clicked
                        if (vectorImageBounds.contains(offset) && isRemoveIconShow) {
                            Toast
                                .makeText(context, "Remove clicked", Toast.LENGTH_SHORT)
                                .show()
                            if (onRemove != null && currentIndex != -1)
                                onRemove(cropId, currentIndex)
                        } else if (onClick != null) { // crop clicked
                            selectUnselect.value = !selectUnselect.value
                            Toast
                                .makeText(context, "${selectUnselect.value}", Toast.LENGTH_SHORT)
                                .show()
                            onClick(selectUnselect.value, cropId)
                        }
                    }
                )
            }
            .indication(
                interactionSource,
                rememberRipple(color = Color(0xFF00796B))
            )
    )
}


@Composable
fun rememberImageBounds(): Rect {
    val offset = 24.dp.dpToPx()
    val imageSize = 66.dp.dpToPx()
    val topOffset = 0.dp.dpToPx()
    return remember {
        Rect(
            left = imageSize - offset,
            top = topOffset,
            right = imageSize,
            bottom = offset
        )
    }
}



