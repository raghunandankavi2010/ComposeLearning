package com.example.composelearning.speechlang.presentation

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.speechlang.domain.model.DetectedLanguage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Entry point: wires the ViewModel and the `RECORD_AUDIO` permission to the stateless
 * [SpeechLangScreen]. Listening is only allowed once the permission is granted; otherwise the mic
 * tap requests it.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpeechLangRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: SpeechLangViewModel =
        viewModel(factory = SpeechLangViewModel.Factory(context.applicationContext))
    val state by viewModel.state.collectAsStateWithLifecycle()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val granted = micPermission.status is PermissionStatus.Granted

    SpeechLangScreen(
        state = state,
        micPermissionGranted = granted,
        onToggleListening = {
            if (granted) viewModel.onIntent(SpeechLangIntent.ToggleListening)
            else micPermission.launchPermissionRequest()
        },
        onConsumeError = { viewModel.onIntent(SpeechLangIntent.ConsumeError) },
        onSelectInputLanguage = { tag -> viewModel.onIntent(SpeechLangIntent.SelectInputLanguage(tag)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechLangScreen(
    state: SpeechLangUiState,
    micPermissionGranted: Boolean,
    onToggleListening: () -> Unit,
    onConsumeError: () -> Unit,
    onSelectInputLanguage: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface transient errors as a snackbar, then clear them so they don't re-show on recomposition.
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onConsumeError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Speak & Detect Language") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StatusLine(state = state, micPermissionGranted = micPermissionGranted)

            LanguagePicker(
                selectedTag = state.inputLanguageTag,
                enabled = !state.isListening && !state.isProcessing,
                onSelect = onSelectInputLanguage,
            )

            TranscriptCard(transcript = state.transcript)

            ResultBanner(
                detectedLanguage = state.detectedLanguage,
                isProcessing = state.isProcessing,
            )

            Spacer(Modifier.size(8.dp))

            MicButton(
                isListening = state.isListening,
                enabled = !state.isProcessing,
                onClick = onToggleListening,
            )

            Text(
                text = if (state.isListening) "Listening… tap to stop" else "Tap the mic and start talking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusLine(state: SpeechLangUiState, micPermissionGranted: Boolean) {
    val label = when {
        !micPermissionGranted -> "Microphone permission needed"
        !state.detectorReady -> "Loading language model…"
        state.status == SpeechStatus.LISTENING -> "Listening"
        state.status == SpeechStatus.PROCESSING -> "Detecting language…"
        state.status == SpeechStatus.SUCCESS -> "Done"
        else -> "Ready"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * Dropdown that picks which language the recognizer should expect. Setting this makes STT
 * transcribe in the correct script (e.g. Tamil instead of romanized Latin), which is what lets the
 * downstream detector classify accurately. Disabled while a session is active.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePicker(
    selectedTag: String?,
    enabled: Boolean,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = SupportedInputLanguages.firstOrNull { it.tag == selectedTag }
        ?: SupportedInputLanguages.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Spoken language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SupportedInputLanguages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.label) },
                    onClick = {
                        onSelect(language.tag)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TranscriptCard(transcript: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Transcript",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = transcript.ifBlank { "Your speech will appear here…" },
                style = MaterialTheme.typography.titleMedium,
                color = if (transcript.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            )
        }
    }
}

@Composable
private fun ResultBanner(detectedLanguage: DetectedLanguage?, isProcessing: Boolean) {
    when {
        isProcessing -> {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("Detecting language…") },
                leadingIcon = {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                },
            )
        }

        detectedLanguage != null -> {
            val percent = (detectedLanguage.confidence * 100).toInt()
            AssistChip(
                onClick = {},
                label = {
                    Text("Detected: ${detectedLanguage.displayName} (${detectedLanguage.code}) · $percent%")
                },
                leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(
                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun MicButton(isListening: Boolean, enabled: Boolean, onClick: () -> Unit) {
    // Subtle pulse while listening for clear visual feedback.
    val scale by animateFloatAsState(targetValue = if (isListening) 1.12f else 1f, label = "micPulse")
    val icon: ImageVector = when {
        isListening -> Icons.Filled.Stop
        else -> Icons.Filled.Mic
    }

    Box(contentAlignment = Alignment.Center) {
        FloatingActionButton(
            onClick = { if (enabled) onClick() },
            shape = CircleShape,
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            modifier = Modifier
                .size(96.dp)
                .scale(scale),
        ) {
            Icon(
                imageVector = if (!enabled) Icons.Filled.GraphicEq else icon,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Success")
@Composable
private fun SpeechLangScreenSuccessPreview() {
    SpeechLangScreen(
        state = SpeechLangUiState(
            status = SpeechStatus.SUCCESS,
            transcript = "Bonjour, comment allez-vous aujourd'hui ?",
            detectedLanguage = DetectedLanguage(code = "fr", displayName = "French", confidence = 0.97f),
            detectorReady = true,
        ),
        micPermissionGranted = true,
        onToggleListening = {},
        onConsumeError = {},
        onSelectInputLanguage = {},
    )
}

@Preview(showBackground = true, name = "Listening")
@Composable
private fun SpeechLangScreenListeningPreview() {
    SpeechLangScreen(
        state = SpeechLangUiState(
            status = SpeechStatus.LISTENING,
            transcript = "the quick brown fox",
            detectorReady = true,
            inputLanguageTag = "ta-IN",
        ),
        micPermissionGranted = true,
        onToggleListening = {},
        onConsumeError = {},
        onSelectInputLanguage = {},
    )
}
