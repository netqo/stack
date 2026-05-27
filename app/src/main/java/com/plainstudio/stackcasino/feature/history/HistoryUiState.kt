package com.plainstudio.stackcasino.feature.history

import com.plainstudio.stackcasino.model.GameKey
import com.plainstudio.stackcasino.model.RoundOutcome

/**
 * Aggregate snapshot the history screen renders. A static
 * [historyPreviewData] seeds both the @Preview composables and the
 * live nav-host entry until the Firestore-backed VM ships.
 *
 * [summary] is the aggregate over the full unfiltered round set
 * (mockup spec shows 128 rounds even with only five visible); the
 * screen-local filter + search apply to [rounds] only.
 */
data class HistoryData(
    val summary: HistorySummary,
    val rounds: List<HistoryRound>,
)

data class HistorySummary(
    val totalRounds: Int,
    val winRatePercent: Int,
    val netLabel: String,
)

/**
 * Single round row. [multiplierLabel] becomes a Prediction label for
 * coinflip ([multiplierLabel] always carries the right-most cell's
 * value, the heading is decided by [game]).
 */
data class HistoryRound(
    val id: String,
    val game: GameKey,
    val timestampLabel: String,
    val betLabel: String,
    val payoutLabel: String,
    val multiplierLabel: String,
    val outcome: RoundOutcome,
)

/**
 * Single-select filter applied to the game column.
 * [All] keeps every game; the rest narrow the list to one game.
 */
enum class GameFilter(
    val label: String,
    val match: GameKey?,
) {
    All("All", null),
    Roulette("Roulette", GameKey.Roulette),
    Blackjack("Blackjack", GameKey.Blackjack),
    Crash("Crash", GameKey.Crash),
    Mines("Mines", GameKey.Mines),
    Coinflip("Coinflip", GameKey.Coinflip),
}

/**
 * Single-select filter applied to the outcome column.
 * Null [match] means "do not filter on outcome".
 */
enum class ResultFilter(
    val label: String,
    val match: RoundOutcome?,
) {
    All("All", null),
    Wins("Wins", RoundOutcome.Win),
    Losses("Losses", RoundOutcome.Loss),
}
