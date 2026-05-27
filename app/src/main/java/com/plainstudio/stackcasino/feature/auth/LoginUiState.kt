package com.plainstudio.stackcasino.feature.auth

/**
 * UI state for the login screen.
 *
 * Mirrors the four illustrated states of the mockup (`cu-02-login-*`):
 *   * [Idle] -> first-launch default, "Sign in with Google" CTA active.
 *   * [Returning] -> the user signed in on this device before; we render
 *     a "Continue as ..." card seeded from the LastSignInHint cache.
 *     Tapping the card runs the same Credential Manager flow, tapping
 *     "Use another account" clears the hint and lets the user pick.
 *   * [Loading] -> CTA disabled with the "Connecting to Google..."
 *     spinner.
 *   * [Error] -> red banner at the top of the screen (replacing the
 *     PLAIN STUDIO / POLYGON MAINNET meta row so the hero does not
 *     shift), CTA stays active so the user can retry. User-initiated
 *     cancellations do NOT land here; they go back to Idle/Returning
 *     silently.
 */
sealed interface LoginUiState {
    data object Idle : LoginUiState

    data class Returning(
        val displayName: String,
        val email: String,
        val photoUrl: String?,
    ) : LoginUiState

    data object Loading : LoginUiState

    data class Error(
        val message: String,
    ) : LoginUiState
}

/**
 * One-shot navigation effects emitted by [LoginViewModel] so the
 * NavController call lives in the composable layer (state hoisting).
 */
sealed interface LoginEvent {
    data object NavigateToLobby : LoginEvent
}
