/*
 * Copyright 2024 Raghunandan Kavi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composelearning.permissions

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import kotlinx.coroutines.launch

/**
 * A sample screen demonstrating the Passkey (FIDO2) flow in Android using Credential Manager.
 *
 * Note: For a real app, you would need to communicate with a server (like the provided passkey_server.py)
 * to get challenges and verify responses. This sample mocks the network calls for UI demonstration.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasskeySample(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    var statusMessage by remember { mutableStateOf("Ready to secure your account.") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passkeys Demo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Passwordless Future",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (statusMessage.contains("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            registerPasskey(context, credentialManager) { msg -> statusMessage = msg }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register a Passkey")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            signInWithPasskey(context, credentialManager) { msg -> statusMessage = msg }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign in with Passkey")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            PasskeyInfoCard()
        }
    }
}

/**
 * Step 1: Registration
 */
private suspend fun registerPasskey(
    context: Context,
    credentialManager: CredentialManager,
    onStatus: (String) -> Unit
) {
    try {
        // In a real app, 'requestJson' comes from your server's /register/options endpoint
        val requestJson = """
            {
                "challenge": "aGVsbG93b3JsZA==",
                "rp": { "name": "Compose Learning", "id": "localhost" },
                "user": { "id": "YWRtaW4=", "name": "user@example.com", "displayName": "Raghunandan" },
                "pubKeyCredParams": [{ "alg": -7, "type": "public-key" }],
                "timeout": 60000,
                "attestation": "none"
            }
        """.trimIndent()

        val createRequest = CreatePublicKeyCredentialRequest(requestJson)

        onStatus("Opening System Prompt...")
        val result = credentialManager.createCredential(context, createRequest)

        // At this point, 'result.registrationResponseJson' contains the public key to send to your server
        onStatus("Passkey Registered Successfully!")
    } catch (e: CreateCredentialException) {
        onStatus("Error: ${e.message}")
    }
}

/**
 * Step 2: Sign In
 */
private suspend fun signInWithPasskey(
    context: Context,
    credentialManager: CredentialManager,
    onStatus: (String) -> Unit
) {
    try {
        // In a real app, 'requestJson' comes from your server's /login/options endpoint
        val requestJson = """
            {
                "challenge": "c2lnbmluY2hhbGxlbmdl",
                "timeout": 60000,
                "rpId": "localhost",
                "userVerification": "required"
            }
        """.trimIndent()

        val getRequest = GetCredentialRequest(
            listOf(GetPublicKeyCredentialOption(requestJson))
        )

        onStatus("Opening System Prompt...")
        val result = credentialManager.getCredential(context, getRequest)

        // 'result.credential' contains the signature to verify on your server
        onStatus("Logged in with Passkey!")
    } catch (e: GetCredentialException) {
        onStatus("Error: ${e.message}")
    }
}

@Composable
fun PasskeyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How it works (The Math)", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Public Key Cryptography: When you register, a Private Key is created on your device and a Public Key is sent to the server.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "2. Challenge: During login, the server sends a 'challenge'. Your device signs this challenge with your Private Key.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "3. No Password Sent: Your password (or biometric data) never leaves your device. The server only sees a digital signature.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PasskeySamplePreview() {
    MaterialTheme {
        PasskeySample(onBack = {})
    }
}
