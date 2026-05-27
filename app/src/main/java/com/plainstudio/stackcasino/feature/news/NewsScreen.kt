package com.plainstudio.stackcasino.feature.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plainstudio.stackcasino.ui.components.ErrorState
import com.plainstudio.stackcasino.ui.components.ErrorStateDefaults
import com.plainstudio.stackcasino.ui.components.FilterChip
import com.plainstudio.stackcasino.ui.components.FilterChipRow
import com.plainstudio.stackcasino.ui.components.gridBackground
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow

/**
 * News screen reproducing the cu-12 mockup
 * (mockup/js/screens/news.js). Owns:
 *
 *   * the header + reactive search input + dynamic source filter chips,
 *   * a state branch on [NewsUiState] (Loading / Success / Error),
 *   * a featured-articles carousel (HorizontalPager) at the top of the
 *     Success body and a LazyColumn for the rest of the LATEST list.
 *
 * Per the TPO doc the screen mandates Loading / Success / Error
 * states, Retrofit for the listing, reactive search + filter, Glide
 * for image rendering and a parametric Detail route — all wired here.
 */
@Composable
fun NewsScreen(
    onOpenArticle: (articleId: String) -> Unit,
    viewModel: NewsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NewsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSourceChange = viewModel::onSourceChange,
        onRetry = viewModel::refresh,
        onOpenArticle = onOpenArticle,
    )
}

@Composable
private fun NewsContent(
    state: NewsUiState,
    onQueryChange: (String) -> Unit,
    onSourceChange: (SourceFilter) -> Unit,
    onRetry: () -> Unit,
    onOpenArticle: (String) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBase) {
        Column(modifier = Modifier.fillMaxSize().gridBackground()) {
            Header()
            HorizontalDivider()
            SearchInput(
                value = (state as? NewsUiState.Success)?.query.orEmpty(),
                enabled = state is NewsUiState.Success,
                onValueChange = onQueryChange,
            )
            if (state is NewsUiState.Success) {
                SourceChips(
                    sources = state.sources,
                    selected = state.selectedSource,
                    onSelect = onSourceChange,
                )
            }
            when (state) {
                NewsUiState.Loading -> NewsLoadingBody()
                is NewsUiState.Success ->
                    NewsSuccessBody(state = state, onOpenArticle = onOpenArticle)
                is NewsUiState.Error -> NewsErrorBody(state = state, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenHorizontalPadding,
                    end = ScreenHorizontalPadding,
                    top = HeaderTopPadding,
                    bottom = HeaderBottomPadding,
                ),
    ) {
        Text(
            text = "News",
            color = TextHigh,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SearchInput(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SearchVerticalPadding)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(SearchIconColumnWidth).height(SearchInputHeight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextLow,
                modifier = Modifier.size(SearchIconSize),
            )
        }
        Box(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = enabled,
                textStyle = TextStyle(color = TextHigh, fontSize = SearchFontSize),
                cursorBrush = SolidColor(AccentViolet),
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isEmpty()) {
                Text(
                    text = "Search articles...",
                    color = TextLow,
                    fontSize = SearchFontSize,
                )
            }
        }
    }
}

@Composable
private fun SourceChips(
    sources: List<String>,
    selected: SourceFilter,
    onSelect: (SourceFilter) -> Unit,
) {
    val chips =
        buildList<FilterChip<SourceFilter>> {
            add(FilterChip(key = SourceFilter.All, label = "All sources"))
            sources.forEach { source ->
                add(FilterChip(key = SourceFilter.Named(source), label = source))
            }
        }
    FilterChipRow(
        chips = chips,
        selected = selected,
        onSelect = onSelect,
        scrollable = true,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SourceChipsVerticalPadding),
    )
}

@Composable
private fun NewsErrorBody(
    state: NewsUiState.Error,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        ErrorState(
            icon = { ErrorStateDefaults.OfflineIcon() },
            title = "Couldn't load articles",
            message = state.message,
            primaryActionLabel = "Retry",
            onPrimaryAction = onRetry,
        )
    }
}

@Composable
internal fun HorizontalDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceOutline),
    )
}

// ---------------------------------------------------------------------------
// Tokens shared across the news feature files.
// ---------------------------------------------------------------------------

internal val ScreenHorizontalPadding = 16.dp
internal val SectionVerticalPadding = 16.dp
internal val MetaFontSize = 10.sp
internal val SmallMetaFontSize = 9.sp
internal val TrackedLetterSpacing = 1.2.sp

private val HeaderTopPadding = 24.dp
private val HeaderBottomPadding = 16.dp
private val SearchVerticalPadding = 16.dp
private val SearchIconColumnWidth = 44.dp
private val SearchInputHeight = 40.dp
private val SearchIconSize = 16.dp
private val SearchFontSize = 14.sp
private val SourceChipsVerticalPadding = 8.dp
