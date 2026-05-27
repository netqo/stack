package com.plainstudio.stackcasino.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plainstudio.stackcasino.HiltTestActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Smoke-validates that every [Route] declared in the sealed hierarchy
 * is actually registered in the nav graph and reachable from the
 * start destination. If a route is added to [Route] without a matching
 * `composable(...)` block in [StackNavHost], the call to [navigate]
 * here throws and the test fails.
 *
 * Runs against [HiltTestActivity] (not the default `ComponentActivity`
 * the bare `createComposeRule()` would spin up) because several
 * destinations call `hiltViewModel()` which only resolves on an
 * `@AndroidEntryPoint` host.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StackNavHostTest {
    private val hiltRule = HiltAndroidRule(this)
    private val composeRule = createAndroidComposeRule<HiltTestActivity>()

    // Hilt has to inject the test before the Compose rule mounts the
    // activity, otherwise hiltViewModel() inside the first composed
    // screen has no graph to pull from.
    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    private lateinit var navController: TestNavHostController

    @Test
    fun every_static_route_is_reachable() {
        composeRule.setContent {
            navController =
                TestNavHostController(composeRule.activity).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            StackNavHost(navController = navController, startDestination = Route.Login.path)
        }

        val staticTargets =
            listOf(
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
            composeRule.runOnUiThread { navController.navigate(route.defaultPath) }
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
                TestNavHostController(composeRule.activity).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            StackNavHost(navController = navController, startDestination = Route.Login.path)
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
