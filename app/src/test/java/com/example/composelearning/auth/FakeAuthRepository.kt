package com.example.composelearning.auth

import com.example.composelearning.auth.domain.AuthRepository
import com.example.composelearning.auth.domain.model.AuthTokens
import com.example.composelearning.auth.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake standing in for the whole data layer. Because [AuthRepository] is the domain
 * boundary, the ViewModel + use cases can be tested with zero Retrofit/OkHttp/Android involved.
 */
class FakeAuthRepository(
    private val profile: UserProfile = UserProfile("demo", "Demo User", "demo@example.com", 0L),
    var failWith: Throwable? = null,
) : AuthRepository {

    private val _tokens = MutableStateFlow<AuthTokens?>(null)
    override val tokens: StateFlow<AuthTokens?> = _tokens.asStateFlow()

    var loginCount = 0
    var profileCount = 0
    var refreshCount = 0
    var forceExpireCount = 0
    var logoutCount = 0

    private fun pair(seed: String) = AuthTokens("at_$seed", "rt_$seed", 30_000L, 3_600_000L)

    override suspend fun login(username: String, password: String) {
        failWith?.let { throw it }
        loginCount++
        _tokens.value = pair("login$loginCount")
    }

    override suspend fun getProfile(): UserProfile {
        failWith?.let { throw it }
        profileCount++
        return profile
    }

    override suspend fun refresh() {
        failWith?.let { throw it }
        refreshCount++
        _tokens.value = pair("refresh$refreshCount")
    }

    override suspend fun forceExpireAccessToken() {
        failWith?.let { throw it }
        forceExpireCount++
        _tokens.value = _tokens.value?.copy(accessExpiresAtMillis = 0L)
    }

    override suspend fun logout() {
        logoutCount++
        _tokens.value = null
    }
}
