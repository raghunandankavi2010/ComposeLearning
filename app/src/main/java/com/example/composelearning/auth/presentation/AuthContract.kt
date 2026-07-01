package com.example.composelearning.auth.presentation

import androidx.compose.runtime.Immutable
import com.example.composelearning.auth.domain.AuthEvent
import com.example.composelearning.auth.domain.model.AuthTokens
import com.example.composelearning.auth.domain.model.UserProfile

/** Immutable UI state for the auth-token demo screen (unidirectional data flow). */
@Immutable
data class AuthUiState(
    val usernameInput: String = "demo",
    val passwordInput: String = "password",
    val tokens: AuthTokens? = null,
    val profile: UserProfile? = null,
    val events: List<AuthEvent> = emptyList(),
    val isBusy: Boolean = false,
    val error: String? = null,
) {
    val isLoggedIn: Boolean get() = tokens != null
}
