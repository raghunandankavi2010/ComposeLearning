package com.example.composelearning.audiostream.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composelearning.audiostream.domain.StreamingState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioStreamRoute(
    viewModel: AudioStreamViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val micPermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    AudioStreamScreen(
        state = uiState,
        onToggle = {
            if (micPermission.status.isGranted) {
                viewModel.onToggleStreaming()
            } else {
                micPermission.launchPermissionRequest()
            }
        },
    )
}

@Composable
fun AudioStreamScreen(
    state: AudioStreamUiState,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Real-time Audio Streaming",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Captures 16 kHz mono PCM via AudioRecord and streams it over a WebSocket " +
                "to the Ktor server (run :audio-stream-server first).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onToggle,
            shape = CircleShape,
            modifier = Modifier.size(140.dp),
            colors = if (state.isActive) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Icon(
                imageVector = if (state.isActive) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (state.isActive) "Stop streaming" else "Start streaming",
                modifier = Modifier.size(56.dp),
            )
        }

        StatusText(state)
    }
}

@Composable
private fun StatusText(state: AudioStreamUiState) {
    val (label, color) = when (val s = state.state) {
        StreamingState.Idle -> "Tap to start streaming" to MaterialTheme.colorScheme.onSurfaceVariant
        StreamingState.Connecting -> "Connecting…" to MaterialTheme.colorScheme.primary
        is StreamingState.Streaming -> "● Streaming — ${s.bytesSent / 1024} KB sent" to MaterialTheme.colorScheme.error
        StreamingState.Stopped -> "Stopped — END sent, socket closed" to MaterialTheme.colorScheme.primary
        is StreamingState.Error -> s.message to MaterialTheme.colorScheme.error
    }
    Text(text = label, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Medium)
}
