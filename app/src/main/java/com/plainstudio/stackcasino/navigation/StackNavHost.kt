package com.plainstudio.stackcasino.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Wires every [Route] into a single Compose nav graph. Each card 09 and
 * 10 will replace the placeholder bodies with the real screens; the
 * routing surface itself stays stable.
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
        composable(Route.Login.path) { Placeholder("Login") }
        composable(Route.Lobby.path) { Placeholder("Lobby") }
        composable(Route.Wallet.path) { Placeholder("Wallet") }
        composable(Route.HouseWallet.path) { Placeholder("House Wallet") }
        composable(Route.History.path) { Placeholder("History") }
        composable(Route.News.path) { Placeholder("News") }
        composable(Route.Profile.path) { Placeholder("Profile") }
        composable(Route.Kyc.path) { Placeholder("KYC") }
        composable(Route.Assistant.path) { Placeholder("Assistant") }
        composable(Route.Coinflip.path) { Placeholder("Coinflip") }
        composable(Route.Roulette.path) { Placeholder("Roulette") }
        composable(Route.Crash.path) { Placeholder("Crash") }
        composable(Route.Mines.path) { Placeholder("Mines") }
        composable(Route.Blackjack.path) { Placeholder("Blackjack") }
        composable(
            route = Route.RoundDetail.path,
            arguments = listOf(navArgument(Route.RoundDetail.ARG_ROUND_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Route.RoundDetail.ARG_ROUND_ID).orEmpty()
            Placeholder("Round Detail · $id")
        }
        composable(
            route = Route.NewsDetail.path,
            arguments = listOf(navArgument(Route.NewsDetail.ARG_ARTICLE_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Route.NewsDetail.ARG_ARTICLE_ID).orEmpty()
            Placeholder("News Detail · $id")
        }
    }
}

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
