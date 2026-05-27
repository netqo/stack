package com.plainstudio.stackcasino.feature.lobby

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plainstudio.stackcasino.navigation.Route
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LobbyScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun success_state_renders_user_balance_games_and_recent_activity() {
        composeRule.setContent {
            StackcasinoTheme {
                LobbyScreen(
                    state = LobbyUiState.Success(previewLobbyData()),
                    onNavigate = {},
                    onOpenWallet = {},
                    onRetry = {},
                    onUseCache = {},
                )
            }
        }

        composeRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeRule.onNodeWithText("$1,234.56").assertIsDisplayed()
        composeRule.onNodeWithText("European Roulette").assertIsDisplayed()
        composeRule.onNodeWithText("LAST PLAYED").assertIsDisplayed()
        composeRule.onNodeWithText("Deposit").assertIsDisplayed()
        // Recent activity sits below the fold on shorter viewports;
        // scrollTo lands it before asserting.
        composeRule.onNodeWithText("+$125.50").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun success_state_routes_game_card_taps_to_the_matching_game_route() {
        var navigated: Route? = null
        composeRule.setContent {
            StackcasinoTheme {
                LobbyScreen(
                    state = LobbyUiState.Success(previewLobbyData()),
                    onNavigate = { navigated = it },
                    onOpenWallet = {},
                    onRetry = {},
                    onUseCache = {},
                )
            }
        }

        // The Crash label appears twice (game card + recent activity); the
        // unambiguous handle is the European Roulette card.
        composeRule.onNodeWithText("European Roulette").performClick()
        composeRule.waitForIdle()

        assertEquals(Route.Roulette, navigated)
    }

    @Test
    fun error_state_renders_retry_and_use_cache_actions() {
        var retried = false
        var usedCache = false
        composeRule.setContent {
            StackcasinoTheme {
                LobbyScreen(
                    state =
                        LobbyUiState.Error(
                            message = "Failed to sync.",
                            lastSyncedLabel = "Last synced 4 minutes ago",
                        ),
                    onNavigate = {},
                    onOpenWallet = {},
                    onRetry = { retried = true },
                    onUseCache = { usedCache = true },
                )
            }
        }

        composeRule.onNodeWithText("Connection Lost").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY").assertIsDisplayed()
        composeRule.onNodeWithText("USE CACHE").assertIsDisplayed()

        composeRule.onNodeWithText("RETRY").performClick()
        composeRule.onNodeWithText("USE CACHE").performClick()
        composeRule.waitForIdle()

        assertEquals(true, retried)
        assertEquals(true, usedCache)
    }
}
