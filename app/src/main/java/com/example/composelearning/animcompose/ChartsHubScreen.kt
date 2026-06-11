/*
 * Copyright 2024 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.animcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composelearning.charts.*
import com.example.composelearning.graphics.SineWaveSample

/**
 * A central hub for all Charting and Wave-related screens.
 * Consolidates various chart types and wave animations into a single tabbed interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsHubScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Line", "Bar", "Donut", "Pie", "Candle", "Speed", "Temp", "Bezier", "Waves"
    )

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                TopAppBar(
                    title = { Text("Charts & Waves Showcase", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> LineChartContent()
                1 -> BarChartContent()
                2 -> DonutChartContent()
                3 -> PieChartContent()
                4 -> CandleChartContent()
                5 -> SpeedometerContent()
                6 -> TemperatureContent()
                7 -> BezierContent()
                8 -> WavesTabContent()
            }
        }
    }
}

@Composable
private fun WavesTabContent() {
    val tabs = listOf("Sine Wave Sample", "Sin Wave Path", "Canvas Sin Wave")
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedSubTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title) }
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedSubTab) {
                0 -> SineWaveSample(onBack = {})
                1 -> TutorialContent()
                2 -> SinWave()
            }
        }
    }
}
