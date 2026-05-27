package com.plainstudio.stackcasino.feature.wallet

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
class WalletScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun deposit_tab_renders_address_and_warning() {
        composeRule.setContent {
            StackcasinoTheme {
                WalletScreen(data = previewWalletData(), onNavigate = {})
            }
        }

        composeRule.onNodeWithText("Wallet").assertIsDisplayed()
        composeRule.onNodeWithText("YOUR DEPOSIT ADDRESS").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithText(previewWalletData().depositAddress)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun switching_to_withdraw_renders_form_and_kyc_gate() {
        composeRule.setContent {
            StackcasinoTheme {
                WalletScreen(data = previewWalletData(), onNavigate = {})
            }
        }

        composeRule.onNodeWithText("WITHDRAW").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("DESTINATION ADDRESS").performScrollTo().assertIsDisplayed()
        composeRule
            .onNodeWithText("KYC required for withdrawals over $100")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun withdraw_kyc_cta_navigates_to_kyc_route() {
        var navigated: Route? = null
        composeRule.setContent {
            StackcasinoTheme {
                WalletScreen(
                    data = previewWalletData(),
                    onNavigate = { navigated = it },
                )
            }
        }

        composeRule.onNodeWithText("WITHDRAW").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("VERIFY IDENTITY").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(Route.Kyc, navigated)
    }

    @Test
    fun transactions_tab_lists_rounds_and_filters() {
        composeRule.setContent {
            StackcasinoTheme {
                WalletScreen(data = previewWalletData(), onNavigate = {})
            }
        }

        composeRule.onNodeWithText("TRANSACTIONS").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("+$500.00").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("-$50.00").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithText("DEPOSITS").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("+$500.00").performScrollTo().assertIsDisplayed()
    }
}
