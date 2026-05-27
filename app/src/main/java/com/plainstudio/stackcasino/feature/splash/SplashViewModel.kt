package com.plainstudio.stackcasino.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.plainstudio.stackcasino.navigation.StartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides where the activity should land after the system splash.
 *
 * Wraps the FirebaseAuth lookup in a coroutine even though
 * `currentUser` is currently a synchronous disk read: future cards
 * (Firestore profile pull, KYC status sync) will plug in here and we
 * want a single state-flow consumer in MainActivity from the start.
 *
 * The flow starts at [SplashUiState.Resolving]; MainActivity holds
 * the splash visible while that state is observed and only swaps to
 * the nav graph once [SplashUiState.Ready] is emitted.
 */
@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Resolving)
        val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val destination =
                    if (firebaseAuth.currentUser != null) {
                        StartDestination.Lobby
                    } else {
                        StartDestination.Login
                    }
                _uiState.value = SplashUiState.Ready(destination)
            }
        }
    }
