package com.example.composelearning.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Define your navigation routes
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Details : Screen("details/{name}") {
        fun createRoute(name: String): String {
            return "details/$name"
        }
    }

    data object ThirdScreen : Screen("thirdScreen")
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(onNavigateSecondScreen: (String) -> Unit) {
    var name by remember { mutableStateOf("detailscreen") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text= "Title") }
            )
        }
    ) {
        Column {
            Button(modifier = Modifier.padding(top = 100.dp).heightIn(min = 80.dp).fillMaxWidth(),
                onClick = {
                    onNavigateSecondScreen(name)

            }) {
                Text("Second Screen")
            }
            Text("Hello")
            Text(name)
        }

    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(name: String, onBackClick: () -> Unit, onNavigateThirdScreen: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details screen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column {
            Button(modifier = Modifier.padding(top = 100.dp).heightIn(min = 80.dp).fillMaxWidth(),
                onClick = {
                    onNavigateThirdScreen()

                }) {
                Text("Navigate ThirdScreen Screen")
            }

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirdScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Third screen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Text("Third Screen")
    }
}
