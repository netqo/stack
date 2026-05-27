package com.plainstudio.stackcasino.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.model.GameKey
import com.plainstudio.stackcasino.model.RoundOutcome
import com.plainstudio.stackcasino.ui.components.EmptyState
import com.plainstudio.stackcasino.ui.components.FilterChip
import com.plainstudio.stackcasino.ui.components.FilterChipRow
import com.plainstudio.stackcasino.ui.components.StackCard
import com.plainstudio.stackcasino.ui.components.gridBackground
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * History screen reproducing the cu-10 mockup
 * (mockup/js/screens/history.js). Owns the screen-local search and
 * filter state; the summary strip aggregates over the full unfiltered
 * round set (matching the mockup where the strip shows 128 rounds
 * even with only a handful visible below).
 */
@Composable
fun HistoryScreen(
    data: HistoryData,
    onOpenRound: (roundId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var gameFilter by rememberSaveable { mutableStateOf(GameFilter.All) }
    var resultFilter by rememberSaveable { mutableStateOf(ResultFilter.All) }

    val visibleRounds by remember(data.rounds, query, gameFilter, resultFilter) {
        derivedStateOf { data.rounds.filter { it.matches(query, gameFilter, resultFilter) } }
    }

    Surface(modifier = modifier.fillMaxSize(), color = SurfaceBase) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .gridBackground()
                    .verticalScroll(rememberScrollState()),
        ) {
            HistoryHeader()
            HorizontalDivider()
            SearchInput(value = query, onValueChange = { query = it })
            FiltersBlock(
                gameFilter = gameFilter,
                onGameFilterChange = { gameFilter = it },
                resultFilter = resultFilter,
                onResultFilterChange = { resultFilter = it },
            )
            Spacer(modifier = Modifier.height(SectionGap))
            SummaryStrip(summary = data.summary)
            RoundList(
                rounds = visibleRounds,
                onRoundTap = { round -> onOpenRound(round.id) },
            )
            Spacer(modifier = Modifier.height(BottomScrollPadding))
        }
    }
}

@Composable
private fun HistoryHeader() {
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
            text = "Game History",
            color = TextHigh,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SearchInput(
    value: String,
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
                textStyle = TextStyle(color = TextHigh, fontSize = SearchFontSize),
                cursorBrush = SolidColor(AccentViolet),
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isEmpty()) {
                Text(
                    text = "Search rounds...",
                    color = TextLow,
                    fontSize = SearchFontSize,
                )
            }
        }
    }
}

@Composable
private fun FiltersBlock(
    gameFilter: GameFilter,
    onGameFilterChange: (GameFilter) -> Unit,
    resultFilter: ResultFilter,
    onResultFilterChange: (ResultFilter) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(FilterGroupGap),
    ) {
        FilterGroup(
            label = "Game",
            chips = GameFilter.entries.map { FilterChip(key = it, label = it.label) },
            selected = gameFilter,
            onSelect = onGameFilterChange,
            wrap = true,
        )
        FilterGroup(
            label = "Result",
            chips = ResultFilter.entries.map { FilterChip(key = it, label = it.label) },
            selected = resultFilter,
            onSelect = onResultFilterChange,
            wrap = false,
        )
    }
}

@Composable
private fun <T> FilterGroup(
    label: String,
    chips: List<FilterChip<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    wrap: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label.uppercase(),
            color = TextMedium,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        FilterChipRow(
            chips = chips,
            selected = selected,
            onSelect = onSelect,
            wrap = wrap,
        )
    }
}

@Composable
private fun SummaryStrip(summary: HistorySummary) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline),
    ) {
        SummaryCell(
            label = "Rounds",
            value = summary.totalRounds.toString(),
            valueColor = TextHigh,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider()
        SummaryCell(
            label = "Win Rate",
            value = "${summary.winRatePercent}%",
            valueColor = SemanticOk,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider()
        SummaryCell(
            label = "Net",
            value = summary.netLabel,
            valueColor = AccentViolet,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(SummaryCellPadding)) {
        Text(
            text = label.uppercase(),
            color = TextMedium,
            fontSize = SmallMetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = SummaryValueFontSize,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun RoundList(
    rounds: List<HistoryRound>,
    onRoundTap: (HistoryRound) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionGap),
        verticalArrangement = Arrangement.spacedBy(RoundGap),
    ) {
        if (rounds.isEmpty()) {
            EmptyRoundsState()
        } else {
            rounds.forEach { round -> RoundCard(round = round, onClick = { onRoundTap(round) }) }
            EndOfResultsSentinel()
        }
    }
}

@Composable
private fun RoundCard(
    round: HistoryRound,
    onClick: () -> Unit,
) {
    val accent = if (round.outcome == RoundOutcome.Win) SemanticOk else SemanticDanger
    StackCard(
        modifier = Modifier.fillMaxWidth(),
        leftAccent = accent,
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column {
            RoundHeader(round = round)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SurfaceOutline),
            )
            Spacer(modifier = Modifier.height(12.dp))
            RoundMetricsRow(round = round)
        }
    }
}

@Composable
private fun RoundHeader(round: HistoryRound) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(RoundIconBoxSize)
                    .border(width = 1.dp, color = SurfaceOutline),
            contentAlignment = Alignment.Center,
        ) {
            RoundIcon(game = round.game)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = round.game.label(),
                color = TextHigh,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = round.timestampLabel,
                color = TextLow,
                fontSize = TimestampFontSize,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextLow,
            modifier = Modifier.size(ChevronSize).padding(top = 2.dp),
        )
    }
}

@Composable
private fun RoundIcon(game: GameKey) {
    if (game == GameKey.Coinflip) {
        // Coinflip uses an inline x2 glyph instead of a drawable so the
        // history list matches the mockup's font-mono badge.
        Text(
            text = "x2",
            color = AccentViolet,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    } else {
        Icon(
            painter = painterResource(game.iconRes()),
            contentDescription = null,
            tint = AccentViolet,
            modifier = Modifier.size(RoundIconSize),
        )
    }
}

@Composable
private fun RoundMetricsRow(round: HistoryRound) {
    val payoutColor =
        when {
            round.outcome == RoundOutcome.Loss -> TextLow
            else -> SemanticOk
        }
    val multiplierColor =
        if (round.outcome == RoundOutcome.Loss) TextLow else AccentViolet
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MetricCell(label = "Bet", value = round.betLabel, valueColor = TextHigh, modifier = Modifier.weight(1f))
        MetricCell(
            label = "Payout",
            value = round.payoutLabel,
            valueColor = payoutColor,
            modifier = Modifier.weight(1f),
        )
        MetricCell(
            label = round.thirdMetricLabel(),
            value = round.multiplierLabel,
            valueColor = multiplierColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            color = TextMedium,
            fontSize = SmallMetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = MetricFontSize,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun EmptyRoundsState() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
        EmptyState(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = TextLow,
                    modifier = Modifier.size(EmptyIconSize),
                )
            },
            title = "No rounds match",
            message = "Try clearing the filters or your search query.",
        )
    }
}

@Composable
private fun EndOfResultsSentinel() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "END OF RESULTS",
            color = TextLow,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HorizontalDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceOutline))
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(SummaryDividerHeight).background(SurfaceOutline))
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun HistoryRound.matches(
    query: String,
    gameFilter: GameFilter,
    resultFilter: ResultFilter,
): Boolean {
    if (gameFilter.match != null && game != gameFilter.match) return false
    if (resultFilter.match != null && outcome != resultFilter.match) return false
    if (query.isBlank()) return true
    val trimmed = query.trim()
    return game.label().contains(trimmed, ignoreCase = true) ||
        id.contains(trimmed, ignoreCase = true)
}

private fun HistoryRound.thirdMetricLabel(): String = if (game == GameKey.Coinflip) "Prediction" else "Multiplier"

private fun GameKey.label(): String =
    when (this) {
        GameKey.Roulette -> "Roulette"
        GameKey.Blackjack -> "Blackjack"
        GameKey.Crash -> "Crash"
        GameKey.Mines -> "Mines"
        GameKey.Coinflip -> "Coinflip"
    }

private fun GameKey.iconRes(): Int =
    when (this) {
        GameKey.Roulette -> R.drawable.ic_game_roulette
        GameKey.Blackjack -> R.drawable.ic_game_blackjack
        GameKey.Crash -> R.drawable.ic_game_crash
        GameKey.Mines -> R.drawable.ic_game_mines
        GameKey.Coinflip -> R.drawable.ic_game_coinflip
    }

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

private val ScreenHorizontalPadding = 16.dp
private val SectionGap = 16.dp
private val HeaderTopPadding = 24.dp
private val HeaderBottomPadding = 16.dp
private val BottomScrollPadding = 96.dp

private val SearchVerticalPadding = 16.dp
private val SearchIconColumnWidth = 44.dp
private val SearchInputHeight = 40.dp
private val SearchIconSize = 16.dp
private val SearchFontSize = 14.sp

private val FilterGroupGap = 12.dp
private val MetaFontSize = 10.sp
private val SmallMetaFontSize = 9.sp
private val TrackedLetterSpacing = 1.2.sp

private val SummaryCellPadding = 12.dp
private val SummaryValueFontSize = 18.sp
private val SummaryDividerHeight = 56.dp

private val RoundGap = 8.dp
private val RoundIconBoxSize = 40.dp
private val RoundIconSize = 18.dp
private val ChevronSize = 14.dp
private val TimestampFontSize = 11.sp
private val MetricFontSize = 14.sp
private val EmptyIconSize = 24.dp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 1400)
@Composable
private fun HistoryScreenPreview() {
    StackcasinoTheme {
        HistoryScreen(data = historyPreviewData(), onOpenRound = {})
    }
}
