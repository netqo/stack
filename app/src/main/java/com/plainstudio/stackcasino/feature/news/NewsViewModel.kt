package com.plainstudio.stackcasino.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.domain.news.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the news feed.
 *
 *   * On construction the VM transitions to [NewsUiState.Loading]
 *     and triggers the first [NewsRepository.refresh].
 *   * Success populates the cached articles list, computes the unique
 *     set of sources (used as filter chips) and recomputes the
 *     filtered view.
 *   * Failure transitions to [NewsUiState.Error] so the screen can
 *     render the retry card.
 *
 * Search and source filtering are applied in-memory over the cached
 * articles; neither dispatches a new network call. The TPO limits us
 * to 100 requests / day so we only refetch on explicit retry.
 */
@HiltViewModel
class NewsViewModel
    @Inject
    constructor(
        private val repository: NewsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
        val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            _uiState.value = NewsUiState.Loading
            viewModelScope.launch {
                repository.refresh().fold(
                    onSuccess = { articles -> _uiState.value = articles.toSuccessState() },
                    onFailure = { throwable ->
                        _uiState.value =
                            NewsUiState.Error(
                                message =
                                    throwable.message
                                        ?: "Couldn't reach NewsAPI right now. Try again in a moment.",
                            )
                    },
                )
            }
        }

        fun onQueryChange(query: String) {
            _uiState.update { current ->
                if (current is NewsUiState.Success) current.copy(query = query).recomputeFiltered() else current
            }
        }

        fun onSourceChange(source: SourceFilter) {
            _uiState.update { current ->
                if (current is NewsUiState.Success) {
                    current.copy(selectedSource = source).recomputeFiltered()
                } else {
                    current
                }
            }
        }

        // ------------------------------------------------------------------
        // Helpers
        // ------------------------------------------------------------------

        private fun List<NewsArticle>.toSuccessState(): NewsUiState.Success {
            val sources =
                map { it.source }
                    .distinct()
                    .sortedBy { it.lowercase() }
            return NewsUiState.Success(
                allArticles = this,
                filtered = this,
                sources = sources,
                query = "",
                selectedSource = SourceFilter.All,
            )
        }

        private fun NewsUiState.Success.recomputeFiltered(): NewsUiState.Success {
            val trimmed = query.trim()
            val filtered =
                allArticles.filter { article ->
                    article.matches(selectedSource) && article.matches(trimmed)
                }
            return copy(filtered = filtered)
        }

        private fun NewsArticle.matches(filter: SourceFilter): Boolean =
            when (filter) {
                SourceFilter.All -> true
                is SourceFilter.Named -> source.equals(filter.name, ignoreCase = true)
            }

        private fun NewsArticle.matches(query: String): Boolean =
            if (query.isEmpty()) {
                true
            } else {
                title.contains(query, ignoreCase = true) ||
                    source.contains(query, ignoreCase = true)
            }
    }
