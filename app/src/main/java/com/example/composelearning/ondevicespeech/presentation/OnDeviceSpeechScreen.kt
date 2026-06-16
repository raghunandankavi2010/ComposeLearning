package com.example.composelearning.ondevicespeech.presentation

import android.Manifest
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composelearning.speechlang.domain.model.DetectedLanguage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Entry point: wires the [SpeechViewModel] and `RECORD_AUDIO` permission to the stateless
 * [OnDeviceSpeechScreen].
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnDeviceSpeechRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: SpeechViewModel =
        viewModel(factory = SpeechViewModel.Factory(context.applicationContext))

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocale by viewModel.selectedLocale.collectAsStateWithLifecycle()
    val partialTranscript by viewModel.partialTranscript.collectAsStateWithLifecycle()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val granted = micPermission.status is PermissionStatus.Granted

    OnDeviceSpeechScreen(
        uiState = uiState,
        selectedLocale = selectedLocale,
        partialTranscript = partialTranscript,
        micPermissionGranted = granted,
        onSelectLocale = { viewModel.onIntent(SpeechIntent.SelectLocale(it)) },
        onToggleListening = {
            if (granted) viewModel.onIntent(SpeechIntent.ToggleListening)
            else micPermission.launchPermissionRequest()
        },
        onRetry = { viewModel.onIntent(SpeechIntent.RetryLanguagePack) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnDeviceSpeechScreen(
    uiState: SpeechUiState,
    selectedLocale: IndicLocale,
    partialTranscript: String,
    micPermissionGranted: Boolean,
    onSelectLocale: (IndicLocale) -> Unit,
    onToggleListening: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("On-Device Speech (Indic)") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LocalePicker(
                selected = selectedLocale,
                enabled = uiState !is SpeechUiState.Listening && uiState !is SpeechUiState.DownloadingPack,
                onSelect = onSelectLocale,
            )

            // Status / content area reacts to the phase machine.
            PhaseContent(
                uiState = uiState,
                partialTranscript = partialTranscript,
                micPermissionGranted = micPermissionGranted,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            val listening = uiState is SpeechUiState.Listening
            val canToggle = micPermissionGranted && (
                uiState is SpeechUiState.ReadyToListen ||
                    uiState is SpeechUiState.Listening ||
                    uiState is SpeechUiState.Success ||
                    uiState is SpeechUiState.Error
                )

            MicButton(isListening = listening, enabled = canToggle, onClick = onToggleListening)

            Text(
                text = when {
                    !micPermissionGranted -> "Microphone permission needed"
                    listening -> "Listening… tap to stop"
                    else -> "Tap the mic and speak in ${selectedLocale.displayName}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PhaseContent(
    uiState: SpeechUiState,
    partialTranscript: String,
    micPermissionGranted: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (uiState) {
            SpeechUiState.CheckingLanguagePacks -> LabeledSpinner("Checking language pack…")

            is SpeechUiState.DownloadingPack -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Downloading ${uiState.locale} language pack…",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (uiState.progress != null) {
                    LinearProgressIndicator(
                        progress = { uiState.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${uiState.progress}%", style = MaterialTheme.typography.labelMedium)
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            SpeechUiState.ReadyToListen -> Text(
                "Ready — tap the mic to start",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            SpeechUiState.Listening -> TranscriptCard(
                title = "Listening",
                text = partialTranscript.ifBlank { "Speak now…" },
            )

            SpeechUiState.Processing -> LabeledSpinner("Detecting language…")

            is SpeechUiState.Success -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TranscriptCard(title = "Transcript", text = uiState.transcribedText)
                DetectedLanguageChip(uiState.detectedLanguage)
            }

            is SpeechUiState.Error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
private fun LabeledSpinner(label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun TranscriptCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun DetectedLanguageChip(detected: DetectedLanguage?) {
    if (detected == null) {
        AssistChip(onClick = {}, enabled = false, label = { Text("Language not detected") })
        return
    }
    val percent = (detected.confidence * 100).toInt()
    AssistChip(
        onClick = {},
        label = { Text("Detected: ${detected.displayName} (${detected.code}) · $percent%") },
        leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null) },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalePicker(
    selected: IndicLocale,
    enabled: Boolean,
    onSelect: (IndicLocale) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            IndicLocale.entries.forEach { locale ->
                DropdownMenuItem(
                    text = { Text("${locale.displayName}  (${locale.tag})") },
                    onClick = {
                        onSelect(locale)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Mic FAB with an expanding ripple ring while [isListening] — a clear "recording" affordance.
 * The ripple animates only when listening, so it costs nothing in the idle state.
 */
@Composable
private fun MicButton(isListening: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val rippleColor = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "ripple")
    val rippleScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "rippleScale",
    )
    val rippleAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "rippleAlpha",
    )

    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(rippleScale)
                    .background(rippleColor.copy(alpha = rippleAlpha), CircleShape),
            )
        }
        FloatingActionButton(
            onClick = { if (enabled) onClick() },
            shape = CircleShape,
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            modifier = Modifier.size(88.dp),
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Success")
@Composable
private fun OnDeviceSpeechSuccessPreview() {
    OnDeviceSpeechScreen(
        uiState = SpeechUiState.Success(
            transcribedText = "வணக்கம், சாப்பிட்டீர்களா?",
            detectedLanguage = DetectedLanguage(code = "ta", displayName = "Tamil", confidence = 0.96f),
        ),
        selectedLocale = IndicLocale.TAMIL,
        partialTranscript = "",
        micPermissionGranted = true,
        onSelectLocale = {},
        onToggleListening = {},
        onRetry = {},
    )
}

@Preview(showBackground = true, name = "Downloading")
@Composable
private fun OnDeviceSpeechDownloadingPreview() {
    OnDeviceSpeechScreen(
        uiState = SpeechUiState.DownloadingPack(locale = "Telugu", progress = 62),
        selectedLocale = IndicLocale.TELUGU,
        partialTranscript = "",
        micPermissionGranted = true,
        onSelectLocale = {},
        onToggleListening = {},
        onRetry = {},
    )
}
