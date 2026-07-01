package com.example.composelearning.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composelearning.auth.domain.AuthEventLog
import com.example.composelearning.auth.domain.AuthRepository
import com.example.composelearning.auth.domain.model.UserProfile
import com.example.composelearning.auth.domain.usecase.ForceExpireAccessTokenUseCase
import com.example.composelearning.auth.domain.usecase.GetProfileUseCase
import com.example.composelearning.auth.domain.usecase.LoginUseCase
import com.example.composelearning.auth.domain.usecase.LogoutUseCase
import com.example.composelearning.auth.domain.usecase.RefreshTokensUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the auth demo. The token pair and the event log are *domain-owned* observable state, so
 * the UI reflects silent refreshes (which happen deep in the OkHttp stack) with no extra plumbing:
 * we simply [combine] the repository's tokens flow and the event log with a bit of screen-local
 * state (inputs, busy, error, last profile).
 *
 * Pure domain dependencies (use cases + two observable holders) — no Android types — so it unit-tests
 * trivially with fakes.
 */
class AuthViewModel(
    private val login: LoginUseCase,
    private val getProfile: GetProfileUseCase,
    private val refresh: RefreshTokensUseCase,
    private val forceExpire: ForceExpireAccessTokenUseCase,
    private val logout: LogoutUseCase,
    repository: AuthRepository,
    eventLog: AuthEventLog,
) : ViewModel() {

    /** State that lives only in the screen (not part of the domain session). */
    private data class Local(
        val username: String = "demo",
        val password: String = "password",
        val profile: UserProfile? = null,
        val isBusy: Boolean = false,
        val error: String? = null,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<AuthUiState> =
        combine(local, repository.tokens, eventLog.events) { l, tokens, events ->
            AuthUiState(
                usernameInput = l.username,
                passwordInput = l.password,
                tokens = tokens,
                profile = if (tokens == null) null else l.profile, // drop profile once logged out
                events = events,
                isBusy = l.isBusy,
                error = l.error,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthUiState())

    fun onUsernameChange(value: String) = local.update { it.copy(username = value) }
    fun onPasswordChange(value: String) = local.update { it.copy(password = value) }
    fun onDismissError() = local.update { it.copy(error = null) }

    fun onLogin() = launch {
        login(local.value.username, local.value.password)
        local.update { it.copy(profile = null) }
    }

    fun onCallProfile() = launch {
        val profile = getProfile()
        local.update { it.copy(profile = profile) }
    }

    fun onManualRefresh() = launch { refresh() }

    fun onForceExpire() = launch { forceExpire() }

    fun onLogout() = launch {
        logout()
        local.update { it.copy(profile = null) }
    }

    /** Runs [block], toggling busy and funneling any failure into [Local.error]. */
    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            local.update { it.copy(isBusy = true, error = null) }
            runCatching { block() }
                .onFailure { e -> local.update { it.copy(error = e.message ?: "Something went wrong") } }
            local.update { it.copy(isBusy = false) }
        }
    }
}
