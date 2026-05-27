package com.plainstudio.stackcasino.data.news

import android.util.Log
import com.plainstudio.stackcasino.BuildConfig
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.domain.news.NewsRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NewsAPI-backed implementation of [NewsRepository].
 *
 * Caches the last successful fetch in a [ConcurrentHashMap] keyed by
 * article URL so [findById] can serve the detail screen without an
 * extra round-trip. The cache is process-scoped: when Room lands in
 * the next entrega it becomes the single source of truth and this
 * map drops out.
 *
 * Failures are logged under the NewsRepo tag with full stack traces
 * so a developer staring at logcat can tell a 401 (bad key) from a
 * 429 (rate limit) without having to wrap their own try/catch.
 */
@Singleton
class NewsRepositoryImpl
    @Inject
    constructor(
        private val service: NewsApiService,
    ) : NewsRepository {
        private val cache = ConcurrentHashMap<String, NewsArticle>()

        override suspend fun refresh(): Result<List<NewsArticle>> {
            if (BuildConfig.NEWSAPI_KEY.isBlank()) {
                Log.w(TAG, "NEWSAPI_KEY is empty; refusing to hit NewsAPI.")
                return Result.failure(IllegalStateException("NEWSAPI_KEY missing"))
            }
            return runCatching {
                val response = service.fetchEverything(query = NEWSAPI_CASINO_QUERY)
                if (response.status != "ok") {
                    error("NewsAPI returned ${response.status}: ${response.code} ${response.message}")
                }
                response.articles.mapNotNull { it.toDomainOrNull() }
            }.onSuccess { articles ->
                cache.clear()
                articles.forEach { cache[it.id] = it }
            }.onFailure { throwable ->
                Log.w(TAG, "NewsAPI refresh failed", throwable)
            }
        }

        override fun findById(id: String): NewsArticle? = cache[id]

        private companion object {
            const val TAG = "NewsRepo"
        }
    }

/**
 * Query string shipped to NewsAPI EP-01. Matches the project's
 * documented scope: anything at the intersection of casino /
 * gambling vocabulary and the crypto / Polygon stack the app runs
 * on top of.
 */
internal const val NEWSAPI_CASINO_QUERY =
    "(casino OR gambling OR blackjack OR roulette) AND (crypto OR blockchain OR polygon OR USDC)"
