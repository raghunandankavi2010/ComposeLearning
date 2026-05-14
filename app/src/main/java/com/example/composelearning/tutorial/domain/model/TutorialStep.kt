package com.example.composelearning.tutorial.domain.model

data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val targetKey: String,
    val cardIndex: Int?,
    val shape: Shape = Shape.RoundedRect(20),
) {
    sealed interface Shape {
        data class RoundedRect(val cornerDp: Int) : Shape
    }
}