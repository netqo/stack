package com.plainstudio.stackcasino

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Smoke test for the Hilt scaffold.
 *
 * Verifies via reflection that [StackCasinoApp] is wired as the Hilt
 * application without needing Robolectric or an instrumentation runner.
 * If the annotation goes missing, KSP would silently skip generating
 * [Hilt_StackCasinoApp] and the runtime DI graph would fail to bootstrap.
 */
class StackCasinoAppTest {
    @Test
    fun `extends Application`() {
        assertTrue(Application::class.java.isAssignableFrom(StackCasinoApp::class.java))
    }

    @Test
    fun `is annotated with HiltAndroidApp`() {
        val annotation = StackCasinoApp::class.java.getAnnotation(HiltAndroidApp::class.java)
        assertNotNull("StackCasinoApp must be annotated with @HiltAndroidApp", annotation)
    }
}
