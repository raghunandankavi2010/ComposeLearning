package com.example.composelearning.tutorial.data

import com.example.composelearning.tutorial.domain.model.FeatureCard
import com.example.composelearning.tutorial.domain.repository.CardsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CardsRepositoryImpl : CardsRepository {

    override fun observeCards(): Flow<List<FeatureCard>> = flow {
        emit(emptyList())
        delay(LOAD_DELAY_MS)
        emit(DUMMY_CARDS)
    }

    private companion object {
        const val LOAD_DELAY_MS = 600L

        val DUMMY_CARDS: List<FeatureCard> = listOf(
            FeatureCard(
                id = "card_overview",
                title = "Welcome back, Raghu",
                subtitle = "Here's a quick look at what's new today.",
                variant = FeatureCard.Variant.Hero,
            ),
            FeatureCard(
                id = "card_streak",
                title = "7-day streak",
                subtitle = "Keep going — you're on fire.",
                variant = FeatureCard.Variant.Stat,
            ),
            FeatureCard(
                id = "card_quick_action",
                title = "Quick actions",
                subtitle = "Scan, share or save in a tap.",
                variant = FeatureCard.Variant.Action,
            ),
            FeatureCard(
                id = "card_profile",
                title = "Your profile",
                subtitle = "Looking sharp today.",
                variant = FeatureCard.Variant.Profile,
            ),
            FeatureCard(
                id = "card_changelog",
                title = "What's new in v2.4",
                subtitle = "Faster lists, richer animations, smoother gestures across the board.",
                variant = FeatureCard.Variant.TextOnly,
            ),
            FeatureCard(
                id = "card_insights",
                title = "This week's insights",
                subtitle = "You logged 12 hours of focus time.",
                variant = FeatureCard.Variant.Chart,
            ),
            FeatureCard(
                id = "card_recommendations",
                title = "Recommended for you",
                subtitle = "Hand-picked based on your activity.",
                variant = FeatureCard.Variant.Hero,
            ),
            FeatureCard(
                id = "card_goals",
                title = "Today's goals",
                subtitle = "3 of 5 complete.",
                variant = FeatureCard.Variant.Stat,
            ),
            FeatureCard(
                id = "card_share",
                title = "Invite a friend",
                subtitle = "Share the love, earn rewards.",
                variant = FeatureCard.Variant.Action,
            ),
            FeatureCard(
                id = "card_settings_tip",
                title = "Personalize your feed",
                subtitle = "Open settings to fine-tune what you see.",
                variant = FeatureCard.Variant.TextOnly,
            ),
        )
    }
}