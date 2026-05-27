package com.plainstudio.stackcasino

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Bare-bones `@AndroidEntryPoint` shell used as the host activity for
 * Compose tests that mount screens calling `hiltViewModel()`. The
 * default `createComposeRule()` spins up a plain `ComponentActivity`
 * which Hilt cannot wire (it throws "Given component holder class
 * androidx.activity.ComponentActivity does not implement interface
 * dagger.hilt.internal.GeneratedComponent..."), so any Compose test
 * that needs the Hilt graph has to launch through this activity via
 * `createAndroidComposeRule<HiltTestActivity>()`.
 *
 * Lives in `src/debug/` (not `src/androidTest/`) because Android's
 * instrumentation runner resolves activities through the target APK's
 * PackageManager: a class declared only in the test APK would fail
 * with "Unable to resolve activity". Production release builds drop
 * the activity entirely since the `debug/` sourceset is variant-scoped.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
