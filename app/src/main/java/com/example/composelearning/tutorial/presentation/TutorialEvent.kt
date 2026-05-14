package com.example.composelearning.tutorial.presentation

sealed interface TutorialEvent {
    data object Start : TutorialEvent
    data object Next : TutorialEvent
    data object Previous : TutorialEvent
    data object Skip : TutorialEvent
}