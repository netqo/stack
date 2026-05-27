package com.plainstudio.stackcasino.feature.news

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.domain.news.NewsArticle
import com.plainstudio.stackcasino.ui.components.EmptyState
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Detail screen for a single news article (cu-13).
 *
 *   * Reads the article id from the back-stack arguments, looks it up
 *     in the [com.plainstudio.stackcasino.domain.news.NewsRepository]
 *     cache and renders the hero + body or a "not found" empty state.
 *   * Top bar mirrors the assistant header: back chip, the article
 *     source as the title, and a share chip on the right that fires
 *     `Intent.ACTION_SEND` with `"$title - $url"`.
 *   * Article body ends with a "Read full article" button that opens
 *     the source URL in the browser via `Intent.ACTION_VIEW`.
 */
@Composable
fun NewsDetailScreen(
    onBack: () -> Unit,
    viewModel: NewsDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBase) {
        when (val current = state) {
            NewsDetailUiState.NotFound -> NotFoundBody(onBack = onBack)
            is NewsDetailUiState.Loaded -> LoadedBody(article = current.article, onBack = onBack)
        }
    }
}

@Composable
private fun LoadedBody(
    article: NewsArticle,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        DetailHeader(
            sourceLabel = article.source,
            onBack = onBack,
            onShare = { context.shareArticle(article) },
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = BottomScrollPadding),
        ) {
            HeroImage(imageUrl = article.imageUrl)
            ArticleBody(article = article, onReadFull = { context.openInBrowser(article.url) })
        }
    }
}

@Composable
private fun DetailHeader(
    sourceLabel: String,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HeaderHorizontalPadding, vertical = HeaderVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconChip(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "NEWS",
                color = TextLow,
                fontSize = HeaderEyebrowFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Text(
                text = sourceLabel,
                color = TextHigh,
                fontSize = HeaderTitleFontSize,
                fontWeight = FontWeight.Bold,
            )
        }
        IconChip(
            icon = Icons.Outlined.IosShare,
            contentDescription = "Share article",
            onClick = onShare,
        )
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceOutline))
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HeroImage(imageUrl: String?) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(HERO_ASPECT_RATIO).background(SurfaceElevated)) {
        GlideImage(
            model = imageUrl,
            contentDescription = null,
            loading = placeholder(R.drawable.ic_news_placeholder),
            failure = placeholder(R.drawable.ic_news_placeholder),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ArticleBody(
    article: NewsArticle,
    onReadFull: () -> Unit,
) {
    Column(modifier = Modifier.padding(BodyPadding)) {
        Text(
            text = article.title,
            color = TextHigh,
            fontSize = TitleFontSize,
            fontWeight = FontWeight.Bold,
            lineHeight = TitleLineHeight,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = formatPublishedAt(article.publishedAtIso),
            color = TextLow,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        if (!article.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = article.description,
                color = TextMedium,
                fontSize = BodyFontSize,
                lineHeight = BodyLineHeight,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        ReadFullArticleButton(onClick = onReadFull)
    }
}

@Composable
private fun ReadFullArticleButton(onClick: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(AccentViolet)
                .clickable(onClick = onClick)
                .padding(ReadFullPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "READ FULL ARTICLE",
            color = Color.White,
            fontSize = ReadFullFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Icon(
            imageVector = Icons.Outlined.OpenInNew,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(ReadFullIconSize),
        )
    }
}

@Composable
private fun NotFoundBody(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_news_placeholder),
                    contentDescription = null,
                    tint = TextLow,
                    modifier = Modifier.size(NotFoundIconSize),
                )
            },
            title = "Article not found",
            message = "The article isn't in your latest feed cache anymore. Refresh the news list and try again.",
            actionLabel = "Back to news",
            onAction = onBack,
        )
    }
}

@Composable
private fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(IconChipSize)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextHigh,
            modifier = Modifier.size(IconChipIconSize),
        )
    }
}

private fun Context.shareArticle(article: NewsArticle) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, "${article.title} - ${article.url}")
        }
    startActivity(Intent.createChooser(send, null))
}

private fun Context.openInBrowser(url: String) {
    val view = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    startActivity(view)
}

private val HeaderHorizontalPadding = 16.dp
private val HeaderVerticalPadding = 12.dp
private val HeaderEyebrowFontSize = 9.sp
private val HeaderTitleFontSize = 18.sp

private val IconChipSize = 36.dp
private val IconChipIconSize = 16.dp

private const val HERO_ASPECT_RATIO = 16f / 9f
private val BodyPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp)
private val TitleFontSize = 22.sp
private val TitleLineHeight = 28.sp
private val BodyFontSize = 14.sp
private val BodyLineHeight = 22.sp
private val BottomScrollPadding = 32.dp

private val ReadFullPadding = PaddingValues(vertical = 14.dp)
private val ReadFullFontSize = 12.sp
private val ReadFullIconSize = 14.dp
private val NotFoundIconSize = 24.dp

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun NewsDetailLoadedPreview() {
    StackcasinoTheme {
        LoadedBody(
            article =
                NewsArticle(
                    id = "preview-id",
                    title = "Bitcoin Surges Past $70,000 as Institutional Interest Grows",
                    source = "CryptoNews",
                    description =
                        "Major fund managers continued to expand their crypto allocations " +
                            "this week as Bitcoin breached the $70,000 threshold.",
                    url = "https://example.com",
                    imageUrl = null,
                    publishedAtIso = "2026-04-16T07:30:00Z",
                ),
            onBack = {},
        )
    }
}
