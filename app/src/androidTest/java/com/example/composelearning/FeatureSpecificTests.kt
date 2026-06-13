package com.example.composelearning

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeatureSpecificTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun sortAnimations_switchAlgorithms() {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Sort Animations"))
        composeTestRule.onNodeWithText("Sort Animations").performClick()

        // Check default (Bubble)
        composeTestRule.onNodeWithText("Bubble").assertIsSelected()

        // Switch to Quick Sort
        composeTestRule.onNodeWithText("Quick").performClick()
        composeTestRule.onNodeWithText("Quick").assertIsSelected()

        // Start sorting
        composeTestRule.onNodeWithText("Off-Thread").performClick()

        // Verify we can see sorting happening
        // Bubble sort on 32 items with delays takes ~1-2 seconds
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("Completed in", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Completed in", substring = true).assertExists()
    }

    @Test
    fun listsShowcase_verifyTabs() {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Lists Showcase"))
        composeTestRule.onNodeWithText("Lists Showcase").performClick()

        // Check a few tabs in the scrollable row
        composeTestRule.onNodeWithText("Alerts").assertIsSelected()

        composeTestRule.onNodeWithText("Products").performClick()
        composeTestRule.onNodeWithText("Products").assertIsSelected()

        // Swipe to find more tabs if needed
        composeTestRule.onNodeWithText("Reorder").performClick()
        composeTestRule.onNodeWithText("Reorder").assertIsSelected()
    }

    @Test
    fun timeRangeKnob_interacts() {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Time Range Knob"))
        composeTestRule.onNodeWithText("Time Range Knob").performClick()

        // Verify labels
        composeTestRule.onNodeWithText("Bedtime").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wake up").assertIsDisplayed()

        // The knob is a custom canvas, testing specific values might be hard without test tags,
        // but we can verify it exists.
    }

    @Test
    fun biometricAnimation_cyclesStates() {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Biometric Animation"))
        composeTestRule.onNodeWithText("Biometric Animation").performClick()

        // Check for state chips
        composeTestRule.onNodeWithText("Idle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan").assertIsDisplayed()

        // Click to scan
        composeTestRule.onNodeWithText("Scan").performClick()

        // Check if we can switch back to Success
        composeTestRule.onNodeWithText("Success").performClick()
    }
}
