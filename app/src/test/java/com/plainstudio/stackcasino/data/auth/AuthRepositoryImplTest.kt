package com.plainstudio.stackcasino.data.auth

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.plainstudio.stackcasino.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * Integration-style unit tests for [AuthRepositoryImpl]. Exercises the
 * sign-out tear-down and the AuthState listener wiring that powers the
 * `currentUser` flow.
 *
 * The full Credential Manager + Firebase ID token round-trip required
 * by `signInWithGoogle` is exercised on-device; mocking the entire
 * Credential Manager handshake here would be more brittle than the
 * production code itself.
 */
class AuthRepositoryImplTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val credentialManager = mockk<CredentialManager>(relaxed = true)
    private val repository = AuthRepositoryImpl(firebaseAuth, credentialManager)

    @Test
    fun `signOut tears down both Firebase and Credential Manager state`() =
        runTest {
            coEvery {
                credentialManager.clearCredentialState(any<ClearCredentialStateRequest>())
            } returns Unit

            repository.signOut()

            verify(exactly = 1) { firebaseAuth.signOut() }
            coVerify(exactly = 1) {
                credentialManager.clearCredentialState(any<ClearCredentialStateRequest>())
            }
        }

    @Test
    fun `currentUser emits null when FirebaseAuth has no cached user`() =
        runTest {
            val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
            every { firebaseAuth.currentUser } returns null
            every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
            }

            repository.currentUser.test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) { firebaseAuth.addAuthStateListener(any()) }
        }

    @Test
    fun `currentUser emits a mapped AuthUser when FirebaseAuth has a cached user`() =
        runTest {
            val firebaseUser =
                mockk<FirebaseUser> {
                    every { uid } returns "uid-9"
                    every { email } returns "jane@example.com"
                    every { displayName } returns "Jane"
                    every { photoUrl } returns null
                }
            val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
            every { firebaseAuth.currentUser } returns firebaseUser
            every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
            }

            repository.currentUser.test {
                val emitted = awaitItem()
                assertEquals("uid-9", emitted?.uid)
                assertEquals("jane@example.com", emitted?.email)
                assertEquals("Jane", emitted?.displayName)
                assertNull(emitted?.photoUrl)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `currentUser unregisters the AuthState listener when the collector cancels`() =
        runTest {
            every { firebaseAuth.currentUser } returns null
            every { firebaseAuth.addAuthStateListener(any()) } answers {
                val listener = firstArg<FirebaseAuth.AuthStateListener>()
                listener.onAuthStateChanged(firebaseAuth)
            }

            repository.currentUser.test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            verify(exactly = 1) { firebaseAuth.removeAuthStateListener(any()) }
        }
}
