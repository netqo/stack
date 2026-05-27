package com.plainstudio.stackcasino.feature.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plainstudio.stackcasino.data.auth.LastSignInHint
import com.plainstudio.stackcasino.domain.auth.AuthRepository
import com.plainstudio.stackcasino.domain.auth.AuthUser
import com.plainstudio.stackcasino.domain.auth.SignInOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the login screen.
 *
 * UI state lives in [uiState]; one-shot navigation effects flow through
 * [events] so the screen can collect them inside a LaunchedEffect and
 * call NavController without leaking it down the ViewModel.
 *
 * On construction the state is seeded from [LastSignInHint] so a
 * returning user (after sign-out) sees the "Continue as ..." card
 * straight away instead of the generic Sign in button.
 */
@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val lastSignInHint: LastSignInHint,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LoginUiState>(deriveRestingState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        private val _events =
            MutableSharedFlow<LoginEvent>(
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

        fun signIn(activity: Activity) {
            if (_uiState.value is LoginUiState.Loading) return
            _uiState.value = LoginUiState.Loading
            viewModelScope.launch {
                handleOutcome(authRepository.signInWithGoogle(activity))
            }
        }

        /**
         * Clear the cached hint and immediately launch a fresh sign-in
         * so the account chooser opens. The state goes Returning ->
         * Loading without surfacing the Idle frame in between.
         */
        fun useAnotherAccount(activity: Activity) {
            lastSignInHint.clear()
            signIn(activity)
        }

        fun dismissError() {
            if (_uiState.value is LoginUiState.Error) {
                _uiState.value = deriveRestingState()
            }
        }

        private fun handleOutcome(outcome: SignInOutcome) {
            when (outcome) {
                is SignInOutcome.Success -> {
                    cacheHint(outcome.user)
                    _uiState.value = deriveRestingState()
                    _events.tryEmit(LoginEvent.NavigateToLobby)
                }
                SignInOutcome.Cancelled -> {
                    // User closed the chooser. No error, just go back to
                    // whatever resting state we had before.
                    _uiState.value = deriveRestingState()
                }
                is SignInOutcome.Failure -> {
                    _uiState.value =
                        LoginUiState.Error(
                            outcome.cause.message ?: "Sign-in failed. Please try again.",
                        )
                }
            }
        }

        private fun cacheHint(user: AuthUser) {
            val displayName = user.displayName ?: return
            val email = user.email ?: return
            lastSignInHint.write(
                LastSignInHint.Hint(
                    displayName = displayName,
                    email = email,
                    photoUrl = user.photoUrl,
                ),
            )
        }

        private fun deriveRestingState(): LoginUiState =
            lastSignInHint.read()?.let { hint ->
                LoginUiState.Returning(
                    displayName = hint.displayName,
                    email = hint.email,
                    photoUrl = hint.photoUrl,
                )
            } ?: LoginUiState.Idle
    }
