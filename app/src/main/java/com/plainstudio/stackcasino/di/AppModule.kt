package com.plainstudio.stackcasino.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Application-scoped Hilt bindings.
 *
 * Empty by design: this card lands only the DI scaffold. Downstream
 * cards (Firebase, Retrofit, Room, Gemini, etc.) add their providers
 * to this module so the dependency graph stays centralized.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
