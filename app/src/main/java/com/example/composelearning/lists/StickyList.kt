package com.example.composelearning.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Sample data
data class Category(val title: String, val items: List<String>)

val categories = listOf(
    Category("Category 1", listOf("Item 1", "Item 2", "Item 3")),
    Category("Category 2", listOf("Item 4", "Item 5")),
    Category("Category 3", listOf("Item 6", "Item 7", "Item 8")),
    Category("Category 4", listOf("Item 6", "Item 7", "Item 8")),
    Category("Category 5", listOf("Item 6", "Item 7", "Item 8")),
    Category("Category 6", listOf("Item 6", "Item 7", "Item 8")),
    Category("Category 7", listOf("Item 6", "Item 7", "Item 8")),
    Category("Category 8", listOf("Item 6", "Item 7", "Item 8"))



)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun App() {
    val listState = rememberLazyListState()
    val topHeaderIndex by remember {
        derivedStateOf {
            val firstVisibleItem = listState.firstVisibleItemIndex
            val headerHeight = 56.dp // Adjust based on your header height
            if (listState.firstVisibleItemScrollOffset.dp < headerHeight) firstVisibleItem else firstVisibleItem + 1
        }
    }
    val scale = remember { mutableStateOf(1f) }


    LazyColumn(
        state = listState,
    ) {

        categories.forEach { (category, models) ->
            stickyHeader {
                StickyHeader(
                    category = category,
                    isTopHeader = category == categories[topHeaderIndex].title,
                    scale = scale.value
                )
            }

            items(models) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

    }
}

@Composable
fun StickyHeader(category: String, isTopHeader: Boolean, scale: Float) {
    val color = if (isTopHeader) Color.LightGray else Color.White
    val cardColors = CardDefaults.cardColors(containerColor = color)
    val elevationDp = if (isTopHeader) 4.dp else 0.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Adjust based on your header height
            .clickable { /* Handle header click */ },
        shape = RoundedCornerShape(4.dp),
        colors = cardColors
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                modifier = Modifier.scale(scale)
            )
        }
    }
}
