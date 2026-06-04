package com.example.composelearning.applerings.domain.usecase

import com.example.composelearning.applerings.domain.model.RingSpec
import com.example.composelearning.applerings.domain.repository.ActivityRingsRepository

class GetActivityRingsUseCase(private val repository: ActivityRingsRepository) {
    suspend operator fun invoke(): List<RingSpec> = repository.getRings()
}
