package com.example.composelearning.gesturenexus.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.HandMode
import com.example.composelearning.gesturenexus.presentation.GestureNexusContract.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureNexusScreen(
    onBack: () -> Unit = {},
    viewModel: GestureNexusViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initTts(context)
    }

    val paletteColors = listOf(
        Color(0xFF00E5FF),
        Color(0xFF00E676),
        Color(0xFFFFD600),
        Color(0xFFFF9100),
        Color(0xFFE040FB)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GestureNexus AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "3D Air Canvas & Sign Synthesizer",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.processIntent(Intent.ClearCanvas) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1117))
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode Selector Tabs
            PrimaryTabRow(
                selectedTabIndex = if (state.handMode == HandMode.AIR_CANVAS_DRAWING) 0 else 1,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF00E5FF),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = state.handMode == HandMode.AIR_CANVAS_DRAWING,
                    onClick = { viewModel.processIntent(Intent.SelectMode(HandMode.AIR_CANVAS_DRAWING)) },
                    text = { Text("Air Canvas", color = Color.White) }
                )
                Tab(
                    selected = state.handMode == HandMode.ASL_SIGN_TRANSLATOR,
                    onClick = { viewModel.processIntent(Intent.SelectMode(HandMode.ASL_SIGN_TRANSLATOR)) },
                    text = { Text("ASL Sign AI", color = Color.White) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas & Hand Skeletal Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161B22))
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
            ) {
                AirCanvasOverlay(
                    handLandmarks = state.handLandmarks,
                    drawnStrokes = state.drawnStrokes,
                    modifier = Modifier.fillMaxSize()
                )

                // Current Gesture Badge
                Surface(
                    color = Color(0xCC0D1117),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (state.handMode == HandMode.AIR_CANVAS_DRAWING) Icons.Default.Brush else Icons.Default.BackHand,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.currentGesture.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom Controls / ASL Transcription Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.handMode == HandMode.AIR_CANVAS_DRAWING) {
                    // Color Palette Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paletteColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (state.activeColor == color) 3.dp else 0.dp,
                                        color = if (state.activeColor == color) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.processIntent(Intent.SelectColor(color)) }
                            )
                        }
                    }
                } else {
                    // ASL Transcription Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Transcribed Sign Sentence", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    text = if (state.transcribedText.isBlank()) "Scanning ASL gestures..." else state.transcribedText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.processIntent(Intent.SpeakTranscribedText) },
                                modifier = Modifier.background(Color(0xFF00E5FF), CircleShape)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
