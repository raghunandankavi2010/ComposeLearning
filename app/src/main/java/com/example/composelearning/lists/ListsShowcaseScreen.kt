package com.example.composelearning.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composelearning.sotry.SwipeToCancel

private val TABS = listOf(
    "Alerts",
    "Products",
    "Sticky",
    "Reorder",
    "Swipe",
    "Staggered",
    "News",
    "Calendar",
    "Circular ↕",
    "Circular ↔",
    "LazyRow",
    "Picker",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsShowcaseScreen() {
    var selected by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        PrimaryScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp) {
            TABS.forEachIndexed { index, label ->
                Tab(
                    selected = index == selected,
                    onClick = { selected = index },
                    text = { Text(label) },
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (selected) {
                0 -> GeneralAlertsList(modifier = Modifier.fillMaxSize())
                1 -> ProductListScreen()
                2 -> StickyList()
                3 -> ReOrderList()
                4 -> SwipeToCancel(closeScreen = {}, isExpandedScreen = false)
                5 -> Test2()
                6 -> JioNews()
                7 -> CalendarLazyRow()
                8 -> PreviewCircularListVertical()
                9 -> PreviewCircularList()
                10 -> LazyRowLikePager()
                11 -> NumberPicker()
            }
        }
    }
}