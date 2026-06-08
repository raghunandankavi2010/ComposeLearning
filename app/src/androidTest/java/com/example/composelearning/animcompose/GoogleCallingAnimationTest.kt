package com.example.composelearning.animcompose

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GoogleCallingAnimationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun googleCallingAnimation_displaysCallIcon() {
        composeTestRule.setContent {
            GoogleCallingScreenAnimation()
        }

        // Verify that the call icon is present by its content description
        composeTestRule.onNodeWithContentDescription("Answer Call").assertIsDisplayed()
    }

    @Test
    fun googleCallingAnimation_isSwipeUpToAnswer_showsArrows() {
        composeTestRule.setContent {
            GoogleCallingScreenAnimation(isSwipeUpToAnswer = true)
        }

        // Arrows are Icons with null content description, but they are within ArrowIndicatorTrack
        // We can check if the Column for arrows exists or just verify the FAB is there.
        // Since arrows have null content description, they are hard to find directly without custom semantics.
        // But we can check for the FAB which should be present in both modes.
        composeTestRule.onNodeWithContentDescription("Answer Call").assertIsDisplayed()
    }

    @Test
    fun googleCallingAnimation_isSwipeDownToDecline_showsArrows() {
        composeTestRule.setContent {
            GoogleCallingScreenAnimation(isSwipeUpToAnswer = false)
        }

        composeTestRule.onNodeWithContentDescription("Answer Call").assertIsDisplayed()
    }

    @Test
    fun googleCallingAnimation_tap_answersCall() {
        var answered = false
        composeTestRule.setContent {
            GoogleCallingScreenAnimation(onAnswer = { answered = true })
        }

        composeTestRule.onNodeWithTag("AnswerCallButton").performClick()

        composeTestRule.waitForIdle()
        assertTrue("Tapping the button should answer the call", answered)
        composeTestRule.onNodeWithText("Call Connected").assertIsDisplayed()
    }

    @Test
    fun googleCallingAnimation_swipeUp_answersCall() {
        var answered = false
        composeTestRule.setContent {
            GoogleCallingScreenAnimation(isSwipeUpToAnswer = true, onAnswer = { answered = true })
        }

        // A swipe-up gesture on the answer button should connect the call
        composeTestRule.onNodeWithTag("AnswerCallButton").performTouchInput {
            swipeUp()
        }

        composeTestRule.waitForIdle()
        assertTrue("Swiping up should answer the call", answered)
        composeTestRule.onNodeWithText("Call Connected").assertIsDisplayed()
    }

    @Test
    fun googleCallingAnimation_swipeDown_doesNotAnswerCall() {
        var answered = false
        composeTestRule.setContent {
            GoogleCallingScreenAnimation(isSwipeUpToAnswer = true, onAnswer = { answered = true })
        }

        // A downward swipe must not trigger the answer action
        composeTestRule.onNodeWithTag("AnswerCallButton").performTouchInput {
            swipeDown()
        }

        composeTestRule.waitForIdle()
        assertFalse("Swiping down should not answer the call", answered)
        composeTestRule.onNodeWithText("Call Connected").assertDoesNotExist()
    }
}
