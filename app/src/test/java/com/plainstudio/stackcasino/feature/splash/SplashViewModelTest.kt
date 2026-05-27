package com.plainstudio.stackcasino.feature.splash

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.plainstudio.stackcasino.navigation.StartDestination
import com.plainstudio.stackcasino.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>()

    @Test
    fun `resolves to Login when there is no current Firebase user`() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val viewModel = SplashViewModel(firebaseAuth)

            viewModel.uiState.test {
                assertEquals(
                    SplashUiState.Ready(StartDestination.Login),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `resolves to Lobby when a Firebase user is cached on the device`() =
        runTest {
            every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()

            val viewModel = SplashViewModel(firebaseAuth)

            viewModel.uiState.test {
                assertEquals(
                    SplashUiState.Ready(StartDestination.Lobby),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }
        }
}
