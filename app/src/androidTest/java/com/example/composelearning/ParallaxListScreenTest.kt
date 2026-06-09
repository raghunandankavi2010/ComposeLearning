package com.example.composelearning

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import com.example.composelearning.lists.ParallaxListScreen
import org.junit.Rule
import org.junit.Test

class ParallaxListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun parallaxList_InitialState_DisplaysHeaderAndItems() {
        composeTestRule.setContent {
            ParallaxListScreen()
        }

        // Check if the list is displayed
        composeTestRule.onNodeWithTag("parallax_list").assertIsDisplayed()

        // Check if the header and image are displayed
        composeTestRule.onNodeWithTag("parallax_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parallax_image").assertIsDisplayed()

        // Check for a few initial list items
        composeTestRule.onNodeWithText("List Item 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("List Item 5").assertIsDisplayed()
    }

    @Test
    fun parallaxList_Scrolling_ShowsSubsequentItems() {
        composeTestRule.setContent {
            ParallaxListScreen()
        }

        // Scroll to an item that is likely off-screen initially
        composeTestRule.onNodeWithTag("parallax_list")
            .performScrollToNode(hasText("List Item 25"))

        // Verify the item is now displayed
        composeTestRule.onNodeWithText("List Item 25").assertIsDisplayed()
    }

    @Test
    fun parallaxList_HeaderText_IsVisible() {
        composeTestRule.setContent {
            ParallaxListScreen()
        }

        // Verify the header text is displayed
        composeTestRule.onNodeWithText("Parallax Header").assertIsDisplayed()
    }
}
