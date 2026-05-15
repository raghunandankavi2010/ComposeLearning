package com.example.composelearning.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.composelearning.lists.InstagramCarouselPreview
import com.example.composelearning.lists.InstagramCarouselPreview2

private val TABS = listOf("Instagram", "Instagram v2", "Pager", "Pager v2")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagerShowcaseScreen() {
    var selected by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        PrimaryTabRow(selectedTabIndex = selected) {
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
                0 -> InstagramCarouselPreview()
                1 -> InstagramCarouselPreview2()
                2 -> PagerDemo(modifier = Modifier.fillMaxSize())
                3 -> Pager2()
            }
        }
    }
}