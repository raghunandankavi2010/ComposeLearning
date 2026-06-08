import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val navHostController: NavHostController = rememberNavController()
    val bottomBarState = remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (bottomBarState.value) {
                BottomNavigationBar(navHostController)
            }
        }
    ) {
        NavigationGraph(navHostController, bottomBarState)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            selected = false,
            onClick = { navController.navigate("favorites") }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.bottomNavWrappedComposable(
    navController: NavController,
    route: String,
    bottomBarState: MutableState<Boolean>,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = { fadeIn(animationSpec = tween(700)) },
        exitTransition = { fadeOut(animationSpec = tween(700)) }
    ) { entry ->
        Column {
            Box(modifier = Modifier.weight(1f)) {
                content(entry)
            }
            if (route == "home" || route == "favorites") {
                bottomBarState.value = true
                BottomNavigationBar(navController)
            } else {
                bottomBarState.value = false
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationGraph(navController: NavHostController, bottomBarState: MutableState<Boolean>) {
    NavHost(navController, startDestination = "home") {
        bottomNavWrappedComposable(navController, "home", bottomBarState) { entry ->
            HomeScreen(navController)
        }
        bottomNavWrappedComposable(navController, "favorites", bottomBarState) { entry ->
            FavoritesScreen(navController)
        }
        composable("details/{itemId}",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) { backStackEntry ->
            bottomBarState.value = false
            DetailsScreen(navController, backStackEntry.arguments?.getString("itemId"))
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column {
        Text("Home Screen")
        Button(onClick = { navController.navigate("details/1") }) {
            Text("Go to Details")
        }
    }
}

@Composable
fun FavoritesScreen(navController: NavController) {
    Column {
        Text("Favorites Screen")
        Button(onClick = { navController.navigate("details/2") }) {
            Text("Go to Details")
        }
    }
}

@Composable
fun DetailsScreen(navController: NavController, itemId: String?) {
    Column {
        Text("Details Screen for item $itemId")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
