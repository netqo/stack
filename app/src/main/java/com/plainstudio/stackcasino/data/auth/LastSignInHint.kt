package com.plainstudio.stackcasino.data.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists a non-secret cosmetic hint about the last user that signed
 * in on this device (display name, email, avatar URL) so the login
 * screen can render the `Continue as ...` card (cu-02-login-returning)
 * after sign-out without re-querying the account chooser.
 *
 * The actual session token lives in Firebase Auth's encrypted storage
 * and is the single source of truth for `is the user logged in`; this
 * cache is purely UI sugar and is safe in plain SharedPreferences.
 */
@Singleton
class LastSignInHint
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

        data class Hint(
            val displayName: String,
            val email: String,
            val photoUrl: String?,
        )

        fun read(): Hint? {
            val displayName = prefs.getString(KEY_DISPLAY_NAME, null) ?: return null
            val email = prefs.getString(KEY_EMAIL, null) ?: return null
            val photoUrl = prefs.getString(KEY_PHOTO_URL, null)
            return Hint(displayName, email, photoUrl)
        }

        fun write(hint: Hint) {
            prefs.edit {
                putString(KEY_DISPLAY_NAME, hint.displayName)
                putString(KEY_EMAIL, hint.email)
                putString(KEY_PHOTO_URL, hint.photoUrl)
            }
        }

        fun clear() {
            prefs.edit { clear() }
        }

        private companion object {
            const val FILE_NAME = "stackcasino_last_signin_hint"
            const val KEY_DISPLAY_NAME = "display_name"
            const val KEY_EMAIL = "email"
            const val KEY_PHOTO_URL = "photo_url"
        }
    }
