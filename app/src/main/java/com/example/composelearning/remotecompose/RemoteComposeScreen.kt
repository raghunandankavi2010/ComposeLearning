package com.example.composelearning.remotecompose

import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RemoteComposeBuffer
import androidx.compose.remote.player.compose.ExperimentalRemotePlayerApi
import androidx.compose.remote.player.compose.RemoteDocumentPlayer
import java.io.ByteArrayInputStream

/**
 * Default base URL. `localhost` works when you run `adb reverse tcp:8080 tcp:8080`
 * (the reliable path — the emulator's 10.0.2.2 host route is often NAT-blocked
 * and times out). On a setup where 10.0.2.2 works, just edit the field.
 */
private const val DEFAULT_BASE_URL = "http://localhost:8080"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteComposeRoute(
    onBack: () -> Unit,
    viewModel: RemoteUiViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var baseUrl by rememberSaveable { mutableStateOf(DEFAULT_BASE_URL) }
    var variant by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RemoteCompose (server-driven)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "The layout below is NOT in this app. The desktop server builds a " +
                    "RemoteCompose document, ships the bytes over HTTP, and the player " +
                    "renders them. Start it with ./gradlew :server:run",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Server base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Variant (the only thing the client chooses):", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0..3).forEach { v ->
                    FilterChip(
                        selected = variant == v,
                        onClick = { variant = v },
                        label = { Text("$v") }
                    )
                }
            }

            Button(
                onClick = { viewModel.load(baseUrl, variant) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch & render document")
            }

            Spacer(Modifier.height(4.dp))

            when (val s = state) {
                RemoteUiState.Idle -> HintCard("Pick a variant and tap fetch.")
                RemoteUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is RemoteUiState.Error -> HintCard(
                    "Couldn't load: ${s.message}\n\nIs the server running? " +
                        "Run ./gradlew :server:run on your machine.",
                    error = true
                )
                is RemoteUiState.Success -> RemoteDocument(s)
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalRemotePlayerApi::class)
@Composable
private fun RemoteDocument(success: RemoteUiState.Success) {
    // Turn the downloaded bytes into a CoreDocument and play it. This is the
    // entire client-side contract: bytes in, native Compose UI out.
    val document = remember(success) {
        val buffer = RemoteComposeBuffer.fromInputStream(ByteArrayInputStream(success.documentBytes))
        CoreDocument().apply { initFromBuffer(buffer) }
    }
    // The document reports its own size, but it can be 0 until the player has
    // painted once — fall back to the server's canvas size so the aspect ratio
    // is always valid (avoids an aspectRatio(NaN) crash on first composition).
    val docW = document.width.takeIf { it > 0 } ?: 400
    val docH = document.height.takeIf { it > 0 } ?: 620

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Rendered from ${success.documentBytes.size} bytes · variant ${success.variant}",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
        RemoteDocumentPlayer(
            document = document,
            documentWidth = docW,
            documentHeight = docH,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(docW.toFloat() / docH.toFloat())
        )
    }
}

@Composable
private fun HintCard(text: String, error: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
