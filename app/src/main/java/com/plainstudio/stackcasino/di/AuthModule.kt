package com.plainstudio.stackcasino.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.plainstudio.stackcasino.data.auth.AuthRepositoryImpl
import com.plainstudio.stackcasino.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI bindings for the authentication slice.
 *
 * Kept separate from [AppModule] so the auth surface stays cohesive
 * and the test rule (HiltTestRunner) can swap [AuthRepository] for a
 * fake without touching unrelated Firebase singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideCredentialManager(
            @ApplicationContext context: Context,
        ): CredentialManager = CredentialManager.create(context)
    }
}
