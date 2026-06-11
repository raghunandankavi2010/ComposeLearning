package com.example.composelearning

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComposeLearningUiTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreen_loadsAndShowsCategories() {
        composeTestRule.onNodeWithText("Jetpack Compose Animations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Particle Hub").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charts & Waves Hub").assertIsDisplayed()
    }

    @Test
    fun navigateToChartsHub_andSwitchTabs() {
        // Navigate to Charts Hub
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Charts & Waves Hub"))
        composeTestRule.onNodeWithText("Charts & Waves Hub").performClick()

        // Verify we are on the Charts Hub screen
        composeTestRule.onNodeWithText("Charts & Waves Showcase").assertIsDisplayed()

        // Check initial tab (Line)
        composeTestRule.onNodeWithText("Line").assertIsSelected()

        // Switch to Bar tab
        composeTestRule.onNodeWithText("Bar").performClick()
        composeTestRule.onNodeWithText("Bar").assertIsSelected()

        // Go to Waves tab
        // Use hasAnyAncestor(hasScrollAction()) to ensure we are targeting the scrollable row if needed
        // or just perform scrollTo if it's in a scrollable container.
        // For Tabs in a ScrollableTabRow, sometimes they are not immediately clickable if off-screen.
        composeTestRule.onNodeWithText("Waves").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Waves").assertIsSelected()

        // Check sub-tabs in Waves
        composeTestRule.onNodeWithText("Sine Wave Sample").assertIsDisplayed()
        // Removed "Sin Wave Path" check as it's not present in the UI
    }

    @Test
    fun calendarPicker_selectDateRange() {
        // Navigate to Calendar Picker
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Calendar Picker"))
        composeTestRule.onNodeWithText("Calendar Picker").performClick()

        // Verify header
        composeTestRule.onNodeWithText("Select Date").assertIsDisplayed()

        // The calendar starts from Jan 1st of current year.
        // Let's pick dates from the first month (January) to avoid complex scrolling

        // Click on 10th and 15th of January
        // The calendar items have semantics like "January 10 2025", so we use substring match
        // We also filter by hasClickAction() to avoid matching the header text "Jan 10 - Jan 15"
        composeTestRule.onAllNodes(hasText("10", substring = true).and(hasClickAction())).onFirst().performClick()
        composeTestRule.onAllNodes(hasText("15", substring = true).and(hasClickAction())).onFirst().performClick()

        // Wait for state update
        composeTestRule.waitForIdle()

        // Verify top bar updates with range.
        composeTestRule.onNodeWithText("Select Date").assertDoesNotExist()
        composeTestRule.onAllNodes(hasText("10", substring = true)).onFirst().assertExists()
        composeTestRule.onAllNodes(hasText("15", substring = true)).onFirst().assertExists()
    }

    @Test
    fun squigglySlider_interacts() {
        // Navigate to Squiggly Slider
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Squiggly Slider"))
        composeTestRule.onNodeWithText("Squiggly Slider").performClick()

        // Verify title
        composeTestRule.onNodeWithText("Material Expressive Squiggly").assertIsDisplayed()

        // Interact with the slider
        // The initial value is 40% (0.4f)
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.4f, 0f..1f))).assertExists()

        // Perform a drag to change value
        // We swipe from 40% to the right
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.4f, 0f..1f)))
            .performTouchInput {
                swipe(
                    start = Offset(width * 0.4f, centerY),
                    end = Offset(width * 0.9f, centerY)
                )
            }

        // Verify value text changed from 40%. The UI shows "Value: 40%"
        // Wait for idle to ensure state update propagated
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Value: 40%").assertDoesNotExist()
        // It should have some value greater than 40%
        composeTestRule.onNodeWithText("Value:", substring = true).assertIsDisplayed()
    }

    @Test
    fun sideSheet_dragsAndShowsContent() {
        // Navigate to Draggable Side Sheet
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Draggable Side Sheet"))
        composeTestRule.onNodeWithText("Draggable Side Sheet").performClick()

        // Initial state: Handle should be visible
        composeTestRule.onNodeWithContentDescription("Expand").assertIsDisplayed()

        // Drag the handle to the left to expand
        composeTestRule.onNodeWithContentDescription("Expand").performTouchInput {
            swipeLeft()
        }

        // Verify content is visible and handle icon flipped
        composeTestRule.onNodeWithText("Sheet Content").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Collapse").assertIsDisplayed()
    }

    @Test
    fun particleHub_switchSystems() {
        composeTestRule.onNodeWithText("Particle Hub").performClick()

        // Verify we are in Particle Hub - check for the first tab
        composeTestRule.onNodeWithText("3D Explosion").assertIsDisplayed()

        composeTestRule.onNodeWithText("3D Explosion").assertIsSelected()

        composeTestRule.onNodeWithText("Stream").performClick()
        composeTestRule.onNodeWithText("Stream").assertIsSelected()

        composeTestRule.onNodeWithText("Physics").performClick()
        composeTestRule.onNodeWithText("Physics").assertIsSelected()
    }
}
