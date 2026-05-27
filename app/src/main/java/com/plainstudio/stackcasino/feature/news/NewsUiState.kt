package com.plainstudio.stackcasino.feature.news

import com.plainstudio.stackcasino.domain.news.NewsArticle

/**
 * UI state for the news feed.
 *
 *   * [Loading] -> first fetch in flight with no cache to fall back on.
 *   * [Success] -> the fetch succeeded; [filtered] is the live filtered
 *     view (after applying [query] + [selectedSource]) over [allArticles].
 *   * [Error] -> the fetch failed and the cache is empty; the screen
 *     shows the centred error card with a retry CTA.
 *
 * Search and source filtering happen in the ViewModel against the
 * cached list so typing does not burn the daily NewsAPI quota.
 */
sealed interface NewsUiState {
    data object Loading : NewsUiState

    data class Success(
        val allArticles: List<NewsArticle>,
        val filtered: List<NewsArticle>,
        val sources: List<String>,
        val query: String,
        val selectedSource: SourceFilter,
    ) : NewsUiState

    data class Error(
        val message: String,
    ) : NewsUiState
}

/**
 * Either the wildcard "All" pill or a specific publisher name. Kept
 * as a sealed type so the chip row can pattern-match without leaning
 * on a magic null or sentinel string.
 */
sealed interface SourceFilter {
    data object All : SourceFilter

    data class Named(
        val name: String,
    ) : SourceFilter
}
