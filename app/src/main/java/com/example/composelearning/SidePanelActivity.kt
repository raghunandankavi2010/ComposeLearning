package com.example.composelearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composelearning.animcompose.SidePanelArrangement
import com.example.composelearning.animcompose.SidePanelLayout
import com.example.composelearning.animcompose.SidePanelState
import com.example.composelearning.animcompose.rememberSidePanelState
import kotlinx.coroutines.launch

class SidePanelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DraggableSheetRight()
                }
            }
        }
    }
}

@Composable
fun MainScreenContent() {
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
            // Removed the width modifier from here, as it's now a direct parameter
            modifier = Modifier.fillMaxHeight(), // Only height, width is handled by maxPanelWidth param
            state = sidePanelState,
            maxPanelWidth = 300.dp, // <--- NEW PARAMETER HERE
            arrangement = SidePanelArrangement.End,
            containerColor = Color.Green,
            dragHandleContent = { isExpanded, progress ->
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 60.dp)
                        .background(Color.DarkGray, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isExpanded) "<" else ">",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            content = { progress ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Panel Content (Green)",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun PreviewMainScreenContent() {
    MaterialTheme {
        MainScreenContent()
    }
}