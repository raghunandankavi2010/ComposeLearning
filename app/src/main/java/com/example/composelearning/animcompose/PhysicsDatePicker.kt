package com.example.composelearning.animcompose

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Immutable
data class DatePickerItem(
    val date: LocalDate,
    val dayOfWeek: String,
    val dayOfMonth: String,
    val month: String
)

@Stable
class PhysicsDatePickerState(initialDate: LocalDate) {
    var selectedDate by mutableStateOf(initialDate)
}

@Composable
fun PhysicsDatePicker(
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    val pickerState = remember { PhysicsDatePickerState(today) }
    
    val dateItems = remember {
        val formatterEEE = DateTimeFormatter.ofPattern("EEE")
        val formatterMMM = DateTimeFormatter.ofPattern("MMM")
        (-365..365).map { offset ->
            val d = today.plusDays(offset.toLong())
            DatePickerItem(
                date = d,
                dayOfWeek = d.format(formatterEEE).uppercase(),
                dayOfMonth = d.dayOfMonth.toString(),
                month = d.format(formatterMMM)
            )
        }
    }
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 365)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val configuration = LocalConfiguration.current
    val horizontalPaddingValues = remember(configuration.screenWidthDp) {
        val screenWidth = configuration.screenWidthDp.dp
        val itemWidth = 80.dp
        PaddingValues(horizontal = (screenWidth - itemWidth) / 2)
    }
    
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { _ ->
                val layoutInfo = listState.layoutInfo
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull { 
                    abs((it.offset + it.size / 2) - center) 
                }
                closestItem?.let {
                    val newDate = dateItems[it.index].date
                    if (newDate != pickerState.selectedDate) {
                        pickerState.selectedDate = newDate
                        currentOnDateSelected(newDate)
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
        DatePickerHeader(dateProvider = { pickerState.selectedDate })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            SelectionIndicator()

            LazyRow(
                state = listState,
                flingBehavior = snapFlingBehavior,
                modifier = Modifier.fillMaxSize(),
                contentPadding = horizontalPaddingValues,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(
                    items = dateItems, 
                    key = { _, item -> item.date.toEpochDay() }
                ) { index, item ->
                    DateItem(
                        item = item,
                        listState = listState,
                        index = index
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerHeader(dateProvider: () -> LocalDate) {
    val date = dateProvider()
    Text(
        text = date.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        style = TextStyle(
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun SelectionIndicator() {
    Surface(
        modifier = Modifier.size(width = 74.dp, height = 110.dp),
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
            )
        )
    ) {}
}

@Composable
private fun DateItem(
    item: DatePickerItem,
    listState: LazyListState,
    index: Int
) {
    val normalizedDistanceState = remember(listState, index) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }
            if (visibleItem != null) {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val itemCenter = visibleItem.offset + visibleItem.size / 2
                val distance = abs(viewportCenter - itemCenter).toFloat()
                (distance / 200f).coerceIn(0f, 1f)
            } else 1f
        }
    }

    val isSelected by remember {
        derivedStateOf { normalizedDistanceState.value < 0.2f }
    }

    DateItemContent(
        item = item,
        isSelectedProvider = { isSelected },
        distanceProvider = { normalizedDistanceState.value }
    )
}

@Composable
private fun DateItemContent(
    item: DatePickerItem,
    isSelectedProvider: () -> Boolean,
    distanceProvider: () -> Float
) {
    val isSelected = isSelectedProvider()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .graphicsLayer {
                val dist = distanceProvider()
                val scale = 1.3f - (dist * 0.4f)
                scaleX = scale
                scaleY = scale
                alpha = 1f - (dist * 0.7f)
            }
    ) {
        Text(
            text = item.dayOfWeek,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.6f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.dayOfMonth,
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.month,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f)
            )
        )
    }
}
