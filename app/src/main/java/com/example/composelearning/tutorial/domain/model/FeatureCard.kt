package com.example.composelearning.tutorial.domain.model

data class FeatureCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val variant: Variant,
) {
    enum class Variant {
        Stat,
        Hero,
        Action,
        Profile,
        TextOnly,
        Chart,
    }
}