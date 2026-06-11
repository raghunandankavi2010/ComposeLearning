package com.example.composelearning.protobufdemo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Default host. With `adb reverse tcp:8080 tcp:8080` the device's own localhost
 * tunnels to the desktop server — the most reliable path on emulators where the
 * 10.0.2.2 NAT route is blocked/timing out. (Use http://10.0.2.2:8080 instead
 * if you are NOT using adb reverse.)
 */
private const val DEFAULT_SERVER_URL = "http://localhost:8080"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtobufDemoRoute(
    onBack: () -> Unit,
    viewModel: ProtobufContactsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var serverUrl by rememberSaveable { mutableStateOf(DEFAULT_SERVER_URL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protobuf over HTTP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        ProtobufDemoScreen(
            uiState = uiState,
            serverUrl = serverUrl,
            onServerUrlChange = { serverUrl = it },
            onFetch = { viewModel.load(serverUrl) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun ProtobufDemoScreen(
    uiState: ContactsUiState,
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    onFetch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Run the desktop server with ./gradlew :server:run, then fetch. " +
                "The list below is decoded from raw protobuf bytes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            label = { Text("Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onFetch,
            enabled = uiState !is ContactsUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch contacts")
        }

        when (uiState) {
            ContactsUiState.Idle -> EmptyHint()
            ContactsUiState.Loading -> LoadingState()
            is ContactsUiState.Error -> ErrorState(uiState.message)
            is ContactsUiState.Success -> SuccessState(uiState)
        }
    }
}

@Composable
private fun EmptyHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Tap \"Fetch contacts\" to load the list over protobuf.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Couldn't load contacts", style = MaterialTheme.typography.titleMedium)
            Text(message, color = MaterialTheme.colorScheme.error)
            Text(
                "Is the server running? Try ./gradlew :server:run on your machine.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessState(state: ContactsUiState.Success) {
    Text(
        text = "${state.contacts.size} contacts decoded from ${state.payloadBytes} protobuf bytes",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.contacts, key = { it.id }) { contact ->
            ContactRow(contact)
        }
    }
}

@Composable
private fun ContactRow(contact: ContactUi) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(contact.name)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    contact.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(contact.role, style = MaterialTheme.typography.labelMedium)
                Text(
                    if (contact.active) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (contact.active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
        }
    }
}
