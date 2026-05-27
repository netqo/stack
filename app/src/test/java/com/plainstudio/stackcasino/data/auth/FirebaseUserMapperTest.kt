package com.plainstudio.stackcasino.data.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirebaseUserMapperTest {
    @Test
    fun `maps every non-null FirebaseUser field into AuthUser`() {
        val photoUri = mockk<Uri>()
        every { photoUri.toString() } returns "https://example.com/avatar.png"

        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "uid-42"
                every { email } returns "jane@example.com"
                every { displayName } returns "Jane Doe"
                every { photoUrl } returns photoUri
            }

        val auth = firebaseUser.toAuthUser()

        assertEquals("uid-42", auth.uid)
        assertEquals("jane@example.com", auth.email)
        assertEquals("Jane Doe", auth.displayName)
        assertEquals("https://example.com/avatar.png", auth.photoUrl)
    }

    @Test
    fun `preserves nullability for optional Firebase fields`() {
        val firebaseUser =
            mockk<FirebaseUser> {
                every { uid } returns "uid-7"
                every { email } returns null
                every { displayName } returns null
                every { photoUrl } returns null
            }

        val auth = firebaseUser.toAuthUser()

        assertEquals("uid-7", auth.uid)
        assertNull(auth.email)
        assertNull(auth.displayName)
        assertNull(auth.photoUrl)
    }
}
