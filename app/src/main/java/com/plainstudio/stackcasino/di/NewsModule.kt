package com.plainstudio.stackcasino.di

import com.plainstudio.stackcasino.BuildConfig
import com.plainstudio.stackcasino.data.news.NewsApiService
import com.plainstudio.stackcasino.data.news.NewsRepositoryImpl
import com.plainstudio.stackcasino.domain.news.NewsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * DI bindings for the news slice.
 *
 * Wires the Retrofit + OkHttp + Moshi stack the TPO mandates for the
 * second submission, attaches the NewsAPI key as a header on every
 * request (so the [NewsApiService] interface stays free of header
 * arguments), and binds [NewsRepositoryImpl] to the
 * [NewsRepository] domain interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NewsModule {
    @Binds
    @Singleton
    abstract fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository

    companion object {
        @Provides
        @Singleton
        fun provideMoshi(): Moshi =
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        @Provides
        @Singleton
        fun provideNewsApiKeyInterceptor(): Interceptor =
            Interceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header("X-Api-Key", BuildConfig.NEWSAPI_KEY)
                        .build()
                chain.proceed(request)
            }

        @Provides
        @Singleton
        fun provideOkHttpClient(apiKeyInterceptor: Interceptor): OkHttpClient {
            // Basic logging in debug only; release builds keep the log
            // noise minimal and never echo response bodies.
            val loggingLevel =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            val logging = HttpLoggingInterceptor().apply { level = loggingLevel }
            return OkHttpClient
                .Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(logging)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(
            client: OkHttpClient,
            moshi: Moshi,
        ): Retrofit =
            Retrofit
                .Builder()
                .baseUrl(NEWSAPI_BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        @Provides
        @Singleton
        fun provideNewsApiService(retrofit: Retrofit): NewsApiService = retrofit.create(NewsApiService::class.java)

        private const val NEWSAPI_BASE_URL = "https://newsapi.org/v2/"
    }
}
