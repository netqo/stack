package com.plainstudio.stackcasino.feature.assistant

import app.cash.turbine.test
import com.plainstudio.stackcasino.domain.assistant.AssistantRepository
import com.plainstudio.stackcasino.domain.assistant.ChatTurn
import com.plainstudio.stackcasino.domain.assistant.Role
import com.plainstudio.stackcasino.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AssistantViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AssistantRepository>()

    private fun viewModel(): AssistantViewModel = AssistantViewModel(repository)

    // --- initial state ----------------------------------------------------

    @Test
    fun `initial state is Welcome`() =
        runTest {
            assertEquals(AssistantUiState.Welcome, viewModel().uiState.value)
        }

    // --- sendMessage happy path ------------------------------------------

    @Test
    fun `sendMessage appends user bubble immediately and flips typing on`() =
        runTest {
            // Gate the repository so the typing state is observable.
            val gate = CompletableDeferred<Result<String>>()
            coEvery { repository.sendMessage(any(), any()) } coAnswers { gate.await() }
            val vm = viewModel()

            vm.uiState.test {
                assertEquals(AssistantUiState.Welcome, awaitItem())

                vm.sendMessage("How does Blackjack splitting work?")

                val typing = awaitItem() as AssistantUiState.Conversation
                assertEquals(1, typing.messages.size)
                assertEquals(Author.User, typing.messages[0].author)
                assertEquals("How does Blackjack splitting work?", typing.messages[0].body)
                assertTrue(typing.isNepTyping)

                gate.complete(Result.success("Hey hey~ Splitting kicks in when..."))

                val replied = awaitItem() as AssistantUiState.Conversation
                assertEquals(2, replied.messages.size)
                assertEquals(Author.Nep, replied.messages[1].author)
                assertFalse(replied.messages[1].isError)
                assertEquals("Hey hey~ Splitting kicks in when...", replied.messages[1].body)
                assertFalse(replied.isNepTyping)
            }
        }

    @Test
    fun `sendMessage trims whitespace and forwards the trimmed text to the repository`() =
        runTest {
            coEvery { repository.sendMessage(any(), any()) } returns Result.success("ok")
            val vm = viewModel()

            vm.sendMessage("   What are the Roulette odds?\n")

            coVerify {
                repository.sendMessage(
                    history = emptyList(),
                    userMessage = "What are the Roulette odds?",
                )
            }
        }

    @Test
    fun `sendMessage drops empty or whitespace-only drafts`() =
        runTest {
            val vm = viewModel()

            vm.sendMessage("")
            vm.sendMessage("   \n\t")

            assertEquals(AssistantUiState.Welcome, vm.uiState.value)
            coVerify(exactly = 0) { repository.sendMessage(any(), any()) }
        }

    @Test
    fun `sendMessage drops drafts longer than the 300 char cap`() =
        runTest {
            val vm = viewModel()

            vm.sendMessage("a".repeat(AssistantViewModel.MAX_USER_MESSAGE_LENGTH + 1))

            assertEquals(AssistantUiState.Welcome, vm.uiState.value)
            coVerify(exactly = 0) { repository.sendMessage(any(), any()) }
        }

    // --- error path -------------------------------------------------------

    @Test
    fun `sendMessage failure surfaces a red Nep bubble flagged as error`() =
        runTest {
            coEvery { repository.sendMessage(any(), any()) } returns
                Result.failure(RuntimeException("network down"))
            val vm = viewModel()

            vm.sendMessage("Explain the Mines multiplier table")

            val state = vm.uiState.value as AssistantUiState.Conversation
            assertEquals(2, state.messages.size)
            assertEquals(Author.Nep, state.messages[1].author)
            assertTrue(state.messages[1].isError)
            assertFalse(state.isNepTyping)
        }

    @Test
    fun `error bubbles are excluded from the history sent on the next request`() =
        runTest {
            // First call fails -> Nep error bubble.
            coEvery { repository.sendMessage(any(), any()) } returns
                Result.failure(RuntimeException("first try fails"))
            val vm = viewModel()
            vm.sendMessage("first")

            // Second call succeeds. The repository should see only the
            // first user turn, not the error bubble.
            coEvery { repository.sendMessage(any(), any()) } returns Result.success("ok")
            vm.sendMessage("second")

            coVerify {
                repository.sendMessage(
                    history = listOf(ChatTurn(role = Role.User, text = "first")),
                    userMessage = "second",
                )
            }
        }

    // --- multi-turn history wiring ---------------------------------------

    @Test
    fun `subsequent sends pass prior user + Nep turns as history`() =
        runTest {
            coEvery { repository.sendMessage(any(), any()) } returnsMany
                listOf(Result.success("Heya~ here you go"), Result.success("Sure thing~"))
            val vm = viewModel()

            vm.sendMessage("question one")
            vm.sendMessage("question two")

            coVerify {
                repository.sendMessage(
                    history =
                        listOf(
                            ChatTurn(role = Role.User, text = "question one"),
                            ChatTurn(role = Role.Nep, text = "Heya~ here you go"),
                        ),
                    userMessage = "question two",
                )
            }
        }

    // --- clear() ---------------------------------------------------------

    @Test
    fun `clear() resets a Conversation back to Welcome`() =
        runTest {
            coEvery { repository.sendMessage(any(), any()) } returns Result.success("ok")
            val vm = viewModel()
            vm.sendMessage("first")
            assertTrue(vm.uiState.value is AssistantUiState.Conversation)

            vm.clear()

            assertEquals(AssistantUiState.Welcome, vm.uiState.value)
        }
}
