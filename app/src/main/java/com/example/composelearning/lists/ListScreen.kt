package com.example.composelearning.lists

// Screens.kt
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun ListScreen(navController: NavController) {
    val listState = rememberLazyListState()

    var items by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        delay(1000)
        items = (1..100).map { "Item #$it" }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("ListScreen", "Destroyed")
        }
    }
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("detail/$item") }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun DetailScreen(item: String?, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Detail for: $item", fontSize = 24.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}