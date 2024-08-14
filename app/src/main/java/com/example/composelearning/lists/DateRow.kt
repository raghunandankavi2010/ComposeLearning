package com.example.composelearning.lists

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composelearning.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarLazyRow() {
    val calendarEndDate: LocalDate = LocalDate.now()
    val calendarStartDate: LocalDate = LocalDate.now().minusYears(1)
    val initialDates = getDatesBetween(calendarStartDate, calendarEndDate)

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM")
    val dateFormatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("d")
    val lazyListState = rememberLazyListState()
    var selectedDate by remember {
        mutableIntStateOf(0)
    }
    val isSelected = { index: Int -> index == selectedDate }

    LazyRow(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentPadding = PaddingValues(start = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(initialDates, key = { _, date -> date.date.dayOfMonth }) { index, date ->
            DateCard(index, isSelected(index), dateFormatter.format(date.date),dateFormatter2.format(date.date)) {
                selectedDate = it
            }
        }
    }

    // Launch an effect to scroll to the current date
//    LaunchedEffect(Unit) {
//        val currentIndex = dates.indexOf { it.date = LocalDate.now()}
//        val approximateVisibleItems = (screenWidth / itemWidthWithSpacing).toInt()
//        val scrollToIndex = maxOf(0, currentIndex - approximateVisibleItems / 2)
//        lazyListState.scrollToItem(scrollToIndex)
//    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateCard(
    index: Int,
    isSelected: Boolean,
    date: String,
    day: String,
    dateSelected: (Int) -> Unit
) {
    Card(
        modifier =
        Modifier
            .width(54.dp)
            .height(72.dp)
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(size = 8.dp))
            .clickable {
                dateSelected(index)
            }
    ) {


        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = date,
                    modifier = Modifier
                        .width(54.dp)
                        .height(20.dp),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(
                            Font(
                                R.font.jio_type_medium, FontWeight(700)
                            )
                        )
                    ),
                    color = Color(0xA6000000),
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(32.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = day,
                        style = TextStyle(
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontFamily = FontFamily(Font(R.font.jio_type_medium)),
                            fontWeight = FontWeight(700),
                            color = Color(0xA6000000),
                            textAlign = TextAlign.Center,
                        )
                    )
                }


            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = Color(0xFF388E3C),
                            shape = RoundedCornerShape(50)
                        )
                )
            }

        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<DateRange> {
    val dates = mutableListOf<DateRange>()
    var currentDate = startDate

    var index = 0

    while (currentDate <= endDate) {
        index ++
        val dateRange = DateRange(currentDate,index)
        dates.add(dateRange)
        currentDate = currentDate.plusDays(1)
    }

    return dates
}

data class DateRange(val date: LocalDate, val key: Int)

fun LazyListState.animateScrollAndCentralizeItem(index: Int, scope: CoroutineScope) {
    val itemInfo = this.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    scope.launch {
        if (itemInfo != null) {
            val center = this@animateScrollAndCentralizeItem.layoutInfo.viewportEndOffset / 2
            val childCenter = itemInfo.offset + itemInfo.size / 2
            this@animateScrollAndCentralizeItem.animateScrollBy((childCenter - center).toFloat())
        } else {
            this@animateScrollAndCentralizeItem.animateScrollToItem(index)
        }
    }
}

fun dateToKey(date: LocalDate): String {
    return "${date.year}${date.monthValue.toString().padStart(2, '0')}${date.dayOfMonth.toString().padStart(2, '0')}"
}