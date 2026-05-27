package com.plainstudio.stackcasino.ui.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BalancePillTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun renders_label_uppercased_and_amount_verbatim() {
        composeRule.setContent {
            StackcasinoTheme {
                BalancePill(label = "Available", amount = "$1,248.50")
            }
        }

        composeRule.onNodeWithText("AVAILABLE").assertIsDisplayed()
        composeRule.onNodeWithText("$1,248.50").assertIsDisplayed()
    }

    @Test
    fun hidden_state_replaces_amount_with_dot_placeholder() {
        composeRule.setContent {
            StackcasinoTheme {
                BalancePill(
                    label = "Available",
                    amount = "$1,248.50",
                    isHidden = true,
                    onToggleVisibility = {},
                )
            }
        }

        composeRule.onAllNodesWithContentDescription("Hide balance").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Show balance").assertIsDisplayed()
        composeRule.onNodeWithText("••••••").assertIsDisplayed()
    }

    @Test
    fun toggle_click_invokes_callback() {
        val hidden = mutableStateOf(false)
        composeRule.setContent {
            StackcasinoTheme {
                BalancePill(
                    label = "Available",
                    amount = "$1,248.50",
                    isHidden = hidden.value,
                    onToggleVisibility = { hidden.value = !hidden.value },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Hide balance").performClick()
        composeRule.waitForIdle()
        assertTrue("Toggle should have flipped to hidden.", hidden.value)

        composeRule.onNodeWithContentDescription("Show balance").performClick()
        composeRule.waitForIdle()
        assertEquals(false, hidden.value)
    }

    @Test
    fun toggle_is_absent_when_callback_is_null() {
        composeRule.setContent {
            StackcasinoTheme {
                BalancePill(label = "Locked", amount = "$0.00")
            }
        }

        composeRule.onAllNodesWithContentDescription("Hide balance").assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription("Show balance").assertCountEquals(0)
    }
}
