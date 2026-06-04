package com.example.composelearning.breathing.domain.usecase

import com.example.composelearning.breathing.domain.model.BreathingSession
import com.example.composelearning.breathing.domain.repository.BreathingRepository

class GetBreathingSessionUseCase(private val repository: BreathingRepository) {
    suspend operator fun invoke(): BreathingSession = repository.getSession()
}
