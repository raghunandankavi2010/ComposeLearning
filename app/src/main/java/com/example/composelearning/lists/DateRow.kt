package com.example.composelearning.lists

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarLazyRow() {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy, MMM d")
    val lazyListState = rememberLazyListState()

    val calendarStartDate: LocalDate = LocalDate.now()
        .withMonth(1).withDayOfMonth(1)

    // Defaulting to 2 years from current date.
    val calendarEndDate: LocalDate = LocalDate.now().plusYears(2)
        .withMonth(12).withDayOfMonth(31)

    // Calculate initial dates (adjust as needed)
    val initialDates = getDatesBetween(calendarStartDate,calendarEndDate)

    // State to hold the currently displayed dates
    val dates by remember { mutableStateOf(initialDates) }

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(dates) { index, date ->
            DateCard(date, dateFormatter)
        }
    }

    // Launch an effect to scroll to the current date
    LaunchedEffect(Unit) {
        val currentIndex = dates.indexOf(LocalDate.now()) - 2
        if (currentIndex >= 0) {
            lazyListState.scrollToItem(currentIndex)
        }
    }
}

@Composable
fun DateCard(date: LocalDate, dateFormatter: DateTimeFormatter) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dateFormatter.format(date))
        }
    }
}



fun getDatesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var currentDate = startDate

    while (currentDate <= endDate) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return dates
}