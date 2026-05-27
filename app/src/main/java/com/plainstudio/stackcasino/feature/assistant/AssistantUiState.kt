package com.plainstudio.stackcasino.feature.assistant

/**
 * UI state for the Nep assistant chat.
 *
 *   * [Welcome] is the empty / first-launch state: the hero card with
 *     the suggestion chips and the static legal blurb.
 *   * [Conversation] takes over the moment the user sends the first
 *     message; [isNepTyping] gates the three-dot indicator.
 *
 * The conversation lives entirely in-memory and resets when the user
 * taps the clear button or the screen is destroyed: matches the
 * mockup footer "Conversation is kept in memory - discarded on close".
 */
sealed interface AssistantUiState {
    data object Welcome : AssistantUiState

    data class Conversation(
        val messages: List<ChatMessage>,
        val isNepTyping: Boolean = false,
    ) : AssistantUiState
}

/**
 * Single message rendered in the chat scroll.
 *
 * [id] is a per-message stable handle the LazyColumn uses as its key
 * so repeated identical questions (same body, same minute) don't
 * collide on a content-derived key and crash the list.
 *
 * [isError] only flips when [author] is [Author.Nep] and the
 * underlying call failed; the chat bubble is then painted with the
 * danger accent so the user can spot the failed turn at a glance.
 */
data class ChatMessage(
    val id: String,
    val author: Author,
    val body: String,
    val timestampLabel: String,
    val isError: Boolean = false,
)

enum class Author { User, Nep }
