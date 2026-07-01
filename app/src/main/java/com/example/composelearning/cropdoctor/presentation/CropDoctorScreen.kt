package com.example.composelearning.cropdoctor.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.composelearning.cropdoctor.domain.model.Diagnosis
import com.example.composelearning.cropdoctor.domain.model.DiseaseInfo
import com.example.composelearning.cropdoctor.domain.model.Prediction
import com.example.composelearning.cropdoctor.domain.model.Severity
import kotlin.math.roundToInt

private val HealthyGreen = Color(0xFF2E7D32)
private val SeverityLow = Color(0xFF558B2F)
private val SeverityModerate = Color(0xFFEF6C00)
private val SeverityHigh = Color(0xFFC62828)

private fun severityColor(severity: Severity): Color = when (severity) {
    Severity.NONE -> HealthyGreen
    Severity.LOW -> SeverityLow
    Severity.MODERATE -> SeverityModerate
    Severity.HIGH -> SeverityHigh
}

private fun severityLabel(severity: Severity): String = when (severity) {
    Severity.NONE -> "Healthy"
    Severity.LOW -> "Low risk"
    Severity.MODERATE -> "Needs attention"
    Severity.HIGH -> "Act now"
}

/**
 * Farmer-first Crop Doctor screen. Big, obvious actions; one clear answer per photo; plain-language
 * advice. Stateless — the [CropDoctorRoute] owns the ViewModel, image pickers and camera permission.
 */
@Composable
fun CropDoctorScreen(
    state: CropDoctorUiState,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Crop Doctor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Point your camera at one affected leaf, or pick a photo. " +
                    "You'll get the likely disease and what to do — all on your phone, no internet needed.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            state.modelError?.let { err ->
                InfoCard(
                    container = MaterialTheme.colorScheme.errorContainer,
                    onContainer = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text("The detection model could not load.", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(err, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // ── Big primary actions ───────────────────────────────────────────────
            BigActionButton(
                text = "Take Photo",
                icon = { Icon(Icons.Filled.PhotoCamera, null, Modifier.size(28.dp)) },
                enabled = state.isModelReady && !state.isAnalyzing,
                filled = true,
                onClick = onTakePhoto
            )
            Spacer(Modifier.height(12.dp))
            BigActionButton(
                text = "Choose from Gallery",
                icon = { Icon(Icons.Filled.PhotoLibrary, null, Modifier.size(28.dp)) },
                enabled = state.isModelReady && !state.isAnalyzing,
                filled = false,
                onClick = onPickGallery
            )

            if (!state.isModelReady && state.modelError == null) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text("  Getting the Crop Doctor ready…")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Selected photo + result ───────────────────────────────────────────
            state.imageUri?.let { uri ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected leaf photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.height(16.dp))
                        when {
                            state.isAnalyzing -> AnalyzingBlock()
                            state.imageError != null -> Text(
                                state.imageError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            state.diagnosis != null -> DiagnosisBlock(state.diagnosis)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Filled.Refresh, null)
                    Text("  Scan another leaf", fontSize = 16.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BigActionButton(
    text: String,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit
) {
    val modifier = Modifier.fillMaxWidth().height(64.dp)
    val shape = RoundedCornerShape(16.dp)
    if (filled) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier, shape = shape) {
            icon()
            Text("  $text", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier, shape = shape) {
            icon()
            Text("  $text", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AnalyzingBlock() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp))
        Text("  Checking the leaf…", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DiagnosisBlock(diagnosis: Diagnosis) {
    if (!diagnosis.isConfident) {
        NotSureBlock()
        return
    }
    val best = diagnosis.best
    val info = best.info
    val accent = severityColor(info.severity)

    // Headline: big icon + crop + condition + severity chip
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (info.isHealthy) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(info.crop, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                info.condition,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }

    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        SeverityChip(info.severity)
        Spacer(Modifier.width(8.dp))
        Text(
            "${(best.confidence * 100).roundToInt()}% match",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(16.dp))
    Text(info.summary, style = MaterialTheme.typography.bodyLarge)

    if (!info.isHealthy) {
        Section(title = "What to look for", body = info.symptoms)
    }

    Spacer(Modifier.height(16.dp))
    Text(
        if (info.isHealthy) "Keep it healthy" else "What to do",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
    info.actions.forEachIndexed { index, step ->
        StepRow(number = index + 1, text = step, accent = accent)
    }

    // Transparency: alternate possibilities
    if (diagnosis.alternates.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Other possibilities",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        diagnosis.alternates.forEach { alt -> AlternateRow(alt) }
    }

    Spacer(Modifier.height(8.dp))
    Text(
        "This is guidance to help you decide. For confirmation and approved local products, " +
            "check with your agriculture extension officer.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NotSureBlock() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.WarningAmber, null, tint = SeverityModerate, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(8.dp))
        Text("Not sure yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "The photo isn't clear enough to be confident. For the best result:",
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(8.dp))
    listOf(
        "Fill the frame with ONE leaf that shows the problem.",
        "Use good daylight and hold the phone steady.",
        "Avoid blur, shadows and busy backgrounds.",
    ).forEachIndexed { i, tip -> StepRow(number = i + 1, text = tip, accent = SeverityModerate) }
}

@Composable
private fun Section(title: String, body: String) {
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun StepRow(number: Int, text: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(accent),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SeverityChip(severity: Severity) {
    val color = severityColor(severity)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(
            severityLabel(severity),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AlternateRow(prediction: Prediction) {
    val info: DiseaseInfo = prediction.info
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${info.crop} · ${info.condition}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "${(prediction.confidence * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoCard(
    container: Color,
    onContainer: Color,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}
