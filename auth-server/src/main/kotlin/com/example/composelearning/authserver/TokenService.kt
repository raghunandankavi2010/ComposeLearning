package com.example.composelearning.authserver

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Thrown by [TokenService] when a credential/token is rejected; carries a machine code for the client. */
class AuthException(val reason: String, message: String) : Exception(message)

/** A demo user record. In a real system the password would be a salted hash. */
private data class User(val username: String, val password: String, val fullName: String, val email: String)

/** An issued access token and when it expires (epoch millis). */
private data class AccessGrant(val username: String, val expiresAtMillis: Long)

/** An issued refresh token and when it expires (epoch millis). */
private data class RefreshGrant(val username: String, val expiresAtMillis: Long)

/**
 * The whole auth brain, deliberately in-memory so the demo is self-contained and restartable.
 *
 * Design choices that make the mechanism *visible*:
 *  • Access tokens are short-lived ([ACCESS_TTL_SECONDS]) so a protected call will realistically
 *    hit a 401 and trigger the client's silent refresh within a demo session.
 *  • Refresh tokens are **rotated**: every successful /refresh invalidates the presented token and
 *    issues a brand-new one. Re-using an old refresh token is rejected — the standard defense
 *    against refresh-token replay.
 *
 * Tokens here are opaque random strings (not real JWTs). The client treats them as opaque too, so
 * swapping in signed JWTs later would not change a single line of client code.
 */
class TokenService(
    private val accessTtlSeconds: Long = ACCESS_TTL_SECONDS,
    private val refreshTtlSeconds: Long = REFRESH_TTL_SECONDS,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val users = mapOf(
        "demo" to User("demo", "password", "Demo User", "demo@example.com"),
    )

    private val accessTokens = ConcurrentHashMap<String, AccessGrant>()
    private val refreshTokens = ConcurrentHashMap<String, RefreshGrant>()

    /** Verify credentials and mint a fresh token pair. @throws AuthException on bad credentials. */
    fun login(username: String, password: String): TokenResponse {
        val user = users[username]
        if (user == null || user.password != password) {
            throw AuthException("invalid_credentials", "Unknown username or wrong password")
        }
        return issuePair(user.username)
    }

    /** Rotate a valid refresh token into a new pair. @throws AuthException if it is unknown/expired. */
    fun refresh(refreshToken: String): TokenResponse {
        val grant = refreshTokens.remove(refreshToken) // remove == single-use rotation
            ?: throw AuthException("invalid_refresh_token", "Refresh token unknown or already used")
        if (now() >= grant.expiresAtMillis) {
            throw AuthException("refresh_token_expired", "Refresh token has expired — log in again")
        }
        return issuePair(grant.username)
    }

    /** Validate a bearer access token and return the owning username. @throws AuthException otherwise. */
    fun authenticate(accessToken: String?): String {
        if (accessToken.isNullOrBlank()) {
            throw AuthException("missing_access_token", "No bearer token supplied")
        }
        val grant = accessTokens[accessToken]
            ?: throw AuthException("invalid_access_token", "Access token not recognized")
        if (now() >= grant.expiresAtMillis) {
            accessTokens.remove(accessToken)
            throw AuthException("access_token_expired", "Access token has expired — refresh it")
        }
        return grant.username
    }

    /** Best-effort logout: drop the refresh token so it can never be rotated again. */
    fun logout(refreshToken: String) {
        refreshTokens.remove(refreshToken)
    }

    /**
     * Demo helper: back-date an access token so it is treated as expired *right now*, without
     * waiting out [ACCESS_TTL_SECONDS]. The next protected call with it gets `access_token_expired`,
     * which is exactly what drives the client's silent refresh. No-op if the token is unknown.
     */
    fun forceExpire(accessToken: String?) {
        if (accessToken.isNullOrBlank()) return
        accessTokens.computeIfPresent(accessToken) { _, grant -> grant.copy(expiresAtMillis = 0) }
    }

    fun profileOf(username: String): ProfileResponse {
        val user = users.getValue(username)
        return ProfileResponse(user.username, user.fullName, user.email, now())
    }

    private fun issuePair(username: String): TokenResponse {
        val access = "at_" + UUID.randomUUID().toString().replace("-", "")
        val refresh = "rt_" + UUID.randomUUID().toString().replace("-", "")
        accessTokens[access] = AccessGrant(username, now() + accessTtlSeconds * 1000)
        refreshTokens[refresh] = RefreshGrant(username, now() + refreshTtlSeconds * 1000)
        return TokenResponse(
            accessToken = access,
            refreshToken = refresh,
            accessExpiresInSeconds = accessTtlSeconds,
            refreshExpiresInSeconds = refreshTtlSeconds,
        )
    }

    companion object {
        /** Short on purpose so the auto-refresh path is exercised during a demo session. */
        const val ACCESS_TTL_SECONDS = 30L
        const val REFRESH_TTL_SECONDS = 60L * 60L // 1 hour
    }
}
