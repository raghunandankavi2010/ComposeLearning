package com.example.composelearning.tutorial.presentation

import com.example.composelearning.tutorial.domain.model.FeatureCard
import com.example.composelearning.tutorial.domain.model.TutorialStep

data class TutorialUiState(
    val cards: CardsState = CardsState.Loading,
    val tutorial: TutorialState = TutorialState.Idle,
) {
    sealed interface CardsState {
        data object Loading : CardsState
        data class Ready(val cards: List<FeatureCard>) : CardsState
    }

    sealed interface TutorialState {
        data object Idle : TutorialState
        data class Running(
            val steps: List<TutorialStep>,
            val currentIndex: Int,
        ) : TutorialState {
            val currentStep: TutorialStep get() = steps[currentIndex]
            val isLast: Boolean get() = currentIndex == steps.lastIndex
        }
    }
}