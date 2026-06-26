package com.example.composelearning.promotions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.composelearning.ui.theme.ComposeLearningTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [DealPromoSection], verifying the timer-driven UI states (Edge Case 3):
 * an active deal keeps "Buy Now" enabled, while an expired deal disables the button and flips the
 * copy to "Deal Ended" / "EXPIRED".
 */
class DealPromoSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Minimal in-memory [DealStore]; androidTest can't see the JVM test source set's fake. */
    private class InMemoryDealStore(initial: Long? = null) : DealStore {
        private val state = MutableStateFlow(initial)
        override val targetEndTime: Flow<Long?> = state
        override suspend fun saveTargetEndTime(timestamp: Long) {
            state.value = timestamp
        }
    }

    private fun viewModelFor(targetEndTimestamp: Long): DealTimerViewModel =
        DealTimerViewModel(
            dealStore = InMemoryDealStore(),
            initialTargetEndTimestamp = targetEndTimestamp,
            timeProvider = SystemTimeProvider()
        )

    @Test
    fun activeDeal_showsTimerAndEnablesBuyNow() {
        val vm = viewModelFor(targetEndTimestamp = System.currentTimeMillis() + 3_600_000L)

        composeTestRule.setContent {
            ComposeLearningTheme {
                // timerState is a WhileSubscribed flow; collecting it here starts the countdown.
                DealPromoSection(viewModel = vm, onBuyNowClick = {})
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Flash Sale!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy Now").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun expiredDeal_disablesBuyNowAndShowsDealEnded() {
        val vm = viewModelFor(targetEndTimestamp = System.currentTimeMillis() - 1_000L)

        composeTestRule.setContent {
            ComposeLearningTheme {
                DealPromoSection(viewModel = vm, onBuyNowClick = {})
            }
        }

        // Wait for the timer coroutine to resolve the expired state and the UI to recompose.
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithText("Deal Ended").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Deal Ended").assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithText("EXPIRED").assertIsDisplayed()
    }
}
