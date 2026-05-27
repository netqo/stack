package com.plainstudio.stackcasino.data.assistant

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.plainstudio.stackcasino.BuildConfig
import com.plainstudio.stackcasino.domain.assistant.AssistantRepository
import com.plainstudio.stackcasino.domain.assistant.ChatTurn
import com.plainstudio.stackcasino.domain.assistant.Role
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini-backed implementation of [AssistantRepository].
 *
 * The model is constructed once per process by [AssistantModule] and
 * carries the Nep system prompt as `systemInstruction`. Each call
 * builds a fresh single-shot chat from [history] so the SDK gets the
 * same prior context the user sees in the UI.
 *
 * Errors (missing key, quota, network) collapse to [Result.failure];
 * the ViewModel decides what to render in the chat bubble. Failures
 * are also logged with their full stack trace so a developer staring
 * at logcat can tell a 401 (bad key) from a network unreachable in
 * one glance instead of seeing only the generic "Something went wrong"
 * bubble.
 */
@Singleton
class GeminiAssistantRepository
    @Inject
    constructor(
        private val model: GenerativeModel,
    ) : AssistantRepository {
        override suspend fun sendMessage(
            history: List<ChatTurn>,
            userMessage: String,
        ): Result<String> {
            if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                Log.w(TAG, "GEMINI_API_KEY is empty; aborting before hitting the SDK.")
                return Result.failure(IllegalStateException("GEMINI_API_KEY missing"))
            }
            return runCatching {
                val chat = model.startChat(history = history.toGeminiHistory())
                val response = chat.sendMessage(userMessage)
                response.text?.takeIf { it.isNotBlank() }
                    ?: error("Gemini returned an empty response.")
            }.onFailure { throwable ->
                Log.w(TAG, "Nep request failed", throwable)
            }
        }

        private fun List<ChatTurn>.toGeminiHistory(): List<Content> =
            map { turn ->
                content(role = turn.role.geminiRole()) { text(turn.text) }
            }

        private fun Role.geminiRole(): String =
            when (this) {
                Role.User -> "user"
                Role.Nep -> "model"
            }

        private companion object {
            const val TAG = "AssistantRepo"
        }
    }
