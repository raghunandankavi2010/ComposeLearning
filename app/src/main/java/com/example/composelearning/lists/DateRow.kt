package com.example.composelearning.lists

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
    val dateFormatter = DateTimeFormatter.ofPattern("MMM")
    val dateFormatter2 = DateTimeFormatter.ofPattern("d")
    val lazyListState = rememberLazyListState()

    val calendarStartDate: LocalDate = LocalDate.now()

    // Defaulting to 2 years from current date.
    val calendarEndDate: LocalDate = LocalDate.now()
        .withMonth(12).withDayOfMonth(31)

    // Calculate initial dates (adjust as needed)
    val initialDates = getDatesBetween(calendarStartDate, calendarEndDate)

    // State to hold the currently displayed dates
    val dates by remember { mutableStateOf(initialDates) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val itemWidthWithSpacing = 54.dp + 8.dp // Assuming

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(dates) { index, date ->
            DateCard(date, dateFormatter, dateFormatter2)
        }
    }

    // Launch an effect to scroll to the current date
    LaunchedEffect(Unit) {
        val currentIndex = dates.indexOf(LocalDate.now())
        val approximateVisibleItems = (screenWidth / itemWidthWithSpacing).toInt()
        val scrollToIndex = maxOf(0, currentIndex - approximateVisibleItems / 2)
        lazyListState.scrollToItem(scrollToIndex)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateCard(date: LocalDate, dateFormatter: DateTimeFormatter, dateFormatter2: DateTimeFormatter) {
    Card(
        modifier =
        Modifier
            .width(54.dp)
            .height(72.dp)
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(size = 8.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = dateFormatter.format(date),
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
                    text = dateFormatter2.format(date),
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
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var currentDate = startDate

    while (currentDate <= endDate) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return dates
}

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