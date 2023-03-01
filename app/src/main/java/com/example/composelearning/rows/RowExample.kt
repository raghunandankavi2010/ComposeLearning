package com.example.composelearning.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun MaxWidthText() {

    var item1WidthPx by remember { mutableStateOf(0) }
    var item2WidthPx by remember { mutableStateOf(0) }
    val maxItemWidth = max(item1WidthPx, item2WidthPx)
        .let { with(LocalDensity.current) { it.toDp() } }

    Row(modifier = Modifier.widthIn(min=300.dp).background(Color.Black), horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(
            text ="Smart Search",
            modifier = Modifier
                .onPlaced { item1WidthPx = it.size.width }
                .widthIn(min = maxItemWidth)
                .background(Color(0xFFFFB74D))
        )
        Text(
            text ="Character Search",
            modifier = Modifier
                .onPlaced { item2WidthPx = it.size.width }
                .widthIn(min = maxItemWidth)
                .background(Color(0xFFFFB74D))
        )
    }
}