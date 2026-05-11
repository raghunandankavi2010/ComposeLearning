package com.example.composelearning.animcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SidePanelDemoScreen() {
    val sidePanelState = rememberSidePanelState(initialState = SidePanelState.Collapsed)
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content of your screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Main Screen Content",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = {
                scope.launch {
                    sidePanelState.toggle()
                }
            }) {
                Text("Toggle Side Panel")
            }
        }

        // Our Custom Side Panel Layout
        SidePanelLayout(
            modifier = Modifier.fillMaxHeight(),
            state = sidePanelState,
            maxPanelWidth = 300.dp,
            arrangement = SidePanelArrangement.End,
            containerColor = Color(0xFF4CAF50),
            dragHandleContent = { isExpanded, progress ->
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 60.dp)
                        .background(Color.DarkGray, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isExpanded) ">" else "<",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            content = { progress ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Panel Content",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        )
    }
}
