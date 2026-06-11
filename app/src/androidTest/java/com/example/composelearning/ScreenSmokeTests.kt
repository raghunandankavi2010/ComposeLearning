package com.example.composelearning

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenSmokeTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val categories = listOf(
        "Particle Hub",
        "Charts & Waves Hub",
        "Canvas Basics Hub",
        "Nav3 — Tabs + Shared Elements",
        "Top-Right Fan Carousel",
        "FlightSeat (Compose port)",
        "SmoothProgressBar (Compose port)",
        "Per-item ViewModels (Compose)",
        "Strava Save Activity",
        "Zoomable Image",
        "Image Processing (AGSL)",
        "Sort Animations",
        "Netflix — Shape redraw",
        "Netflix — Paint redraw (Anmol port)",
        "Calendar Picker",
        "Biometric Animation",
        "Button Animation",
        "Blur Effects",
        "Lists Showcase",
        "Pager & Carousel Showcase",
        "Percentage Layout",
        "Path Progress",
        "Marquee Text",
        "Overlapping Images",
        "Time Range Knob",
        "Shadow Playground",
        "Tutorial Overlay",
        "AGSL Shader Demos",
        "Product Shared Elements",
        "Chat App Navigation",
        "Fluid Tab Bar",
        "3D Card Flip",
        "Sensor Reactive Card",
        "Spotlight Walkthrough",
        "Text Shimmer Effects",
        "Circular Menu",
        "YouTube Style Screen",
        "Staggered Grid Animation",
        "Circular Reveal",
        "Draggable Side Sheet",
        "Pulsating Circles",
        "Multi-Color Progress",
        "Gradient Progress Bar",
        "Bouncing Ball",
        "Animated Balance Counter",
        "Stacked Tinder Cards",
        "April 2026 Updates",
        "Animation Basics",
        "Value-Based Animations",
        "Transition Animations",
        "Physics Animations",
        "Physics Game",
        "Bottle Wave Animation",
        "Date Picker",
        "File Delete animation",
        "Squiggly Spans Math",
        "Squiggly Slider"
    )

    @Test
    fun smokeTest_allScreensLoadWithoutCrashing() {
        categories.forEach { category ->
            // Scroll to the item and click it
            composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText(category))
            composeTestRule.onNodeWithText(category).performClick()

            // Wait for any animations and check if something is displayed
            // We just wait a bit to ensure no immediate crash
            composeTestRule.waitForIdle()

            // Go back to the home screen
            Espresso.pressBack()

            // Verify we're back at the home screen
            composeTestRule.onNodeWithText("Jetpack Compose Animations").assertIsDisplayed()
        }
    }
}
