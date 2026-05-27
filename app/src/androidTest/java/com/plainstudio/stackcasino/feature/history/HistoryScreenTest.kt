package com.plainstudio.stackcasino.feature.history

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_summary_and_every_round() {
        composeRule.setContent {
            StackcasinoTheme {
                HistoryScreen(data = historyPreviewData(), onOpenRound = {})
            }
        }

        composeRule.onNodeWithText("Game History").assertIsDisplayed()
        composeRule.onNodeWithText("128").assertIsDisplayed()
        composeRule.onNodeWithText("54%").assertIsDisplayed()
        composeRule.onNodeWithText("Crash").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Coinflip").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun selecting_wins_filter_hides_losing_rounds() {
        composeRule.setContent {
            StackcasinoTheme {
                HistoryScreen(data = historyPreviewData(), onOpenRound = {})
            }
        }

        composeRule.onNodeWithText("WINS").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Crash").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Roulette").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Mines").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Blackjack").assertCountEquals(0)
        composeRule.onAllNodesWithText("Coinflip").assertCountEquals(0)
    }

    @Test
    fun selecting_a_game_filter_narrows_to_that_game() {
        composeRule.setContent {
            StackcasinoTheme {
                HistoryScreen(data = historyPreviewData(), onOpenRound = {})
            }
        }

        composeRule.onNodeWithText("BLACKJACK").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Blackjack").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("Crash").assertCountEquals(0)
        composeRule.onAllNodesWithText("Roulette").assertCountEquals(0)
    }

    @Test
    fun tapping_a_round_invokes_callback_with_round_id() {
        var opened: String? = null
        composeRule.setContent {
            StackcasinoTheme {
                HistoryScreen(data = historyPreviewData(), onOpenRound = { opened = it })
            }
        }

        composeRule.onNodeWithText("Crash").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals("round-001", opened)
    }
}
