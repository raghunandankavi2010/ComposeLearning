package com.example.composelearning.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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

private val TABS = listOf("3D", "Instagram", "Instagram v2", "Pager", "Pager v2")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagerShowcaseScreen() {
    var selected by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
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
                0 -> ThreeDCarousel(modifier = Modifier.fillMaxSize())
                1 -> InstagramCarouselPreview()
                2 -> InstagramCarouselPreview2()
                3 -> PagerDemo(modifier = Modifier.fillMaxSize())
                4 -> Pager2()
            }
        }
    }
}