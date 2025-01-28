package com.example.composelearning

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Other : Screen("other")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar() {
    val navController = rememberNavController()
    val showSearch = remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf("home", "profile", "settings","other")
    val drawerItems = listOf(
        Icons.Default.AccountCircle,
        Icons.Default.Favorite,
    )

    val drawerSelectedItem = remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(12.dp))
                    items.forEach { item ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_launcher_background),
                                    contentDescription = null
                                )
                            },
                            label = { Text(item) },
                            selected = item == drawerSelectedItem.value,
                            onClick = {
                                scope.launch { drawerState.close() }
                                drawerSelectedItem.value = item
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "BottombarDemo") },
                    actions = {
                       // if (showSearch.value) {
                            IconButton(onClick = { showSearch.value = false }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        //} else {
                            IconButton(onClick = { showSearch.value = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        //}
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                        }
                    }
                )
            },
            bottomBar = {

                // observe the backstack
                val navBackStackEntry by navController.currentBackStackEntryAsState()

                // observe current route to change the icon
                // color,label color when navigated
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Favorite, contentDescription = item) },
                            label = { Text(item) },
                            selected =  currentRoute == item,
                            onClick = {

                                if (currentRoute != item) {
//
//                                    val visibleEntries = navController.visibleEntries.value
//                                    if (visibleEntries.any { it.destination.route == item }) {
//                                        // If the entry already exists, pop it
//                                        navController.popBackStack(item, inclusive = true)
//                                    }
                                    // Navigate to the new destination
                                    navController.navigate(item) {
                                        navController.popBackStack(item,true)
                                        launchSingleTop = true
                                       // restoreState = true
                                    }
                                }
//
//                                    navController.navigate(item) {
//                                    //popUpTo(navController.graph.startDestinationId)
//                                    launchSingleTop = true
//                                }
                            }
                        )
                    }
                }
            },
            ) { inerPadding ->
            NavHost(modifier = Modifier.padding(inerPadding),navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) { HomeContent() }
                composable(Screen.Profile.route) { ProfileContent() }
                composable(Screen.Settings.route) { SettingsContent() }
                composable(Screen.Other.route) { OtherContent() }
            }
        }
    }
}

// Replace with your actual content for each screen
@Composable
fun HomeContent() {
    // Content for Home screen
    Text("home")
}

@Composable
fun ProfileContent() {
    Text("profile")
    // Content for Profile screen
}

@Composable
fun SettingsContent() {
    Text("settings")
    // Content for Settings screen
}

@Composable
fun OtherContent() {
    Text("Other")
    // Content for Other screen
}
