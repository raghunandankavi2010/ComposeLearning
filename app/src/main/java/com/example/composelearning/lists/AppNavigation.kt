package com.example.composelearning.lists

// AppNavigation.kt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation

// Define routes for clarity
object Routes {
    const val LIST_GRAPH = "list_graph"
    const val LIST_SCREEN = "list"
    const val DETAIL_SCREEN = "detail/{item}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LIST_GRAPH
    ) {
        // --- The List Navigation Graph ---
        // The ViewModel will be scoped to this graph
        navigation(
            startDestination = Routes.LIST_SCREEN,
            route = Routes.LIST_GRAPH
        ) {
            composable(Routes.LIST_SCREEN) {
                // Get the NavBackStackEntry for the graph
                val listGraphBackStackEntry = remember(it) {
                    navController.getBackStackEntry(Routes.LIST_GRAPH)
                }

                // Retrieve the ViewModel, scoped to the graph's back stack entry
                val listViewModel: MyListViewModel = viewModel(listGraphBackStackEntry)

                ListScreen(navController = navController)
            }

            composable(
                route = Routes.DETAIL_SCREEN,
                arguments = listOf(navArgument("item") { type = NavType.StringType })
            ) { backStackEntry ->
                DetailScreen(
                    item = backStackEntry.arguments?.getString("item"),
                    navController = navController
                )
            }
        }
    }
}