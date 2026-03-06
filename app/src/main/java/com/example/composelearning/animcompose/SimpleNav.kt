package com.example.composelearning.animcompose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

// 1. Define Destinations (Using Type-Safe Navigation)
@Serializable object HomeRoute
@Serializable object SettingsRoute
@Serializable data class DetailRoute(val id: Int)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SimpleAppNavigation() {
    val navController = rememberNavController()
    
    // We wrap everything in SharedTransitionLayout to enable shared elements between screens
    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                // Observe the backstack to hide bottom bar on details
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // Show bottom bar only if we are on Home or Settings
                val isTopLevel = currentDestination?.hierarchy?.any { 
                    it.hasRoute<HomeRoute>() || it.hasRoute<SettingsRoute>() 
                } == true

                AnimatedVisibility(
                    visible = isTopLevel,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.hasRoute<HomeRoute>() } == true,
                            onClick = { 
                                navController.navigate(HomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, "Home") },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.hasRoute<SettingsRoute>() } == true,
                            onClick = { 
                                navController.navigate(SettingsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Settings, "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<HomeRoute> {
                    // Pass 'this' (AnimatedVisibilityScope) to the screen for shared elements
                    HomeScreen(
                        animatedVisibilityScope = this,
                        onItemClick = { id -> navController.navigate(DetailRoute(id)) }
                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen()
                }
                composable<DetailRoute> { backStackEntry ->
                    val route: DetailRoute = backStackEntry.arguments?.let { 
                        // In modern Navigation, the arguments are parsed automatically
                        // but for manual retrieval if needed:
                        DetailRoute(it.getInt("id"))
                    } ?: DetailRoute(0)
                    
                    DetailScreen(
                        id = route.id,
                        animatedVisibilityScope = this
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun androidx.compose.animation.SharedTransitionScope.HomeScreen(
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onItemClick: (Int) -> Unit
) {
    val items = (1..20).toList()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Home Screen", fontSize = 24.sp)
        LazyColumn {
            items(items) { id ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { onItemClick(id) }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Shared Element Example: The text will "fly" to the detail screen
                    Text(
                        text = "Item #$id",
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "text-$id"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Hello World", fontSize = 24.sp)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun androidx.compose.animation.SharedTransitionScope.DetailScreen(
    id: Int,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope
) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // The matching shared element key
        Text(
            text = "Item #$id",
            fontSize = 40.sp,
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "text-$id"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        )
        Text("\nThis is the detail screen for item $id.\nNotice the bottom bar is gone!", fontSize = 18.sp)
    }
}
