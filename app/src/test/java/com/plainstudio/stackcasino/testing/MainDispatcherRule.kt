package com.plainstudio.stackcasino.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit 4 rule that swaps the Main dispatcher for a [TestDispatcher]
 * during the test, then resets it.
 *
 * Shared by every ViewModel test in the project: anything that calls
 * `viewModelScope.launch` (which dispatches on Main by default) needs
 * Main to point at a controlled dispatcher so the launched body can
 * complete deterministically inside `runTest { ... }`.
 *
 * Defaults to [UnconfinedTestDispatcher] so coroutines run eagerly
 * the moment they are launched, which keeps test arrangement linear
 * (no `advanceUntilIdle` choreography for state-flow inits).
 */
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
