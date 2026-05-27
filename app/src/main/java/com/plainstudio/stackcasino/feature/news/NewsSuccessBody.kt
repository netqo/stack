package com.plainstudio.stackcasino.feature.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.ui.components.EmptyState
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Success body: the FEATURED swipeable carousel (HorizontalPager
 * over the first [CAROUSEL_SIZE] articles) followed by the LATEST
 * LazyColumn for the rest. The carousel is the project's "carousel"
 * deliverable; the LazyColumn is the TPO-required LazyColumn fed by
 * the Retrofit response.
 *
 * Search + source filtering reshape [NewsUiState.Success.filtered]
 * upstream; this composable just renders whatever subset is current
 * and shows an [EmptyState] when the filter has no matches.
 */
@Composable
internal fun NewsSuccessBody(
    state: NewsUiState.Success,
    onOpenArticle: (String) -> Unit,
) {
    val visible = state.filtered
    if (visible.isEmpty()) {
        EmptyFilterState()
        return
    }
    val featured = visible.take(CAROUSEL_SIZE)
    val latest = visible.drop(featured.size)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SectionVerticalPadding),
    ) {
        item("carousel") {
            FeaturedCarousel(featured = featured, onOpenArticle = onOpenArticle)
        }
        if (latest.isNotEmpty()) {
            item("latest-header") {
                LatestSectionHeader()
            }
            items(items = latest, key = { it.id }) { article ->
                LatestRow(article = article, onClick = { onOpenArticle(article.id) })
            }
        }
        item("footer") {
            ListFooter(total = state.allArticles.size, visible = visible.size)
        }
    }
}

@Composable
private fun FeaturedCarousel(
    featured: List<NewsArticle>,
    onOpenArticle: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { featured.size })
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenHorizontalPadding)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = CarouselPeek),
            pageSpacing = CarouselPageSpacing,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            FeaturedCard(article = featured[page], onClick = { onOpenArticle(featured[page].id) })
        }
        Spacer(modifier = Modifier.height(CarouselIndicatorTopGap))
        CarouselIndicator(pageCount = featured.size, currentPage = pagerState.currentPage)
    }
}

@Composable
private fun CarouselIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width = if (isActive) IndicatorActiveWidth else IndicatorDotSize
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = width, height = IndicatorDotSize)
                        .background(if (isActive) AccentViolet else SurfaceOutline),
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun FeaturedCard(
    article: NewsArticle,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(FEATURED_ASPECT_RATIO).background(SurfaceElevated)) {
            GlideImage(
                model = article.imageUrl,
                contentDescription = null,
                loading = placeholder(R.drawable.ic_news_placeholder),
                failure = placeholder(R.drawable.ic_news_placeholder),
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier =
                    Modifier
                        .padding(BadgeOffset)
                        .background(AccentViolet)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "FEATURED",
                    color = Color.White,
                    fontSize = BadgeFontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = TrackedLetterSpacing,
                )
            }
        }
        Column(modifier = Modifier.padding(FeaturedTextPadding)) {
            Text(
                text = article.title,
                color = TextHigh,
                fontSize = FeaturedTitleFontSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = FeaturedTitleLineHeight,
                maxLines = FEATURED_TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = article.source,
                    color = AccentViolet,
                    fontSize = MetaFontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = TrackedLetterSpacing,
                )
                Text(
                    text = formatPublishedAt(article.publishedAtIso),
                    color = TextLow,
                    fontSize = MetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                )
            }
        }
    }
}

@Composable
private fun LatestSectionHeader() {
    Text(
        text = "LATEST",
        color = TextMedium,
        fontSize = MetaFontSize,
        letterSpacing = TrackedLetterSpacing,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenHorizontalPadding,
                    end = ScreenHorizontalPadding,
                    top = LatestHeaderTopGap,
                    bottom = LatestHeaderBottomGap,
                ),
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LatestRow(
    article: NewsArticle,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = LatestRowVerticalPadding)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.size(LatestThumbSize).background(SurfaceElevated)) {
            GlideImage(
                model = article.imageUrl,
                contentDescription = null,
                loading = placeholder(R.drawable.ic_news_placeholder),
                failure = placeholder(R.drawable.ic_news_placeholder),
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(modifier = Modifier.weight(1f).padding(LatestRowTextPadding)) {
            Text(
                text = article.source,
                color = AccentViolet,
                fontSize = SmallMetaFontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.title,
                color = TextHigh,
                fontSize = LatestTitleFontSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = LatestTitleLineHeight,
                maxLines = LATEST_TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatPublishedAt(article.publishedAtIso),
                color = TextLow,
                fontSize = MetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
    }
}

@Composable
private fun ListFooter(
    total: Int,
    visible: Int,
) {
    val text =
        if (visible == total) "$total ARTICLES CACHED" else "SHOWING $visible OF $total ARTICLES"
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = FooterVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TextLow,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

@Composable
private fun EmptyFilterState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = null,
                    tint = TextLow,
                    modifier = Modifier.size(EmptyIconSize),
                )
            },
            title = "No articles match",
            message = "Try clearing the filters or your search query.",
        )
    }
}

// Surface a hint of the next page so users discover the carousel is
// swipeable; matches the typical mobile pager convention.
private val CarouselPeek = 24.dp
private val CarouselPageSpacing = 12.dp
private val CarouselIndicatorTopGap = 12.dp
private val IndicatorDotSize = 6.dp
private val IndicatorActiveWidth = 18.dp

private const val FEATURED_ASPECT_RATIO = 16f / 9f
private val FeaturedTextPadding = 16.dp
private val FeaturedTitleFontSize = 16.sp
private val FeaturedTitleLineHeight = 22.sp
private const val FEATURED_TITLE_MAX_LINES = 3

private val BadgeOffset = 12.dp
private val BadgeFontSize = 9.sp

private val LatestHeaderTopGap = 24.dp
private val LatestHeaderBottomGap = 8.dp
private val LatestRowVerticalPadding = 4.dp
private val LatestThumbSize = 112.dp
private val LatestRowTextPadding = 12.dp
private val LatestTitleFontSize = 14.sp
private val LatestTitleLineHeight = 20.sp
private const val LATEST_TITLE_MAX_LINES = 2

private val FooterVerticalPadding = 16.dp
private val EmptyIconSize = 24.dp

private const val CAROUSEL_SIZE = 5
