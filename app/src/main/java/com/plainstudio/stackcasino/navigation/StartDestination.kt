package com.plainstudio.stackcasino.navigation

/**
 * Where to land the user after the system splash dismisses.
 *
 * Emitted by SplashViewModel based on the cached Firebase Auth state:
 *   * [Login] when no session is persisted on the device.
 *   * [Lobby] when a session exists; the user goes straight to the hub.
 *
 * Kept as a small sealed surface so downstream cards (e.g. KYC gating
 * or deep-link entry points) can extend it without changing the
 * MainActivity/StackApp contract.
 */
sealed interface StartDestination {
    val route: String

    data object Login : StartDestination {
        override val route: String = Route.Login.path
    }

    data object Lobby : StartDestination {
        override val route: String = Route.Lobby.path
    }
}
