package com.plainstudio.stackcasino.data.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.plainstudio.stackcasino.BuildConfig
import com.plainstudio.stackcasino.domain.auth.AuthRepository
import com.plainstudio.stackcasino.domain.auth.AuthUser
import com.plainstudio.stackcasino.domain.auth.SignInOutcome
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production [AuthRepository] backed by Firebase Auth + Credential
 * Manager.
 *
 * The sign-in flow is the path Google recommends in 2025/2026: request
 * a Google ID token via Credential Manager, then exchange it with
 * Firebase Auth. The legacy `GoogleSignInClient` API was retired.
 *
 * The web client ID lives in `local.properties` and is exposed via
 * `BuildConfig.GOOGLE_WEB_CLIENT_ID`; if the property is missing the
 * field is an empty string and sign-in fails fast with
 * [IllegalStateException].
 */
@Singleton
class AuthRepositoryImpl
    @Inject
    constructor(
        private val firebaseAuth: FirebaseAuth,
        private val credentialManager: CredentialManager,
    ) : AuthRepository {
        override val currentUser: Flow<AuthUser?> =
            callbackFlow {
                val listener =
                    FirebaseAuth.AuthStateListener { auth ->
                        trySend(auth.currentUser?.toAuthUser())
                    }
                firebaseAuth.addAuthStateListener(listener)
                awaitClose { firebaseAuth.removeAuthStateListener(listener) }
            }

        override suspend fun signInWithGoogle(activity: Activity): SignInOutcome =
            try {
                val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                check(webClientId.isNotBlank()) {
                    "GOOGLE_WEB_CLIENT_ID is not set in local.properties; " +
                        "Google Sign-In cannot proceed."
                }

                val request =
                    GetCredentialRequest
                        .Builder()
                        .addCredentialOption(
                            GetGoogleIdOption
                                .Builder()
                                .setServerClientId(webClientId)
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(true)
                                .build(),
                        ).build()

                val response = credentialManager.getCredential(activity, request)
                val credential = response.credential
                check(
                    credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
                ) {
                    "Credential Manager returned an unexpected credential type: ${credential.type}"
                }

                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential =
                    GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                val firebaseUser =
                    firebaseAuth.signInWithCredential(firebaseCredential).await().user
                        ?: error("Firebase returned a null user after a successful sign-in.")

                SignInOutcome.Success(firebaseUser.toAuthUser())
            } catch (
                @Suppress("SwallowedException") cancellation: GetCredentialCancellationException,
            ) {
                // User-initiated dismissal carries no additional context; the
                // outcome type itself is the signal the UI consumes.
                SignInOutcome.Cancelled
            } catch (
                @Suppress("TooGenericExceptionCaught") other: Exception,
            ) {
                SignInOutcome.Failure(other)
            }

        override suspend fun signOut() {
            firebaseAuth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }
