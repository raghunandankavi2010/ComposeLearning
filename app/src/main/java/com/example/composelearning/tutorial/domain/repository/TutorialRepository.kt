package com.example.composelearning.tutorial.domain.repository

import com.example.composelearning.tutorial.domain.model.TutorialStep

interface TutorialRepository {
    fun getSteps(): List<TutorialStep>
}