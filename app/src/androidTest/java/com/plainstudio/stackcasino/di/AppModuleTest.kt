package com.plainstudio.stackcasino.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Verifies the Firebase singletons exposed by [AppModule] actually
 * resolve through Hilt. The test runs against the real Firebase SDK
 * with the project's google-services.json, so a missing or malformed
 * configuration would fail here before any feature code is added.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AppModuleTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var firebaseAuth: FirebaseAuth

    @Inject lateinit var firebaseFirestore: FirebaseFirestore

    @Inject lateinit var firebaseAuthAgain: FirebaseAuth

    @Before
    fun setUp() = hiltRule.inject()

    @Test
    fun provides_firebase_auth() {
        assertNotNull(firebaseAuth)
    }

    @Test
    fun provides_firebase_firestore() {
        assertNotNull(firebaseFirestore)
    }

    @Test
    fun firebase_auth_is_singleton() {
        assertSame(firebaseAuth, firebaseAuthAgain)
    }
}
