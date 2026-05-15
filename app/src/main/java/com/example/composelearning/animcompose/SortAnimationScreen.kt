package com.example.composelearning.animcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private data class SortTab(
    val label: String,
    val content: @Composable () -> Unit,
)

/**
 * Tabbed hub for every sort animation. Hosts the status-bar inset itself and asks each visualizer
 * to skip its own top-inset (the overlay still aligns to the top of the visualizer Box, which now
 * sits below the tab bar).
 */
@Composable
fun SortAnimationScreen(modifier: Modifier = Modifier) {
    val tabs = remember {
        listOf(
            SortTab("Bubble") { BubbleSortRainbow(applyOwnInsets = false) },
            SortTab("Quick") { QuickSortRainbow(applyOwnInsets = false) },
            SortTab("Insertion") { InsertionSortRainbow(applyOwnInsets = false) },
            SortTab("Selection") { SelectionSortRainbow(applyOwnInsets = false) },
            SortTab("Shell") { ShellSortRainbow(applyOwnInsets = false) },
            SortTab("Merge") { MergeSortRainbow(applyOwnInsets = false) },
            SortTab("Heap") { HeapSortRainbow(applyOwnInsets = false) },
            SortTab("Timsort") { TimSortRainbow(applyOwnInsets = false) },
        )
    }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding(),
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = Color.White,
            edgePadding = 0.dp,
        ) {
            tabs.forEachIndexed { i, tab ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            tabs[selectedTab].content()
        }
    }
}