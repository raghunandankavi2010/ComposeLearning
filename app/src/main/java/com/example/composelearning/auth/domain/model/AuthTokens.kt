package com.example.composelearning.auth.domain.model

/**
 * The credentials that identify a session, with expiry stored as **absolute** epoch millis so
 * anyone (UI countdown, interceptor, pre-emptive refresh) can reason about "is this still valid?"
 * without knowing when it was issued.
 *
 * The access token is what every protected request carries; it is short-lived by design. The
 * refresh token is longer-lived and its only job is to mint a new access token when the old one dies.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAtMillis: Long,
    val refreshExpiresAtMillis: Long,
) {
    fun isAccessExpired(nowMillis: Long): Boolean = nowMillis >= accessExpiresAtMillis
}
