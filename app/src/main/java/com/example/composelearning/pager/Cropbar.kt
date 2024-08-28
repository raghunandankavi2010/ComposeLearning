package com.example.composelearning.pager

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.LogCompositions
import com.example.composelearning.R
import com.example.composelearning.pager.TabRowDefaults.tabIndicatorOffset
import com.example.composelearning.sotry.CustomButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Preview
@Composable
private fun Test() {
//    CropBar() {
//
//    }
}

private val colors = listOf(
    Color.Yellow,
    Color.Red,
    Color.White,
    Color.Blue,
    Color.Magenta,
    Color.Yellow,
    Color.Red,
    Color.White,
    Color.Blue,
    Color.Magenta,
    Color.Magenta,
    Color.Yellow,
    Color.Red,
    Color.White,
    Color.Blue,
    Color.Magenta
)

//@Composable
//fun CropBar(
//    modifier: Modifier = Modifier,
//    pages: List<String>,
//    onCropClicked: (Int) -> Unit,
//    selectedIndex: Int,
//    tabSelected: (Int) -> Unit
//) {
//
//    val isSelected = { index: Int -> index == selectedIndex }
//    Column {
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//
//        val indicator = @Composable { tabPositions: List<TabPosition> ->
//            val color = colors[selectedIndex]
//            CustomIndicator(tabPositions = tabPositions, selectedIndex = selectedIndex, color)
//        }
//        MyScrollableTabRow(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(58.dp),
//            selectedTabIndex = 0,
//            backgroundColor = Color(0xFF03753C),
//            indicator = { tabPositions ->
////                TabRowDefaults.Indicator(
////                    color = Color.Black,
////                    modifier = Modifier
////                        .tabIndicatorOffset(tabPositions[selectedIndex])
////                        .fillMaxWidth()
////                )
//            },
//            minItemWidth = 58.dp,
//            edgePadding = 0.dp,
//            divider = {
//            },
//
//            ) {
//            pages.forEachIndexed { index, title ->
//
//                key(title) {
//                    Tab(
//                        modifier = Modifier
//                            .height(58.dp)
//                            .width(42.dp)
//                            .zIndex(2f),
//                        selected = isSelected(index),
//                        onClick = {
//                            tabSelected(index)
//                            onCropClicked(index)
//                        },
//                        interactionSource = NoRippleInteractionSource()
//                    ) {
//                        SampleImage()
//                    }
//                }
//            }
//
//        }
//    }
//}


//@Composable
//private fun CustomIndicator(tabPositions: List<TabPosition>, selectedIndex: Int, color: Color) {
//
//    val transition = updateTransition(selectedIndex, label = "transition")
//
//    val indicatorStart by transition.animateDp(
//        transitionSpec = {
//            tween(
//                durationMillis = 500,
//                easing = LinearOutSlowInEasing
//            )
//        },
//        label = ""
//    ) {
//        tabPositions[it].left
//    }
//
//    val indicatorEnd by transition.animateDp(
//        transitionSpec = {
//            tween(
//                durationMillis = 500,
//                easing = LinearOutSlowInEasing
//            )
//        },
//        label = "",
//    ) {
//        tabPositions[it].right
//    }
//    Box(
//        Modifier
//            .offset(x = indicatorStart)
//            .wrapContentSize(align = Alignment.BottomStart)
//            .width(indicatorEnd - indicatorStart)
//            .paint(
//                // Replace with your image id
//                painterResource(id = R.drawable.ic_launcher_background), // some background vector drawable image
//                contentScale = ContentScale.FillWidth,
//                colorFilter = ColorFilter.tint(color) // for tinting
//            )
//            .zIndex(1f)
//    )
//}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SampleImage() {
//
//    BoxWithConstraints(
//        modifier = Modifier.wrapContentSize(),
//    ) {

    LogCompositions(tag = "CropBar", msg = "Test" )
    Image(
        modifier = Modifier
            .width(42.dp)
            .height(42.dp)
            .clip(CircleShape),
        painter = painterResource(id = R.drawable.ic_launcher_background),
        contentDescription = "Image"
    )
    // }
}

class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

//        Spacer(modifier = Modifier.height(20.dp))

//        MyScrollableTabRow(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(58.dp),
//            selectedTabIndex = selectedIndex,
//            backgroundColor = Color(0xFF03753C),
//            indicator = indicator,
//            minItemWidth = 42.dp,
//            edgePadding = 16.dp,
//            divider = {
//            },
//
//            ) {
//            pages.forEachIndexed { index, title ->
//
//                Tab(
//                    modifier = Modifier
//                        .height(58.dp)
//                        .width(42.dp)
//                        .zIndex(2f),
//                    selected = selectedIndex == index,
//                    onClick = {
//                        selectedIndex = index
//                        onCropClicked(index)
//                    },
//                    interactionSource = NoRippleInteractionSource()
//                ) {
//
//                    SampleImage(selectedIndex)
//                }
//            }
//        }

@Composable
fun TabScreen() {
    var tabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "kotlin",
        "java",
        "c#",
        "php",
        "golang",
        "A",
        "B",
        "C",
        "kotlin",
        "java",
        "c#",
        "php",
        "golang",
        "A",
        "B",
        "C"
    )
    //val tabs = listOf("Home", "About", "Settings", "More", "Something", "Everything")

    Column(modifier = Modifier.fillMaxWidth()) {
        MyScrollableTabRow(selectedTabIndex = tabIndex,
            divider = {

            },
            indicator = { tabPositions ->

                Image(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[tabIndex])
                        .width(42.dp)
                        .height(42.dp)
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Image"
                )
            }) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { SampleImage() },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                )
            }
        }
        when (tabIndex) {
            0 -> CustomButton(onClick = {  }) {

            }

            1 -> CustomButton(onClick = {  }) {

            }

            2 -> CustomButton(onClick = {  }) {

            }

            3 -> CustomButton(onClick = {  }) {

            }

            4 -> CustomButton(onClick = {  }) {

            }

            5 -> CustomButton(onClick = {  }) {

            }
        }
    }
}