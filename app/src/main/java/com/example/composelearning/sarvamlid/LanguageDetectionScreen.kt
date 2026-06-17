package com.example.composelearning.sarvamlid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Route: owns the ViewModel and passes plain state + lambdas down (Route -> Screen pattern).
 * Supply your Sarvam key via BuildConfig / secrets — never hard-code it in source.
 */
@Composable
fun LanguageDetectionRoute(
    apiKey: String,
    viewModel: LanguageDetectionViewModel = viewModel(
        factory = LanguageDetectionViewModel.Factory(apiKey),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LanguageDetectionScreen(
        state = uiState,
        onInputChange = viewModel::onInputChange,
        onDetect = viewModel::detect,
    )
}

@Composable
fun LanguageDetectionScreen(
    state: LanguageDetectionUiState,
    onInputChange: (String) -> Unit,
    onDetect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Indic Language Detector",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Detects Hindi, Marathi, Gujarati, Telugu, Tamil & Kannada. " +
                "Resolved on-device by script; only Hindi vs Marathi needs the cloud.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.input,
            onValueChange = onInputChange,
            label = { Text("Type or paste text") },
            placeholder = { Text("उदा. नमस्ते / வணக்கம் / ನಮಸ್ಕಾರ") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )

        Button(
            onClick = onDetect,
            enabled = !state.isDetecting && state.input.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(0.dp))
                Text("  Detecting…")
            } else {
                Text("Detect language")
            }
        }

        state.result?.let { ResultCard(it) }
    }
}

@Composable
private fun ResultCard(result: DetectionResult) {
    val (container, content) = when (result) {
        is DetectionResult.Success -> MaterialTheme.colorScheme.primaryContainer to
            MaterialTheme.colorScheme.onPrimaryContainer
        is DetectionResult.Failure -> MaterialTheme.colorScheme.errorContainer to
            MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (result) {
                is DetectionResult.Success -> {
                    Text(
                        text = "${result.language.nativeName}  •  ${result.language.displayName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = content,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = result.language.sarvamCode,
                            style = MaterialTheme.typography.bodyMedium,
                            color = content,
                        )
                        result.scriptCode?.let {
                            Text("  ·  script $it", style = MaterialTheme.typography.bodyMedium, color = content)
                        }
                    }
                    SourceBadge(result.source, content)
                }

                is DetectionResult.EmptyInput ->
                    Text("Please enter some text first.", color = content)

                is DetectionResult.Unsupported ->
                    Text(
                        "Detected ${result.sarvamCode ?: "an unknown language"}, " +
                            "which is outside the six supported languages.",
                        color = content,
                    )

                is DetectionResult.Failure ->
                    Text(result.message, color = content)
            }
        }
    }
}

@Composable
private fun SourceBadge(source: DetectionSource, content: Color) {
    val label = when (source) {
        DetectionSource.ON_DEVICE -> "⚡ Resolved offline (on-device)"
        DetectionSource.CLOUD -> "☁ Resolved via Sarvam cloud"
    }
    Text(label, style = MaterialTheme.typography.labelMedium, color = content)
}

@Preview(showBackground = true)
@Composable
private fun PreviewSuccess() {
    LanguageDetectionScreen(
        state = LanguageDetectionUiState(
            input = "મારું નામ શું છે",
            result = DetectionResult.Success(IndicLanguage.GUJARATI, DetectionSource.ON_DEVICE, "Gujr"),
        ),
        onInputChange = {},
        onDetect = {},
    )
}
