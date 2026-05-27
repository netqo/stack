package com.plainstudio.stackcasino.feature.lobby

/**
 * Static seed used by both the lobby @Preview composables and the live
 * navigation entry until the Firestore-backed ViewModel ships.
 *
 * Numbers and labels mirror the mockup mock-data
 * (mockup/js/screens/lobby.js) so the rendered screen matches the
 * design source one-for-one.
 */
internal fun previewLobbyData(): LobbyData =
    LobbyData(
        user = previewUser(),
        balance = previewBalance(),
        session = previewSession(),
        games = previewGames(),
        recentActivity = previewRecentActivity(),
    )

private fun previewUser(): UserSummary =
    UserSummary(
        displayName = "John Doe",
        greeting = "Good Evening",
        hasUnreadNotifications = true,
    )

private fun previewBalance(): BalanceSummary =
    BalanceSummary(
        amountLabel = "$1,234.56",
        currencyCode = "USDC",
        networkLabel = "Polygon",
        todayPnLLabel = "+$45.00 Today",
        lockedSubtitle = "No active bets",
    )

private fun previewSession(): SessionStats = SessionStats(rounds = 8, wins = 5, losses = 3)

private fun previewGames(): List<GameCardData> =
    listOf(
        GameCardData(
            key = GameKey.Roulette,
            title = "European Roulette",
            subtitle = "European",
            infoRight = "RTP 97.3%",
        ),
        GameCardData(
            key = GameKey.Blackjack,
            title = "Blackjack",
            subtitle = "Classic",
            infoRight = "RTP 99.5%",
        ),
        GameCardData(
            key = GameKey.Crash,
            title = "Crash",
            subtitle = "Multiplier",
            infoRight = "Last 2.41x",
            isLastPlayed = true,
        ),
        GameCardData(
            key = GameKey.Mines,
            title = "Mines",
            subtitle = "5x5 Grid",
            infoRight = "Up to 24x",
        ),
        GameCardData(
            key = GameKey.Coinflip,
            title = "Coinflip",
            subtitle = "x2 Payout",
            infoRight = "Heads · Tails",
        ),
    )

private fun previewRecentActivity(): List<RecentRound> =
    listOf(
        RecentRound(
            game = GameKey.Crash,
            gameLabel = "Crash",
            agoLabel = "2m ago",
            resultLabel = "2.51x",
            amountLabel = "+$125.50",
            outcome = RoundOutcome.Win,
        ),
        RecentRound(
            game = GameKey.Roulette,
            gameLabel = "Roulette",
            agoLabel = "18m ago",
            resultLabel = "7x",
            amountLabel = "+$175.00",
            outcome = RoundOutcome.Win,
        ),
        RecentRound(
            game = GameKey.Blackjack,
            gameLabel = "Blackjack",
            agoLabel = "1h ago",
            resultLabel = "Bust",
            amountLabel = "-$100.00",
            outcome = RoundOutcome.Loss,
        ),
    )
