package com.example.composelearning.tutorial.data

import com.example.composelearning.tutorial.domain.model.TutorialStep
import com.example.composelearning.tutorial.domain.repository.TutorialRepository

class TutorialRepositoryImpl : TutorialRepository {

    override fun getSteps(): List<TutorialStep> = STEPS

    private companion object {
        val STEPS: List<TutorialStep> = listOf(
            TutorialStep(
                id = "step_hero",
                title = "Your daily dashboard",
                description = "This card is your home base — recap of the day's highlights.",
                targetKey = "card_overview",
                cardIndex = 0,
            ),
            TutorialStep(
                id = "step_streak",
                title = "Track your streak",
                description = "Stay consistent. Tap the streak chip anytime to see history.",
                targetKey = "card_streak",
                cardIndex = 1,
            ),
            TutorialStep(
                id = "step_quick_action",
                title = "Quick actions live here",
                description = "Save, scan, and share without leaving the feed.",
                targetKey = "card_quick_action_button",
                cardIndex = 2,
            ),
            TutorialStep(
                id = "step_insights",
                title = "Weekly insights",
                description = "Get a digest of your focus and progress every week.",
                targetKey = "card_insights",
                cardIndex = 5,
            ),
            TutorialStep(
                id = "step_share",
                title = "Invite friends",
                description = "Tap here to share and earn rewards together.",
                targetKey = "card_share",
                cardIndex = 8,
            ),
            TutorialStep(
                id = "step_settings",
                title = "Personalize it all",
                description = "Open settings to fine-tune your feed — you're all set!",
                targetKey = "card_settings_tip",
                cardIndex = 9,
            ),
        )
    }
}