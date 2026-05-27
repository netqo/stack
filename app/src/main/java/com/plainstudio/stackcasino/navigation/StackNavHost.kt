package com.plainstudio.stackcasino.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.plainstudio.stackcasino.feature.assistant.AssistantScreen
import com.plainstudio.stackcasino.feature.auth.LoginScreen
import com.plainstudio.stackcasino.feature.history.HistoryScreen
import com.plainstudio.stackcasino.feature.history.historyPreviewData
import com.plainstudio.stackcasino.feature.lobby.LobbyScreen
import com.plainstudio.stackcasino.feature.lobby.LobbyUiState
import com.plainstudio.stackcasino.feature.lobby.previewLobbyData
import com.plainstudio.stackcasino.feature.news.NewsDetailScreen
import com.plainstudio.stackcasino.feature.news.NewsScreen
import com.plainstudio.stackcasino.feature.wallet.WalletScreen
import com.plainstudio.stackcasino.feature.wallet.previewWalletData

/**
 * Wires every [Route] into a single Compose nav graph. Routes that
 * have not had a real screen implemented yet point at [Placeholder]
 * (showing the route label centered), so navigation tests can already
 * walk the whole graph end-to-end while individual screens land in
 * subsequent feature work.
 *
 * [startDestination] is supplied by the caller (see StackApp), which
 * derives it from SplashViewModel: Login when there is no Firebase
 * session, Lobby otherwise. The system splash from the AndroidX
 * SplashScreen API covers the boot frames, so no Compose-side splash
 * route is needed.
 */
@Composable
fun StackNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Route.Login.path) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Route.Lobby.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Route.Lobby.path) {
            LobbyScreen(
                state = LobbyUiState.Success(previewLobbyData()),
                onNavigate = { route ->
                    navController.navigate(route.path) { launchSingleTop = true }
                },
                onRetry = {},
                onUseCache = {},
            )
        }
        composable(Route.Wallet.path) {
            WalletScreen(
                data = previewWalletData(),
                onNavigate = { route ->
                    navController.navigate(route.path) { launchSingleTop = true }
                },
            )
        }
        composable(Route.History.path) {
            HistoryScreen(
                data = historyPreviewData(),
                onOpenRound = { roundId ->
                    navController.navigate(Route.RoundDetail.build(roundId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Route.Assistant.path) {
            AssistantScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.News.path) {
            NewsScreen(
                onOpenArticle = { articleId ->
                    navController.navigate(Route.NewsDetail.build(articleId)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        PLACEHOLDER_ROUTES.forEach { (route, label) ->
            placeholderRoute(route, label)
        }
        addParametricRoutes(navController)
    }
}

/**
 * Routes with placeholders for their string argument. Kept out of the
 * main [StackNavHost] body so the entry function stays under the
 * detekt LongMethod budget and the parametric registrations group
 * together for readers scanning the file top-down.
 */
private fun NavGraphBuilder.addParametricRoutes(navController: NavHostController) {
    composable(
        route = Route.RoundDetail.path,
        arguments = listOf(navArgument(Route.RoundDetail.ARG_ROUND_ID) { type = NavType.StringType }),
    ) { entry ->
        val id = entry.requireStringArg(Route.RoundDetail.ARG_ROUND_ID)
        Placeholder("Round Detail · $id")
    }
    composable(
        route = Route.NewsDetail.path,
        arguments = listOf(navArgument(Route.NewsDetail.ARG_ARTICLE_ID) { type = NavType.StringType }),
    ) {
        NewsDetailScreen(onBack = { navController.popBackStack() })
    }
}

/**
 * Routes that have not had a real screen implemented yet. Each entry
 * resolves to [Placeholder] in the nav graph; replacing the placeholder
 * is a one-line edit when the feature lands.
 */
private val PLACEHOLDER_ROUTES: List<Pair<Route, String>> =
    listOf(
        Route.HouseWallet to "House Wallet",
        Route.Profile to "Profile",
        Route.Kyc to "KYC",
        Route.Coinflip to "Coinflip",
        Route.Roulette to "Roulette",
        Route.Crash to "Crash",
        Route.Mines to "Mines",
        Route.Blackjack to "Blackjack",
    )

private fun NavGraphBuilder.placeholderRoute(
    route: Route,
    label: String,
) {
    composable(route.path) { Placeholder(label) }
}

/**
 * Read a mandatory navigation argument. Missing arguments indicate a
 * malformed navigate() call somewhere upstream; surface that loudly
 * instead of silently falling back to an empty string.
 */
private fun androidx.navigation.NavBackStackEntry.requireStringArg(name: String): String =
    arguments?.getString(name)
        ?: error("Navigation argument '$name' is missing on ${destination.route}")

@Composable
private fun Placeholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
