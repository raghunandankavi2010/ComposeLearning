package com.example.composelearning.auth.data

import com.example.composelearning.auth.domain.TokenStorage
import com.example.composelearning.auth.domain.model.AuthTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tokens held only in RAM — restart the app and you're logged out. That is intentional for a demo:
 * it keeps the flow obvious and leaks nothing. A production impl would back this with the encrypted
 * DataStore / Keystore, but the interface (and every caller) would stay identical.
 *
 * [MutableStateFlow] is thread-safe, so [current] (OkHttp threads) and [tokens] (main thread) can
 * be read/written concurrently without extra locking.
 */
class InMemoryTokenStorage : TokenStorage {

    private val _tokens = MutableStateFlow<AuthTokens?>(null)
    override val tokens: StateFlow<AuthTokens?> = _tokens.asStateFlow()

    override fun current(): AuthTokens? = _tokens.value

    override fun save(tokens: AuthTokens) {
        _tokens.value = tokens
    }

    override fun clear() {
        _tokens.value = null
    }
}
