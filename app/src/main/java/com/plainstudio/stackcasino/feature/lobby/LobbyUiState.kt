package com.plainstudio.stackcasino.feature.lobby

import com.plainstudio.stackcasino.model.GameKey
import com.plainstudio.stackcasino.model.RoundOutcome

/**
 * UI state for the lobby screen.
 *
 * Mirrors the three states drawn by the mockup
 * (mockup/js/screens/lobby.js, `data-lobby-show="success|loading|error"`):
 *
 *   * [Success] renders the full screen seeded with [LobbyData].
 *   * [Loading] renders the skeleton placeholders only (no header
 *     content is computed yet because the user data has not loaded).
 *   * [Error] renders the centered "Connection Lost" card with the
 *     retry / use-cache CTAs and the last-sync footer.
 *
 * No ViewModel ships with this PR. The screen is driven by static
 * preview data so the UI iteration can land independently of the data
 * layer; the real Firestore-backed VM lands when wallet + history
 * repositories are in place.
 */
sealed interface LobbyUiState {
    data class Success(
        val data: LobbyData,
    ) : LobbyUiState

    data object Loading : LobbyUiState

    data class Error(
        val message: String,
        val lastSyncedLabel: String,
    ) : LobbyUiState
}

/**
 * Aggregate of everything the success state needs. Kept as a single
 * snapshot so the screen does not have to reason about partial loads.
 */
data class LobbyData(
    val user: UserSummary,
    val balance: BalanceSummary,
    val session: SessionStats,
    val games: List<GameCardData>,
    val recentActivity: List<RecentRound>,
)

data class UserSummary(
    val displayName: String,
    val greeting: String,
    val hasUnreadNotifications: Boolean,
)

/**
 * Header balance block. [todayPnLLabel] is the formatted "+$45.00 Today"
 * chip; it is null when the user has no rounds today (and the chip is
 * hidden entirely). [lockedSubtitle] mirrors the right-side stack
 * ("No active bets" when there are none, otherwise a formatted amount).
 */
data class BalanceSummary(
    val amountLabel: String,
    val currencyCode: String,
    val networkLabel: String,
    val todayPnLLabel: String?,
    val lockedSubtitle: String,
)

data class SessionStats(
    val rounds: Int,
    val wins: Int,
    val losses: Int,
)

/**
 * Single entry in the games grid.
 *
 * [infoRight] is the right-aligned secondary label ("RTP 97.3%",
 * "Last 2.41x", "Up to 24x"); it is callback data, the formatting
 * already happened at the data layer.
 */
data class GameCardData(
    val key: GameKey,
    val title: String,
    val subtitle: String,
    val infoRight: String,
    val isLastPlayed: Boolean = false,
)

/**
 * Recent activity row.
 *
 * [amountLabel] is pre-signed ("+$125.50", "-$100.00") so the UI does
 * not have to combine outcome + amount. [outcome] only drives the left
 * accent stripe and the amount color.
 */
data class RecentRound(
    val game: GameKey,
    val gameLabel: String,
    val agoLabel: String,
    val resultLabel: String,
    val amountLabel: String,
    val outcome: RoundOutcome,
)
