package com.example.composelearning.auth.data

import com.example.composelearning.auth.data.remote.AuthApi
import com.example.composelearning.auth.data.remote.LoginRequestDto
import com.example.composelearning.auth.data.remote.LogoutRequestDto
import com.example.composelearning.auth.data.remote.RefreshRequestDto
import com.example.composelearning.auth.data.remote.toDomain
import com.example.composelearning.auth.domain.AuthEventLog
import com.example.composelearning.auth.domain.AuthRepository
import com.example.composelearning.auth.domain.TokenStorage
import com.example.composelearning.auth.domain.model.AuthTokens
import com.example.composelearning.auth.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Coordinates the API, the token store and the event log. Note what is *absent*: there is no
 * 401-handling here. Attaching the token and refreshing it are the OkHttp layer's job
 * ([AccessTokenInterceptor] + [TokenAuthenticator]), so this reads like plain, happy-path code —
 * which is exactly the point of putting the token machinery in the network stack.
 */
class AuthRepositoryImpl(
    private val api: AuthApi,
    private val storage: TokenStorage,
    private val eventLog: AuthEventLog,
    private val now: () -> Long = System::currentTimeMillis,
) : AuthRepository {

    override val tokens: StateFlow<AuthTokens?> = storage.tokens

    override suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) {
        eventLog.emit("→ POST /login as \"$username\"")
        val dto = api.login(LoginRequestDto(username, password))
        storage.save(dto.toDomain(now()))
        eventLog.emit("✅ Login OK — access + refresh tokens stored")
    }

    override suspend fun getProfile(): UserProfile = withContext(Dispatchers.IO) {
        eventLog.emit("→ GET /profile (access token attached automatically)")
        val profile = api.profile().toDomain()
        eventLog.emit("✅ /profile returned data for ${profile.username}")
        profile
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val current = storage.current() ?: error("Not logged in")
        eventLog.emit("→ POST /refresh (manual rotation)")
        val dto = api.refresh(RefreshRequestDto(current.refreshToken))
        storage.save(dto.toDomain(now()))
        eventLog.emit("✅ Tokens rotated — old refresh token is now invalid")
    }

    override suspend fun forceExpireAccessToken() = withContext(Dispatchers.IO) {
        val current = storage.current() ?: error("Not logged in")
        eventLog.emit("🧪 Asking server to expire the current access token…")
        api.forceExpire()
        // Mirror it locally so the on-screen countdown flips to EXPIRED immediately.
        storage.save(current.copy(accessExpiresAtMillis = now()))
        eventLog.emit("🧪 Access token expired — the next /profile call will auto-refresh")
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        storage.current()?.let { runCatching { api.logout(LogoutRequestDto(it.refreshToken)) } }
        storage.clear()
        eventLog.emit("👋 Logged out — server revoked refresh token, local session cleared")
    }
}
