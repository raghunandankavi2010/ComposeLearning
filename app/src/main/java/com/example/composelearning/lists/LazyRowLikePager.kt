package com.example.composelearning.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun LazyRowLikePager() {

    BoxWithConstraints {
        val boxWithConstraintsScope = this
        val itemWidth = boxWithConstraintsScope.maxWidth * 0.8f // 80% of screen width
        val listState = rememberLazyListState()


        val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState) }
        val flingBehavior = rememberSnapFlingBehavior(snappingLayout)


        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp),
            state = listState,
            flingBehavior = flingBehavior
        ) {
            items(200) { index ->
                Box(
                    modifier =
                    Modifier
                        .width(itemWidth)
                        .height(400.dp)
                        .background(Color.Gray)

                ) {
                    Text(
                        text = index.toString(),
                        fontSize = 32.sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

