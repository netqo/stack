package com.plainstudio.stackcasino.feature.history

import com.plainstudio.stackcasino.model.GameKey
import com.plainstudio.stackcasino.model.RoundOutcome

/**
 * Static seed for the history screen previews + live nav-host entry.
 * Numbers and labels mirror mockup/js/screens/history.js so the
 * rendered screen matches the design source one-for-one.
 */
internal fun historyPreviewData(): HistoryData =
    HistoryData(
        summary = previewSummary(),
        rounds = previewRounds(),
    )

private fun previewSummary(): HistorySummary =
    HistorySummary(
        totalRounds = 128,
        winRatePercent = 54,
        netLabel = "+$412.30",
    )

private fun previewRounds(): List<HistoryRound> =
    listOf(
        HistoryRound(
            id = "round-001",
            game = GameKey.Crash,
            timestampLabel = "4/16/2026 · 7:30 AM",
            betLabel = "$50.00",
            payoutLabel = "$125.50",
            multiplierLabel = "2.51x",
            outcome = RoundOutcome.Win,
        ),
        HistoryRound(
            id = "round-002",
            game = GameKey.Roulette,
            timestampLabel = "4/16/2026 · 6:15 AM",
            betLabel = "$25.00",
            payoutLabel = "$175.00",
            multiplierLabel = "7x",
            outcome = RoundOutcome.Win,
        ),
        HistoryRound(
            id = "round-003",
            game = GameKey.Blackjack,
            timestampLabel = "4/15/2026 · 11:20 AM",
            betLabel = "$100.00",
            payoutLabel = "$0.00",
            multiplierLabel = "0x",
            outcome = RoundOutcome.Loss,
        ),
        HistoryRound(
            id = "round-004",
            game = GameKey.Mines,
            timestampLabel = "4/15/2026 · 8:45 AM",
            betLabel = "$20.00",
            payoutLabel = "$48.00",
            multiplierLabel = "2.40x",
            outcome = RoundOutcome.Win,
        ),
        HistoryRound(
            id = "round-005",
            game = GameKey.Coinflip,
            timestampLabel = "4/14/2026 · 1:30 PM",
            betLabel = "$75.00",
            payoutLabel = "$0.00",
            multiplierLabel = "Heads",
            outcome = RoundOutcome.Loss,
        ),
    )
