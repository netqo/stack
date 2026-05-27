package com.plainstudio.stackcasino.feature.auth

import android.app.Activity
import app.cash.turbine.test
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.plainstudio.stackcasino.data.auth.LastSignInHint
import com.plainstudio.stackcasino.domain.auth.AuthRepository
import com.plainstudio.stackcasino.domain.auth.AuthUser
import com.plainstudio.stackcasino.domain.auth.SignInOutcome
import com.plainstudio.stackcasino.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>()
    private val lastSignInHint = mockk<LastSignInHint>(relaxUnitFun = true)
    private val activity = mockk<Activity>()

    @Before
    fun setUp() {
        // Default: no prior sign-in cached. Tests that need Returning
        // override this with a specific hint.
        every { lastSignInHint.read() } returns null
    }

    private fun viewModel(): LoginViewModel = LoginViewModel(authRepository, lastSignInHint)

    // --- initial state --------------------------------------------------

    @Test
    fun `initial state is Idle when LastSignInHint cache is empty`() =
        runTest {
            assertEquals(LoginUiState.Idle, viewModel().uiState.value)
        }

    @Test
    fun `initial state is Returning when LastSignInHint cache has a hint`() =
        runTest {
            every { lastSignInHint.read() } returns
                LastSignInHint.Hint(
                    displayName = "John Doe",
                    email = "john.doe@gmail.com",
                    photoUrl = "https://example.com/avatar.png",
                )

            assertEquals(
                LoginUiState.Returning(
                    displayName = "John Doe",
                    email = "john.doe@gmail.com",
                    photoUrl = "https://example.com/avatar.png",
                ),
                viewModel().uiState.value,
            )
        }

    // --- sign-in transitions -------------------------------------------

    @Test
    fun `signIn success goes Idle to Loading to Idle and emits NavigateToLobby`() =
        runTest {
            val user =
                AuthUser(uid = "u-1", email = "jane@example.com", displayName = "Jane", photoUrl = null)
            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { authRepository.signInWithGoogle(activity) } coAnswers { gate.await() }

            val vm = viewModel()

            turbineScope {
                val state = vm.uiState.testIn(backgroundScope)
                val events = vm.events.testIn(backgroundScope)

                assertEquals(LoginUiState.Idle, state.awaitItem())
                vm.signIn(activity)
                assertEquals(LoginUiState.Loading, state.awaitItem())
                gate.complete(SignInOutcome.Success(user))
                assertEquals(LoginUiState.Idle, state.awaitItem())
                assertEquals(LoginEvent.NavigateToLobby, events.awaitItem())
            }

            verify(exactly = 1) {
                lastSignInHint.write(
                    LastSignInHint.Hint(
                        displayName = "Jane",
                        email = "jane@example.com",
                        photoUrl = null,
                    ),
                )
            }
        }

    @Test
    fun `signIn success without displayName or email skips writing the hint`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(activity) } returns
                SignInOutcome.Success(
                    AuthUser(uid = "u-1", email = null, displayName = null, photoUrl = null),
                )

            viewModel().signIn(activity)

            verify(exactly = 0) { lastSignInHint.write(any()) }
        }

    @Test
    fun `signIn failure goes Idle to Loading to Error with the throwable message`() =
        runTest {
            val message = "Network unreachable"
            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { authRepository.signInWithGoogle(activity) } coAnswers { gate.await() }

            val vm = viewModel()
            vm.uiState.test {
                assertEquals(LoginUiState.Idle, awaitItem())
                vm.signIn(activity)
                assertEquals(LoginUiState.Loading, awaitItem())
                gate.complete(SignInOutcome.Failure(RuntimeException(message)))
                assertEquals(LoginUiState.Error(message), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `signIn failure with null message falls back to a friendly default`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(activity) } returns
                SignInOutcome.Failure(RuntimeException())

            val vm = viewModel()
            vm.signIn(activity)

            val state = vm.uiState.value
            assertTrue("Expected Error state, got $state", state is LoginUiState.Error)
            assertTrue((state as LoginUiState.Error).message.isNotBlank())
        }

    @Test
    fun `signIn cancellation returns to Idle without surfacing an error`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(activity) } returns SignInOutcome.Cancelled

            val vm = viewModel()
            vm.signIn(activity)

            assertEquals(LoginUiState.Idle, vm.uiState.value)
        }

    @Test
    fun `signIn cancellation falls back to Returning when a cached hint exists`() =
        runTest {
            val hint =
                LastSignInHint.Hint(displayName = "John", email = "john@example.com", photoUrl = null)
            every { lastSignInHint.read() } returns hint
            coEvery { authRepository.signInWithGoogle(activity) } returns SignInOutcome.Cancelled

            val vm = viewModel()
            vm.signIn(activity)

            assertEquals(
                LoginUiState.Returning("John", "john@example.com", null),
                vm.uiState.value,
            )
        }

    // --- dismiss + retry ----------------------------------------------

    @Test
    fun `dismissError moves Error back to Idle when no hint is cached`() =
        runTest {
            coEvery { authRepository.signInWithGoogle(activity) } returns
                SignInOutcome.Failure(RuntimeException("boom"))

            val vm = viewModel()
            vm.signIn(activity)
            assertEquals(LoginUiState.Error("boom"), vm.uiState.value)

            vm.dismissError()
            assertEquals(LoginUiState.Idle, vm.uiState.value)
        }

    @Test
    fun `dismissError moves Error back to Returning when a hint is cached`() =
        runTest {
            val hint = LastSignInHint.Hint("Jane", "jane@example.com", null)
            every { lastSignInHint.read() } returns hint
            coEvery { authRepository.signInWithGoogle(activity) } returns
                SignInOutcome.Failure(RuntimeException("boom"))

            val vm = viewModel()
            vm.signIn(activity)
            assertEquals(LoginUiState.Error("boom"), vm.uiState.value)

            vm.dismissError()
            assertEquals(LoginUiState.Returning("Jane", "jane@example.com", null), vm.uiState.value)
        }

    // --- use another account -----------------------------------------

    @Test
    fun `useAnotherAccount clears the hint and triggers a fresh sign-in`() =
        runTest {
            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { authRepository.signInWithGoogle(activity) } coAnswers { gate.await() }

            val vm = viewModel()
            vm.useAnotherAccount(activity)

            verify(exactly = 1) { lastSignInHint.clear() }
            assertEquals(LoginUiState.Loading, vm.uiState.value)
            coVerify(exactly = 1) { authRepository.signInWithGoogle(activity) }

            gate.complete(
                SignInOutcome.Success(
                    AuthUser(uid = "u-1", email = null, displayName = null, photoUrl = null),
                ),
            )
        }

    // --- double-tap guard --------------------------------------------

    @Test
    fun `signIn calls while already loading are ignored`() =
        runTest {
            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { authRepository.signInWithGoogle(activity) } coAnswers { gate.await() }

            val vm = viewModel()
            vm.signIn(activity)
            vm.signIn(activity)
            vm.signIn(activity)

            assertEquals(LoginUiState.Loading, vm.uiState.value)
            coVerify(exactly = 1) { authRepository.signInWithGoogle(activity) }

            gate.complete(
                SignInOutcome.Success(
                    AuthUser(uid = "u-1", email = null, displayName = null, photoUrl = null),
                ),
            )
        }
}
