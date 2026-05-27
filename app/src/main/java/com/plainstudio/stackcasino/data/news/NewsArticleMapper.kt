package com.plainstudio.stackcasino.data.news

import com.plainstudio.stackcasino.domain.news.NewsArticle

/**
 * Maps the network DTO into the domain model. Drops articles where
 * NewsAPI returns nulls for fields that are not nullable on the
 * domain side (title, url, publishedAt, source name) so the UI never
 * has to defensively render empty cards.
 *
 * NewsAPI uses `"[Removed]"` as a sentinel for content stripped at
 * indexing time; those articles are dropped too because they cannot
 * be opened or read.
 */
internal fun NewsApiArticleDto.toDomainOrNull(): NewsArticle? {
    val cleanTitle = title?.cleaned()
    val cleanUrl = url?.cleaned()
    val cleanPublishedAt = publishedAt?.cleaned()
    val cleanSource = source.name?.cleaned()
    if (anyMissing(cleanTitle, cleanUrl, cleanPublishedAt, cleanSource)) return null
    return NewsArticle(
        id = cleanUrl!!,
        title = cleanTitle!!,
        source = cleanSource!!,
        description = description?.cleaned(),
        url = cleanUrl,
        imageUrl = urlToImage?.takeUnless { it.isBlank() },
        publishedAtIso = cleanPublishedAt!!,
    )
}

private fun anyMissing(vararg values: String?): Boolean = values.any { it == null }

private fun String.cleaned(): String? = takeUnless { isBlank() || this == REMOVED_SENTINEL }

private const val REMOVED_SENTINEL = "[Removed]"
