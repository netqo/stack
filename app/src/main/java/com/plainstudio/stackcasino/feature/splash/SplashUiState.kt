package com.plainstudio.stackcasino.feature.splash

import com.plainstudio.stackcasino.navigation.StartDestination

/**
 * State emitted by SplashViewModel while it decides where to send the
 * user after the system splash dismisses.
 *
 * The activity keeps the splash on screen while this is [Resolving],
 * then renders the nav graph rooted at [Ready.startDestination].
 */
sealed interface SplashUiState {
    data object Resolving : SplashUiState

    data class Ready(
        val startDestination: StartDestination,
    ) : SplashUiState
}
