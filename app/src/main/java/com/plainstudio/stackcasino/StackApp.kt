package com.plainstudio.stackcasino

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.plainstudio.stackcasino.navigation.PrimaryTab
import com.plainstudio.stackcasino.navigation.Route
import com.plainstudio.stackcasino.navigation.StackNavHost
import com.plainstudio.stackcasino.navigation.StartDestination
import com.plainstudio.stackcasino.ui.components.StackBottomBar
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme

/**
 * Top-level composable. Owns the [Scaffold] that renders the bottom
 * navigation bar plus the [StackNavHost] for the current destination.
 *
 * The bar is only shown when the active route is one of [PrimaryTab];
 * login, deep details, KYC, house wallet, the assistant and the five
 * games own the full viewport.
 *
 * [startDestination] is decided by SplashViewModel from the cached
 * Firebase Auth state; MainActivity holds the system splash until the
 * decision is made and only then mounts this composable.
 */
@Composable
fun StackApp(startDestination: StartDestination) {
    StackcasinoTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentRoute in PrimaryTab.routePaths) {
                    StackBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route.path) {
                                popUpTo(Route.Lobby.path) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { padding ->
            StackNavHost(
                navController = navController,
                startDestination = startDestination.route,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
