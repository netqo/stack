package com.plainstudio.stackcasino.domain.assistant

/**
 * Single turn in a Nep conversation. The data layer translates this
 * to whatever the underlying provider (Gemini today, anything else
 * tomorrow) needs to model multi-turn context.
 */
data class ChatTurn(
    val role: Role,
    val text: String,
)

enum class Role { User, Nep }

/**
 * Domain boundary for the in-app casino assistant. The implementation
 * decides which provider, which model, and how to keep the persona
 * consistent; the ViewModel only sees turns in / response text out.
 */
interface AssistantRepository {
    /**
     * Sends [userMessage] to Nep with [history] as prior context and
     * returns Nep's reply.
     *
     * Failures (network, quota, missing API key, content policy) are
     * surfaced as a failed [Result] so the caller can choose how to
     * render them without exception-bubbling across the layer boundary.
     */
    suspend fun sendMessage(
        history: List<ChatTurn>,
        userMessage: String,
    ): Result<String>
}
