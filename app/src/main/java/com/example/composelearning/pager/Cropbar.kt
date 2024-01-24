package com.example.composelearning.pager

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.composelearning.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview
@Composable
private fun Test() {
    CropBar() {

    }
}

@Composable
fun CropBar(onCropClicked: (Int) -> Unit) {
    Column {

        Spacer(modifier = Modifier.height(20.dp))
        var selectedIndex by remember { mutableStateOf(0) }
        val pages = listOf("kotlin", "java", "c#", "php", "golang", "A", "B", "C")
        val colors = listOf(Color.Yellow, Color.Red, Color.White, Color.Blue, Color.Magenta)

        val indicator = @Composable { tabPositions: List<TabPosition> ->
            val color = when (selectedIndex) {
                0 -> colors[0]
                1 -> colors[1]
                2 -> colors[2]
                3 -> colors[3]
                else -> colors[4]
            }
            CustomIndicator(tabPositions = tabPositions, selectedIndex = selectedIndex, color)
        }
        MyScrollableTabRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            selectedTabIndex = selectedIndex,
            backgroundColor = Color(0xFF03753C),
            indicator = indicator,
            minItemWidth = 100.dp,
            edgePadding = 0.dp,
            divider = {
            },

            ) {
            pages.forEachIndexed { index, title ->

                Tab(
                    modifier = Modifier
                        .height(58.dp)
                        .width(74.dp)
                        .zIndex(2f),
                    selected = selectedIndex == index,
                    onClick = {
                        selectedIndex = index
                        onCropClicked(index)
                    },
                    interactionSource = NoRippleInteractionSource()
                ) {

                    SampleImage(selectedIndex)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        MyScrollableTabRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            selectedTabIndex = selectedIndex,
            backgroundColor = Color(0xFF03753C),
            indicator = indicator,
            minItemWidth = 0.dp,
            edgePadding = 0.dp,
            divider = {
            },

            ) {
            pages.forEachIndexed { index, title ->

                Tab(
                    modifier = Modifier
                        .height(58.dp)
                        .width(74.dp)
                        .zIndex(2f),
                    selected = selectedIndex == index,
                    onClick = {
                        selectedIndex = index
                        onCropClicked(index)
                    },
                    interactionSource = NoRippleInteractionSource()
                ) {

                    SampleImage(selectedIndex)
                }
            }
        }
    }
}


@Composable
private fun CustomIndicator(tabPositions: List<TabPosition>, selectedIndex: Int, color: Color) {

    val transition = updateTransition(selectedIndex, label = "transition")

    val indicatorStart by transition.animateDp(
        transitionSpec = {
            tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            )
        },
        label = ""
    ) {
        tabPositions[it].left
    }

    val indicatorEnd by transition.animateDp(
        transitionSpec = {
            tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            )
        },
        label = "",
    ) {
        tabPositions[it].right
    }
    Box(
        Modifier
            .padding(top = 8.dp)
            .offset(x = indicatorStart)
            .wrapContentSize(align = Alignment.BottomStart)
            .width(indicatorEnd - indicatorStart)
            .paint(
                // Replace with your image id
                painterResource(id = R.drawable.ic_launcher_background), // some background vector drawable image
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(color) // for tinting
            )
            .zIndex(1f)
    )
}

@Composable
fun SampleImage(selectedIndex: Int) {

    BoxWithConstraints(
        modifier = Modifier,
    ) {
        Image(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(42.dp)
                .height(42.dp)
                .align(Alignment.BottomCenter),
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Image"
        )

        if(selectedIndex == 1) {
            Text(
                text = "180 Days",
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 18.dp)
                    .width(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Gray)
                    .graphicsLayer {
                        translationX = 5f
                    }
            )
        }
    }
}

class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}