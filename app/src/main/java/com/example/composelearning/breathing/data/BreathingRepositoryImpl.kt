package com.example.composelearning.breathing.data

import com.example.composelearning.breathing.domain.model.BreathingSession
import com.example.composelearning.breathing.domain.repository.BreathingRepository

/** Palette + copy from the original Headspace demo. */
class BreathingRepositoryImpl : BreathingRepository {
    override suspend fun getSession(): BreathingSession = BreathingSession(
        title = "Following the breath",
        baseColor = 0xFF60D1B9,
        wave1 = 0xFF2AB8AA,
        wave2 = 0xFF3A9DBB,
        wave3 = 0xFF2A7FB8,
        startLabel = "0:25",
        endLabel = "3:16"
    )
}
