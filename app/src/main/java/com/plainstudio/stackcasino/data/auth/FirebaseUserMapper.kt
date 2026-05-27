package com.plainstudio.stackcasino.data.auth

import com.google.firebase.auth.FirebaseUser
import com.plainstudio.stackcasino.domain.auth.AuthUser

/**
 * Pure mapping helper kept at file scope so the unit tests can exercise
 * it without standing up the whole [AuthRepositoryImpl] (which requires
 * Credential Manager and an Activity).
 */
internal fun FirebaseUser.toAuthUser(): AuthUser =
    AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
