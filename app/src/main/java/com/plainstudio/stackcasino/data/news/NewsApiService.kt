package com.plainstudio.stackcasino.data.news

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit binding for NewsAPI.org EP-01 ("/everything").
 *
 * The query is kept narrow at construction time
 * ([NEWSAPI_CASINO_QUERY] in the Hilt module) because NewsAPI charges
 * per-request and the free Developer tier caps at 100 / day. The
 * single endpoint is enough for the second submission: search and
 * filtering operate over the cached response, not over additional
 * round-trips.
 */
interface NewsApiService {
    @GET("everything")
    suspend fun fetchEverything(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = DEFAULT_PAGE_SIZE,
    ): NewsApiResponseDto

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}

@JsonClass(generateAdapter = true)
data class NewsApiResponseDto(
    val status: String,
    val totalResults: Int = 0,
    val articles: List<NewsApiArticleDto> = emptyList(),
    val code: String? = null,
    val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class NewsApiArticleDto(
    val source: NewsApiSourceDto,
    val title: String?,
    val description: String?,
    val url: String?,
    val urlToImage: String?,
    val publishedAt: String?,
)

@JsonClass(generateAdapter = true)
data class NewsApiSourceDto(
    val id: String?,
    val name: String?,
)
