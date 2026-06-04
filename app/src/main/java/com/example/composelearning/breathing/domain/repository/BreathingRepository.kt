package com.example.composelearning.breathing.domain.repository

import com.example.composelearning.breathing.domain.model.BreathingSession

interface BreathingRepository {
    suspend fun getSession(): BreathingSession
}
