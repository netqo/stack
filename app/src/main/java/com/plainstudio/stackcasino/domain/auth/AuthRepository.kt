package com.plainstudio.stackcasino.domain.auth

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for authentication. The implementation owns the
 * Firebase Auth + Credential Manager wiring; the ViewModels only see
 * this contract.
 */
interface AuthRepository {
    /**
     * Cold stream of the persisted user (or null when no session is
     * cached). Backed by `FirebaseAuth.authStateChanges()`, so it emits
     * across sign-in, sign-out and token refreshes.
     */
    val currentUser: Flow<AuthUser?>

    /**
     * Runs the Google one-tap / account chooser flow using Credential
     * Manager, exchanges the resulting Google ID token with Firebase
     * Auth and returns the resolved [AuthUser].
     *
     * Returns a [SignInOutcome] so the UI can distinguish a real failure
     * from a user cancellation (closing the sheet should not surface an
     * error banner).
     *
     * Requires an Activity reference because Credential Manager attaches
     * the system bottom sheet to the activity window.
     */
    suspend fun signInWithGoogle(activity: Activity): SignInOutcome

    /**
     * Tears down the Firebase session and clears the Credential Manager
     * state so the chooser appears again next time. Idempotent: safe to
     * call when no session is active.
     */
    suspend fun signOut()
}
