package com.example.composelearning.auth.domain

import com.example.composelearning.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.StateFlow

/**
 * The single source of truth for the current session's tokens.
 *
 * Two access styles on purpose:
 *  • [tokens] — a [StateFlow] the UI observes reactively.
 *  • [current] — a *synchronous* read for the OkHttp interceptor/authenticator, which run on
 *    background threads outside any coroutine and must decide "which token do I attach?" instantly.
 *
 * Implementations must be thread-safe: the authenticator can mutate tokens from an OkHttp thread
 * while the ViewModel reads them from the main thread.
 */
interface TokenStorage {
    val tokens: StateFlow<AuthTokens?>

    fun current(): AuthTokens?

    fun save(tokens: AuthTokens)

    fun clear()
}
