package com.plainstudio.stackcasino.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.plainstudio.stackcasino.BuildConfig
import com.plainstudio.stackcasino.data.assistant.GeminiAssistantRepository
import com.plainstudio.stackcasino.data.assistant.NEP_SYSTEM_PROMPT
import com.plainstudio.stackcasino.domain.assistant.AssistantRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI bindings for the Nep assistant slice.
 *
 * Holds the singleton [GenerativeModel] (configured with the Nep
 * system prompt) and binds [GeminiAssistantRepository] to the
 * [AssistantRepository] domain interface.
 *
 * The API key is read from `BuildConfig.GEMINI_API_KEY`, which the
 * Gradle script wires from `local.properties` (gitignored). If the
 * key is empty the SDK will still build, and the first request will
 * fail loudly with a 4xx that the repository surfaces as a failed
 * Result for the UI to render.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AssistantModule {
    @Binds
    @Singleton
    abstract fun bindAssistantRepository(impl: GeminiAssistantRepository): AssistantRepository

    companion object {
        @Provides
        @Singleton
        fun provideGenerativeModel(): GenerativeModel =
            GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = BuildConfig.GEMINI_API_KEY,
                systemInstruction = content { text(NEP_SYSTEM_PROMPT) },
            )

        // gemini-2.5-flash is Google's officially recommended free
        // tier workhorse (10 RPM, 250 RPD) and ships with the broadest
        // regional coverage. The 2.0 family returned "limit: 0" on
        // fresh AI Studio projects (probably geo-restricted) and the
        // bare 1.5-flash alias was retired from the v1beta endpoint
        // this SDK targets.
        private const val MODEL_NAME = "gemini-2.5-flash"
    }
}
