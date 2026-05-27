package com.plainstudio.stackcasino.feature.news

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.domain.news.NewsRepository
import com.plainstudio.stackcasino.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Detail-screen state machine.
 *
 * Only two real states: the article is in the repository cache and
 * we render it, or it is not (the user opened the deep link before
 * the feed loaded) and we render a "Not found" empty state.
 *
 * No fetch happens here: the second-submission scope keeps everything
 * the user can navigate to under the umbrella of the most recent
 * [NewsRepository.refresh] from the feed screen.
 */
@HiltViewModel
class NewsDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        repository: NewsRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                savedStateHandle
                    .get<String>(Route.NewsDetail.ARG_ARTICLE_ID)
                    ?.let { Uri.decode(it) }
                    ?.let { repository.findById(it) }
                    ?.let { NewsDetailUiState.Loaded(it) }
                    ?: NewsDetailUiState.NotFound,
            )
        val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()
    }

sealed interface NewsDetailUiState {
    data object NotFound : NewsDetailUiState

    data class Loaded(
        val article: NewsArticle,
    ) : NewsDetailUiState
}
