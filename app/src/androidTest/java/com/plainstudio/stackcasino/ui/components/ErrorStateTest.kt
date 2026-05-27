package com.plainstudio.stackcasino.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_title_message_and_primary_action() {
        composeRule.setContent {
            StackcasinoTheme {
                ErrorState(
                    icon = { ErrorStateDefaults.OfflineIcon() },
                    title = "Couldn't load profile",
                    message = "Sync with Firestore failed.",
                    primaryActionLabel = "Retry",
                    onPrimaryAction = {},
                )
            }
        }

        composeRule.onNodeWithText("Couldn't load profile").assertIsDisplayed()
        composeRule.onNodeWithText("Sync with Firestore failed.").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY").assertIsDisplayed()
    }

    @Test
    fun primary_action_click_invokes_callback() {
        var clicks = 0
        composeRule.setContent {
            StackcasinoTheme {
                ErrorState(
                    icon = { ErrorStateDefaults.OfflineIcon() },
                    title = "Boom",
                    message = "Try again.",
                    primaryActionLabel = "Retry",
                    onPrimaryAction = { clicks += 1 },
                )
            }
        }

        composeRule.onNodeWithText("RETRY").performClick()
        composeRule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun secondary_action_renders_only_when_both_label_and_callback_provided() {
        var primary = 0
        var secondary = 0
        composeRule.setContent {
            StackcasinoTheme {
                ErrorState(
                    icon = { ErrorStateDefaults.OfflineIcon() },
                    title = "Connection Lost",
                    message = "Offline.",
                    primaryActionLabel = "Retry",
                    onPrimaryAction = { primary += 1 },
                    secondaryActionLabel = "Use cache",
                    onSecondaryAction = { secondary += 1 },
                )
            }
        }

        composeRule.onNodeWithText("USE CACHE").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        assertEquals(0, primary)
        assertEquals(1, secondary)
    }

    @Test
    fun footer_is_uppercased_when_provided() {
        composeRule.setContent {
            StackcasinoTheme {
                ErrorState(
                    icon = { ErrorStateDefaults.OfflineIcon() },
                    title = "Couldn't load",
                    message = "msg",
                    primaryActionLabel = "Retry",
                    onPrimaryAction = {},
                    footer = "Last successful fetch 2h ago",
                )
            }
        }

        composeRule.onNodeWithText("LAST SUCCESSFUL FETCH 2H AGO").assertIsDisplayed()
    }
}
