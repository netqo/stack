package com.plainstudio.stackcasino.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke-validates that every [Route] declared in the sealed hierarchy is
 * actually registered in the nav graph and reachable from the start
 * destination. If a route is added to [Route] without a matching
 * `composable(...)` block in [StackNavHost], the call to [navigate]
 * here throws and the test fails.
 */
@RunWith(AndroidJUnit4::class)
class StackNavHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Test
    fun every_static_route_is_reachable() {
        composeRule.setContent {
            navController =
                TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                    navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
                }
            StackNavHost(navController = navController)
        }

        val staticTargets =
            listOf(
                Route.Splash,
                Route.Login,
                Route.Lobby,
                Route.Wallet,
                Route.HouseWallet,
                Route.History,
                Route.News,
                Route.Profile,
                Route.Kyc,
                Route.Assistant,
                Route.Coinflip,
                Route.Roulette,
                Route.Crash,
                Route.Mines,
                Route.Blackjack,
            )

        staticTargets.forEach { route ->
            composeRule.runOnUiThread { navController.navigate(route.path) }
            composeRule.waitForIdle()
            assertEquals(
                "Navigation to ${route.path} did not land on the expected destination.",
                route.path,
                navController.currentBackStackEntry?.destination?.route,
            )
        }
    }

    @Test
    fun parametric_routes_resolve_with_arguments() {
        composeRule.setContent {
            navController =
                TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                    navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
                }
            StackNavHost(navController = navController)
        }

        composeRule.runOnUiThread { navController.navigate(Route.RoundDetail.build("round-42")) }
        composeRule.waitForIdle()
        assertEquals(Route.RoundDetail.path, navController.currentBackStackEntry?.destination?.route)
        assertEquals(
            "round-42",
            navController.currentBackStackEntry?.arguments?.getString(Route.RoundDetail.ARG_ROUND_ID),
        )

        composeRule.runOnUiThread { navController.navigate(Route.NewsDetail.build("article-7")) }
        composeRule.waitForIdle()
        assertEquals(Route.NewsDetail.path, navController.currentBackStackEntry?.destination?.route)
        assertEquals(
            "article-7",
            navController.currentBackStackEntry?.arguments?.getString(Route.NewsDetail.ARG_ARTICLE_ID),
        )
    }
}
