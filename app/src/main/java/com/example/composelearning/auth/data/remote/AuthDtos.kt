package com.example.composelearning.auth.data.remote

import com.example.composelearning.auth.domain.model.AuthTokens
import com.example.composelearning.auth.domain.model.UserProfile
import kotlinx.serialization.Serializable

/**
 * Wire models — the exact JSON the `:auth-server` speaks. Kept separate from the domain models so
 * the server's shape can change without rippling into the app; the mappers below are the only bridge.
 */

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class LogoutRequestDto(val refreshToken: String)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val accessExpiresInSeconds: Long,
    val refreshExpiresInSeconds: Long,
)

@Serializable
data class ProfileResponseDto(
    val username: String,
    val fullName: String,
    val email: String,
    val serverTimeMillis: Long,
)

/** Server's relative TTLs -> absolute expiry instants the rest of the app reasons about. */
fun TokenResponseDto.toDomain(nowMillis: Long): AuthTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresAtMillis = nowMillis + accessExpiresInSeconds * 1000,
    refreshExpiresAtMillis = nowMillis + refreshExpiresInSeconds * 1000,
)

fun ProfileResponseDto.toDomain(): UserProfile =
    UserProfile(username = username, fullName = fullName, email = email, serverTimeMillis = serverTimeMillis)
