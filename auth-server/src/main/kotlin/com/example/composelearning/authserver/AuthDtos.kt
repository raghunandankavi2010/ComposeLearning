package com.example.composelearning.authserver

import kotlinx.serialization.Serializable

/**
 * Wire contracts shared (by convention, not by module) with the Android client's DTOs.
 * Everything is plain JSON so the flow is easy to inspect with curl / Logcat.
 */

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

/**
 * The token pair returned by /login and /refresh. TTLs are sent to the client so it can
 * pre-emptively refresh (or just show a countdown) without hard-coding server policy.
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val accessExpiresInSeconds: Long,
    val refreshExpiresInSeconds: Long,
)

@Serializable
data class ProfileResponse(
    val username: String,
    val fullName: String,
    val email: String,
    /** Echoed back so the client can prove the call reached a live, authenticated endpoint. */
    val serverTimeMillis: Long,
)

/** Uniform error envelope. [reason] is a machine-readable code the client logs/branches on. */
@Serializable
data class ErrorResponse(
    val error: String,
    val reason: String,
)
