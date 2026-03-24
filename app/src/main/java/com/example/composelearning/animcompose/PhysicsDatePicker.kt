package com.example.composelearning.animcompose

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun PhysicsDatePicker(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    // Generate dates: 1 year before and after today
    val today = remember { LocalDate.now() }
    val dates = remember {
        (-365..365).map { offset -> today.plusDays(offset.toLong()) }
    }
    
    val initialIndex = 365 // Today's index
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemWidth = 80.dp
    
    // Calculate content padding to center the items
    val horizontalPadding = (screenWidth - itemWidth) / 2
    
    // Track the selected date based on center position
    var selectedDate by remember { mutableStateOf(today) }
    
    // Efficiently track the center item
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { _ ->
                val layoutInfo = listState.layoutInfo
                val center = layoutInfo.viewportEndOffset / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                    abs((it.offset + it.size / 2) - center) 
                }
                closestItem?.let {
                    val newDate = dates[it.index]
                    if (newDate != selectedDate) {
                        selectedDate = newDate
                        onDateSelected(newDate)
                    }
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selected Month/Year Header
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // Static Selection Indicator
            Surface(
                modifier = Modifier
                    .size(width = 74.dp, height = 110.dp),
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            ) {}

            // The Scrolling Dates
            LazyRow(
                state = listState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(dates) { index, date ->
                    DateItem(
                        date = date,
                        listState = listState,
                        index = index
                    )
                }
            }
        }
    }
}

@Composable
private fun DateItem(
    date: LocalDate,
    listState: LazyListState,
    index: Int
) {
    // Distance from the center of the viewport
    val distanceFromCenter = remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }
            if (visibleItem != null) {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val itemCenter = visibleItem.offset + visibleItem.size / 2
                abs(viewportCenter - itemCenter).toFloat()
            } else {
                1000f // Far away if not visible
            }
        }
    }

    // Normalized distance
    val maxDistance = 200f 
    val normalizedDistance = (distanceFromCenter.value / maxDistance).coerceIn(0f, 1f)
    
    // Interpolation for scaling and alpha
    val scale = 1.3f - (normalizedDistance * 0.4f)
    val alpha = 1f - (normalizedDistance * 0.7f)
    val isSelected = normalizedDistance < 0.2f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .scale(scale)
            .alpha(alpha)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = date.dayOfMonth.toString(),
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = date.format(DateTimeFormatter.ofPattern("MMM")),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f)
            )
        )
    }
}
