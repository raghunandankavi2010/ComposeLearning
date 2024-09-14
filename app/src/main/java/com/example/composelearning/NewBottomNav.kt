package com.example.composelearning

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SimpleJetsnackApp() {
    val navController = rememberNavController()
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    CompositionLocalProvider(
                        LocalNavAnimatedVisibilityScope provides this
                    ) {
                        HomeScreen(onSnackSelected = { navController.navigate("detail") })
                    }
                }
                composable(
                    "detail",
                ) {
                    DetailScreen()
                }
            }
        }
    }
}




@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(onSnackSelected: () -> Unit) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No SharedElementScope found")
    Scaffold(
        bottomBar = {
            with(animatedVisibilityScope) {
                with(sharedTransitionScope) {
                    NavigationBar(
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = true, // You'll need to manage the selected state
                            onClick = { /*TODO*/ }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") },
                            selected = false, // You'll need to manage the selected state
                            onClick = { /*TODO*/ }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onSnackSelected) {
                    Text("Go to Detail")
                }
            }

    }
}

@Composable
fun DetailScreen() {
    // Detail screen content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("Detail Screen")
    }
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
