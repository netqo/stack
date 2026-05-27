package com.plainstudio.stackcasino.domain.news

/**
 * Domain boundary for the news feed.
 *
 * The implementation owns whichever cache strategy makes sense for
 * the deliverable: the second submission keeps the latest fetch in
 * memory; the final submission swaps the backing store for Room
 * without touching the feature layer (offline-first as required by
 * the TPO).
 *
 * Failures bubble up as a failed [Result] so the ViewModel decides
 * whether to surface the Error state or render stale cached items
 * underneath the error card.
 */
interface NewsRepository {
    /**
     * Returns the freshest set of articles. Always hits the network
     * for now; the cache TTL guard ships with the Room layer in the
     * next entrega.
     */
    suspend fun refresh(): Result<List<NewsArticle>>

    /**
     * Looks an article up by [id] inside whatever cache the last
     * [refresh] populated. Returns null if the cache is empty or the
     * article was never seen (e.g. user opened a deep link the cache
     * does not cover yet).
     */
    fun findById(id: String): NewsArticle?
}
