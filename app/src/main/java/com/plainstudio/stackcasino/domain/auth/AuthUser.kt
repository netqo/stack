package com.plainstudio.stackcasino.domain.auth

/**
 * Minimal projection of the authenticated user that the app's UI and
 * ViewModels consume.
 *
 * Keeps the surface domain-pure: nothing else in the codebase depends
 * on FirebaseUser, so the auth provider could swap without touching the
 * feature layer.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
