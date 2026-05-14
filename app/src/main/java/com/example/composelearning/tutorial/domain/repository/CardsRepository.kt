package com.example.composelearning.tutorial.domain.repository

import com.example.composelearning.tutorial.domain.model.FeatureCard
import kotlinx.coroutines.flow.Flow

interface CardsRepository {
    fun observeCards(): Flow<List<FeatureCard>>
}