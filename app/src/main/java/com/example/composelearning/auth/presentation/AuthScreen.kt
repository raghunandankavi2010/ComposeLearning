package com.example.composelearning.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composelearning.auth.domain.AuthEvent
import com.example.composelearning.auth.domain.model.AuthTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthRoute(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AuthScreen(
        state = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::onLogin,
        onCallProfile = viewModel::onCallProfile,
        onManualRefresh = viewModel::onManualRefresh,
        onForceExpire = viewModel::onForceExpire,
        onLogout = viewModel::onLogout,
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onCallProfile: () -> Unit,
    onManualRefresh: () -> Unit,
    onForceExpire: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header()

        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        state.error?.let { ErrorBanner(it) }

        if (!state.isLoggedIn) {
            LoginCard(
                username = state.usernameInput,
                password = state.passwordInput,
                enabled = !state.isBusy,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onLogin = onLogin,
            )
        } else {
            TokensCard(state.tokens!!)
            SessionActions(
                enabled = !state.isBusy,
                onCallProfile = onCallProfile,
                onManualRefresh = onManualRefresh,
                onForceExpire = onForceExpire,
                onLogout = onLogout,
            )
            state.profile?.let { ProfileCard(it.username, it.fullName, it.email) }
        }

        EventLogCard(state.events)
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Access & Refresh Tokens",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Log in for a short-lived access token (30s) + a refresh token. Calling /profile " +
                "after it expires triggers a silent refresh in the OkHttp Authenticator — watch the " +
                "log below. Start the backend first: ./gradlew :auth-server:run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun LoginCard(
    username: String,
    password: String,
    enabled: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Sign in", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Username") },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                enabled = enabled,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onLogin, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text("Log in")
            }
            Text(
                "Demo credentials: demo / password",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TokensCard(tokens: AuthTokens) {
    // Tick once a second so the access-token countdown stays live.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val secondsLeft = ((tokens.accessExpiresAtMillis - now) / 1000).coerceAtLeast(0)
    val expired = tokens.isAccessExpired(now)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Current session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            TokenRow("Access token", tokens.accessToken)
            Text(
                text = if (expired) {
                    "⚠ Access token EXPIRED — the next /profile call will auto-refresh it."
                } else {
                    "⏳ Access token valid for ${secondsLeft}s"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )

            TokenRow("Refresh token", tokens.refreshToken)
        }
    }
}

@Composable
private fun TokenRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value.truncateToken(),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun SessionActions(
    enabled: Boolean,
    onCallProfile: () -> Unit,
    onManualRefresh: () -> Unit,
    onForceExpire: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onCallProfile, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("Call protected /profile")
        }
        // The demo shortcut: expire the access token now, then hit /profile to see the auto-refresh.
        OutlinedButton(onClick = onForceExpire, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text("Force-expire access token (demo)")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onManualRefresh, enabled = enabled, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Refresh")
            }
            OutlinedButton(onClick = onLogout, enabled = enabled, modifier = Modifier.weight(1f)) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun ProfileCard(username: String, fullName: String, email: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Protected data ✓", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(fullName, style = MaterialTheme.typography.bodyLarge)
            Text("@$username", style = MaterialTheme.typography.bodyMedium)
            Text(email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EventLogCard(events: List<AuthEvent>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Flow log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${events.size} events",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = events.isEmpty()) {
                Text(
                    "Actions you take will be traced here — including the silent 401 → refresh → retry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Newest first so the latest step is always visible without scrolling.
                events.asReversed().forEach { event ->
                    Text(
                        text = event.message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

/** Keep the ends (which differ between tokens) but hide the noisy middle. */
private fun String.truncateToken(): String =
    if (length <= 18) this else "${take(11)}…${takeLast(4)}"
