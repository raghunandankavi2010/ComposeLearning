package com.example.composelearning.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import com.example.composelearning.ui.theme.AppFontFamilyBold
import com.example.composelearning.ui.theme.AppFontFamilyMedium

data class ListData(val time: String, val type: String, val content: String, val id: Int) {

}


fun getListData(): MutableList<ListData> {
    val list = mutableListOf<ListData>()
    repeat(40) {
        list.add(
            ListData(
                "10:30am,15/09/2024",
                "Advanced Irrigation Management. Ensuring the soil remains consistently moist during the flowering stage is critical for maximizing crop yield. To prevent water stress, monitor the soil moisture levels daily and apply drip irrigation as needed. Proper hydration ensures that the plants remain healthy and productive throughout the growing season.",
                "Advanced Irrigation Management. Ensuring the soil remains consistently moist during the flowering stage is critical for maximizing crop yield. To prevent water stress, monitor the soil moisture levels daily and apply drip irrigation as needed. Proper hydration ensures that the plants remain healthy and productive throughout the growing season.",
                it
            )
        )
    }
    return list
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GeneralList(modifier: Modifier, lazyListState: LazyListState = rememberLazyListState()) {
    val list = getListData()
    val expand = remember { mutableStateMapOf<Int, Boolean>() }
    LazyColumn(
        modifier = modifier
            .semantics {
                testTagsAsResourceId = true // typically at the root ui element
            }
            .background(Color(0x29000000)),
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        state = lazyListState
    ) {

        items(list.size, key = { index -> list[index].id }) { index ->
            val isExpanded = expand[index] ?: false
            GeneralAlerts(alertsData =  list[index], expand = isExpanded, index = index,
                onExpandClicked = { index, isExpand  -> expand[index] = isExpand })
            Spacer(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun GeneralAlerts(
    alertsData: ListData,
    expand: Boolean,
    index: Int,
    onExpandClicked: (Int,Boolean) -> Unit
) {

    ElevatedCard(
        modifier = Modifier
            .testTag(
                "Alerts${alertsData.id}"
            )
            .fillMaxSize()
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        val fontFamily = AppFontFamilyMedium
        val fontFamilyBold = AppFontFamilyBold
        val fontFamilyNormal = AppFontFamilyMedium

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp)
                    .fillMaxWidth()
                    .height(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    maxLines = 1,
                    style = TextStyle(fontSize = 12.sp, fontFamily = fontFamily),
                    color = Color(0xFF777777),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .weight(2f),
                    text = alertsData.time
                )


                val drawable =
                    if (expand) {
                        R.drawable.icon_up
                    } else {
                        R.drawable.icon_down
                    }

                Image(
                    painter = painterResource(drawable),
                    contentDescription = "Arrow Down",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            onExpandClicked(index,!expand)
                        }
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontFamily = fontFamilyBold),
                text = alertsData.type
            )

            val modifier =
                if (expand ) {
                    Modifier.wrapContentHeight()
                } else {
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))
                    Modifier.height(0.dp)
                }

            AnimatedVisibility(
                visible = expand,
            ) {

                Text(
                    modifier = modifier
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    color = Color(0xFF777777),
                    style = TextStyle(fontSize = 14.sp, fontFamily = fontFamilyNormal),
                    text = alertsData.content
                )

            }

        }

    }
}

