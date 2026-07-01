package com.example.composelearning.auth.domain.usecase

import com.example.composelearning.auth.domain.AuthRepository
import com.example.composelearning.auth.domain.model.UserProfile

/**
 * One thin use case per user intent. They add no logic today, but they keep the ViewModel
 * depending on *verbs* rather than on the whole repository — the seam where real apps grow
 * validation, analytics or caching without touching UI or data code.
 */

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String) =
        repository.login(username.trim(), password)
}

class GetProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): UserProfile = repository.getProfile()
}

class RefreshTokensUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.refresh()
}

class ForceExpireAccessTokenUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.forceExpireAccessToken()
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
