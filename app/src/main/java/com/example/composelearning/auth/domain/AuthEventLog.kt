package com.example.composelearning.auth.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** One human-readable step in the auth flow, timestamped so the UI can render a timeline. */
data class AuthEvent(val timeMillis: Long, val message: String)

/**
 * A shared, observable timeline of what the auth machinery is doing. The repository, the
 * request interceptor and the 401 [com.example.composelearning.auth.data.TokenAuthenticator]
 * all write to it, and the screen renders it — this is what makes the "silent refresh" visible.
 *
 * It is a plain domain object (no Android deps), injected as a Koin singleton so every layer
 * shares the same instance.
 */
class AuthEventLog(private val now: () -> Long = System::currentTimeMillis) {

    private val _events = MutableStateFlow<List<AuthEvent>>(emptyList())
    val events: StateFlow<List<AuthEvent>> = _events.asStateFlow()

    fun emit(message: String) {
        _events.update { (it + AuthEvent(now(), message)).takeLast(MAX_EVENTS) }
    }

    fun clear() = _events.update { emptyList() }

    private companion object {
        const val MAX_EVENTS = 100
    }
}
