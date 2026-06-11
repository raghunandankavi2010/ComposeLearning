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

/**
 * ParticleAnimationsHubScreen
 * Consolidates all particle demos into a single tabbed interface.
 * Fixed UI overlap issue by strictly using Scaffold's padding for HUD elements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticleAnimationsHubScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("3D Explosion", "Stream", "Physics", "Fireworks")

    Scaffold(
        topBar = {
            Surface(
                color = Color.Black.copy(alpha = 0.9f),
                contentColor = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
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
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        // Fill entire screen with black background
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        // Use a Box to hold the full-screen animation background
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                // We pass the padding to the screens so they can offset their HUDs/text
                0 -> ParticleExpExplosion3D(contentPadding = padding)

                1 -> Box(Modifier.padding(padding)) { ParticleExplosionScreen() }

                2 -> Box(Modifier.padding(padding)) { RealisticExplosionScreen() }

                3 -> Box(Modifier.padding(padding)) { NewYearsEveFireworksScreen() }
            }
        }
    }
}
