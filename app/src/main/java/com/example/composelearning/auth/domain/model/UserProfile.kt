package com.example.composelearning.auth.domain.model

/** The payload returned by the protected /profile endpoint — proof a call was authenticated. */
data class UserProfile(
    val username: String,
    val fullName: String,
    val email: String,
    val serverTimeMillis: Long,
)
