package com.example.composelearning.auth.domain

import com.example.composelearning.auth.domain.model.AuthTokens
import com.example.composelearning.auth.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * The domain boundary for authentication. The presentation layer talks only to this (via use
 * cases); the data layer backs it with Retrofit + OkHttp. Swapping the transport, or faking it in
 * tests, requires zero changes above this line.
 */
interface AuthRepository {

    /** The live session, mirrored from [TokenStorage]; `null` means logged out. */
    val tokens: StateFlow<AuthTokens?>

    /** Exchange credentials for a token pair and persist it. @throws on bad credentials/network. */
    suspend fun login(username: String, password: String)

    /** Call the protected endpoint. The access token is attached (and silently refreshed) for you. */
    suspend fun getProfile(): UserProfile

    /** Manually rotate the token pair using the refresh token (the auto path does this on 401). */
    suspend fun refresh()

    /**
     * Demo-only: ask the server to expire the current access token now, so the very next
     * [getProfile] call exercises the silent 401 → refresh → retry path without waiting out the TTL.
     */
    suspend fun forceExpireAccessToken()

    /** Revoke the refresh token server-side (best effort) and clear the local session. */
    suspend fun logout()
}
