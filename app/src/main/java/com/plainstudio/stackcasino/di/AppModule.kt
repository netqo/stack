package com.plainstudio.stackcasino.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Application-scoped Hilt bindings.
 *
 * Currently exposes only the Firebase singletons. They are not consumed
 * yet; the providers exist so downstream cards (Login, Wallet, History,
 * etc.) inject the SDK without touching DI plumbing.
 *
 * FirebaseApp is initialized implicitly by FirebaseInitProvider (which
 * the google-services Gradle plugin registers in the merged manifest),
 * so no manual FirebaseApp.initializeApp call is required.
 *
 * Imports use the non-ktx package paths because Firebase BoM 34+
 * collapsed the Kotlin extensions back into the main modules
 * (the firebase-*-ktx artifacts were retired in July 2025).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore
}
