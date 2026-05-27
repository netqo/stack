package com.plainstudio.stackcasino.domain.auth

/**
 * Discriminated result of [AuthRepository.signInWithGoogle].
 *
 * Using a sealed type instead of `Result<AuthUser>` lets the caller
 * distinguish a user-initiated cancellation (closing the account
 * chooser) from a real failure (network down, invalid web client id,
 * Firebase rejection). The UI surfaces only the latter as an error
 * banner; cancellations go back to the idle/returning state silently.
 */
sealed interface SignInOutcome {
    data class Success(
        val user: AuthUser,
    ) : SignInOutcome

    data object Cancelled : SignInOutcome

    data class Failure(
        val cause: Throwable,
    ) : SignInOutcome
}
