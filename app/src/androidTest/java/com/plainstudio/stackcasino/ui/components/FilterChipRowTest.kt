package com.plainstudio.stackcasino.ui.components

import androidx.compose.runtime.mutableStateOf
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
class FilterChipRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val chips =
        listOf(
            FilterChip(key = "all", label = "All"),
            FilterChip(key = "wins", label = "Wins"),
            FilterChip(key = "losses", label = "Losses"),
        )

    @Test
    fun renders_every_chip_label_uppercased() {
        composeRule.setContent {
            StackcasinoTheme {
                FilterChipRow(chips = chips, selected = "all", onSelect = {})
            }
        }

        composeRule.onNodeWithText("ALL").assertIsDisplayed()
        composeRule.onNodeWithText("WINS").assertIsDisplayed()
        composeRule.onNodeWithText("LOSSES").assertIsDisplayed()
    }

    @Test
    fun clicking_a_chip_invokes_onSelect_with_the_key() {
        val selected = mutableStateOf("all")
        composeRule.setContent {
            StackcasinoTheme {
                FilterChipRow(
                    chips = chips,
                    selected = selected.value,
                    onSelect = { selected.value = it },
                )
            }
        }

        composeRule.onNodeWithText("WINS").performClick()
        composeRule.waitForIdle()

        assertEquals("wins", selected.value)
    }
}
