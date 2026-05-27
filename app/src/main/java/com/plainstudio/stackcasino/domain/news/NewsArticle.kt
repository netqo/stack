package com.plainstudio.stackcasino.domain.news

/**
 * Domain-side article. The data layer maps NewsAPI's JSON envelope to
 * this shape before crossing the layer boundary so the feature layer
 * never imports a Retrofit DTO directly.
 *
 * [id] is the article URL: NewsAPI does not expose a stable numeric
 * identifier and the URL is already unique per article in their
 * dataset, so reusing it lets the detail screen look the article up
 * in the repository cache without an extra index.
 *
 * [imageUrl] and [description] are nullable because NewsAPI omits
 * them for some sources (typically obscure ones); Glide falls back
 * to a placeholder in that case.
 */
data class NewsArticle(
    val id: String,
    val title: String,
    val source: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAtIso: String,
)
