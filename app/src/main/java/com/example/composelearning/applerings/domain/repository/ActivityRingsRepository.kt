package com.example.composelearning.applerings.domain.repository

import com.example.composelearning.applerings.domain.model.RingSpec

interface ActivityRingsRepository {
    suspend fun getRings(): List<RingSpec>
}
