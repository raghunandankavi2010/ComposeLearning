package com.example.composelearning.tutorial.domain.usecase

import com.example.composelearning.tutorial.domain.model.TutorialStep
import com.example.composelearning.tutorial.domain.repository.TutorialRepository

class GetTutorialStepsUseCase(
    private val repository: TutorialRepository,
) {
    operator fun invoke(): List<TutorialStep> = repository.getSteps()
}