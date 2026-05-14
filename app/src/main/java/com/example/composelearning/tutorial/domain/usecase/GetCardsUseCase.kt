package com.example.composelearning.tutorial.domain.usecase

import com.example.composelearning.tutorial.domain.model.FeatureCard
import com.example.composelearning.tutorial.domain.repository.CardsRepository
import kotlinx.coroutines.flow.Flow

class GetCardsUseCase(
    private val repository: CardsRepository,
) {
    operator fun invoke(): Flow<List<FeatureCard>> = repository.observeCards()
}