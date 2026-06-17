package com.example.composelearning.sarvamstt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechToTextRoute(
    viewModel: SpeechToTextViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val micPermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    SpeechToTextScreen(
        state = uiState,
        onRecordToggle = {
            if (micPermission.status.isGranted) {
                viewModel.onRecordToggle()
            } else {
                micPermission.launchPermissionRequest()
            }
        },
    )
}

@Composable
fun SpeechToTextScreen(
    state: SpeechUiState,
    onRecordToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Speak & Detect Language",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Records a short WAV clip and uploads it to Sarvam STT (saaras:v3) to " +
                "transcribe it and detect the spoken language.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RecordButton(state = state, onClick = onRecordToggle)

        StatusLine(state)

        val transcript = (state as? SpeechUiState.Success)?.transcript.orEmpty()
        val language = (state as? SpeechUiState.Success)?.let {
            buildString {
                append(it.languageCode)
                it.confidence?.let { c -> append("  ·  ${(c * 100).toInt()}% confidence") }
            }
        }.orEmpty()

        OutlinedTextField(
            value = language,
            onValueChange = {},
            readOnly = true,
            label = { Text("Detected language") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = transcript,
            onValueChange = {},
            readOnly = true,
            label = { Text("Transcript") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )
    }
}

@Composable
private fun RecordButton(state: SpeechUiState, onClick: () -> Unit) {
    val isRecording = state is SpeechUiState.Recording
    val isUploading = state is SpeechUiState.Uploading

    Button(
        onClick = onClick,
        enabled = !isUploading,
        modifier = Modifier.fillMaxWidth(),
        colors = if (isRecording) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.buttonColors()
        },
    ) {
        when {
            isUploading -> {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(0.dp))
                Text("  Uploading…")
            }
            isRecording -> Text("Stop & Detect")
            else -> Text("Record Audio")
        }
    }
}

@Composable
private fun StatusLine(state: SpeechUiState) {
    val (text, color) = when (state) {
        SpeechUiState.Idle -> "Tap to record." to MaterialTheme.colorScheme.onSurfaceVariant
        SpeechUiState.Recording -> "● Recording — tap again to stop." to MaterialTheme.colorScheme.error
        SpeechUiState.Uploading -> "Transcribing with Sarvam…" to MaterialTheme.colorScheme.onSurfaceVariant
        is SpeechUiState.Success -> "Done." to MaterialTheme.colorScheme.primary
        is SpeechUiState.Error -> state.message to MaterialTheme.colorScheme.error
    }
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
}
