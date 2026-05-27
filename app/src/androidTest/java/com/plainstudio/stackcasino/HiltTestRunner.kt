package com.plainstudio.stackcasino

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation runner that swaps the real [StackCasinoApp] for
 * Hilt's [HiltTestApplication] so each @HiltAndroidTest gets a fresh,
 * test-isolated component graph. Wired through
 * `defaultConfig.testInstrumentationRunner` in app/build.gradle.kts.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
