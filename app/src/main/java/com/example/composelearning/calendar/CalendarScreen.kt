/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.composelearning.calendar.model.CalendarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    onBackPressed: () -> Unit,
    mainViewModel: CalendarViewModel
) {
    val calendarState = remember {
        mainViewModel.calendarState
    }

    CalendarContent(
        calendarState = calendarState,
        onDayClicked = { dateClicked ->
            mainViewModel.onDaySelected(dateClicked)
        },
        onBackPressed = onBackPressed
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    calendarState: CalendarState,
    onDayClicked: (LocalDate) -> Unit,
    onBackPressed: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
            )
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.White,
        topBar = {
            CalendarTopAppBar(calendarState, onBackPressed, scrollBehavior)
        }
    ) { contentPadding ->
        Calendar(
            calendarState = calendarState,
            onDayClicked = onDayClicked,
            contentPadding = contentPadding
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopAppBar(
    calendarState: CalendarState,
    onBackPressed: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    val calendarUiState = calendarState.calendarUiState.value
    val isScrolled = scrollBehavior.state.contentOffset < -1f
    val elevation = if (isScrolled) 4.dp else 0.dp

    Column(modifier = Modifier.shadow(elevation)) {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .fillMaxWidth()
                .background(Color.Blue)
        )
        TopAppBar(
            title = {
                Text(
                    text = if (!calendarUiState.hasSelectedDates) {
                        "Select Date"
                    } else {
                        calendarUiState.selectedDatesFormatted
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                scrolledContainerColor = Color.White,
            ),
            scrollBehavior = scrollBehavior,
        )
    }
}
