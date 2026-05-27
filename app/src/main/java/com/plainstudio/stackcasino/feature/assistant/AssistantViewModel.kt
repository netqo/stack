package com.plainstudio.stackcasino.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plainstudio.stackcasino.domain.assistant.AssistantRepository
import com.plainstudio.stackcasino.domain.assistant.ChatTurn
import com.plainstudio.stackcasino.domain.assistant.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Drives the Nep assistant screen.
 *
 *   * Welcome -> Conversation on first send.
 *   * Each [sendMessage] appends a user bubble immediately, flips
 *     [AssistantUiState.Conversation.isNepTyping] to true, asks the
 *     repository, and replaces the typing indicator with either Nep's
 *     reply or an error bubble.
 *   * [clear] resets to [AssistantUiState.Welcome] and cancels nothing
 *     destructive; in-flight requests just deposit their result into a
 *     stale state and the next clear wipes it again.
 *
 * No streaming yet. The repository returns the full response in one
 * shot, and the UI fakes incremental delivery via the typing indicator.
 */
@HiltViewModel
class AssistantViewModel
    @Inject
    constructor(
        private val repository: AssistantRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Welcome)
        val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

        fun sendMessage(text: String) {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || trimmed.length > MAX_USER_MESSAGE_LENGTH) return

            val userMessage =
                ChatMessage(
                    id = newId(),
                    author = Author.User,
                    body = trimmed,
                    timestampLabel = nowLabel(),
                )
            val historyBefore = _uiState.value.priorTurns()
            _uiState.update { current ->
                current.appendMessage(userMessage).copy(isNepTyping = true)
            }

            viewModelScope.launch {
                val result = repository.sendMessage(history = historyBefore, userMessage = trimmed)
                val nepMessage =
                    result.fold(
                        onSuccess = { reply ->
                            ChatMessage(
                                id = newId(),
                                author = Author.Nep,
                                body = reply,
                                timestampLabel = nowLabel(),
                            )
                        },
                        onFailure = {
                            ChatMessage(
                                id = newId(),
                                author = Author.Nep,
                                body = "Something went wrong on Nep's side. Try sending the question again.",
                                timestampLabel = nowLabel(),
                                isError = true,
                            )
                        },
                    )
                _uiState.update { current -> current.appendMessage(nepMessage).copy(isNepTyping = false) }
            }
        }

        fun clear() {
            _uiState.value = AssistantUiState.Welcome
        }

        // ------------------------------------------------------------------
        // Helpers
        // ------------------------------------------------------------------

        private fun AssistantUiState.priorTurns(): List<ChatTurn> =
            when (this) {
                AssistantUiState.Welcome -> emptyList()
                is AssistantUiState.Conversation ->
                    messages
                        // Errors never went to the model, so they should not
                        // poison the next call's context.
                        .filterNot { it.author == Author.Nep && it.isError }
                        .map { it.toChatTurn() }
            }

        private fun ChatMessage.toChatTurn(): ChatTurn =
            ChatTurn(
                role = if (author == Author.User) Role.User else Role.Nep,
                text = body,
            )

        private fun AssistantUiState.appendMessage(message: ChatMessage): AssistantUiState.Conversation =
            when (this) {
                AssistantUiState.Welcome ->
                    AssistantUiState.Conversation(messages = listOf(message))
                is AssistantUiState.Conversation ->
                    copy(messages = messages + message)
            }

        private fun nowLabel(): String = TIMESTAMP_FORMAT.format(Date())

        // Random per-message handle so the LazyColumn key never collides
        // on repeated identical questions sent inside the same minute.
        private fun newId(): String = UUID.randomUUID().toString()

        companion object {
            const val MAX_USER_MESSAGE_LENGTH = 300
            private val TIMESTAMP_FORMAT = SimpleDateFormat("h:mm a", Locale.US)
        }
    }
