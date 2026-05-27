package com.plainstudio.stackcasino.navigation

/**
 * Single source of truth for every navigable destination in the app.
 *
 * Static routes expose their [path] as the literal nav graph identifier.
 * Parametric routes (e.g. [RoundDetail], [NewsDetail]) expose both the
 * pattern with the placeholder ({argName}) used by the graph builder and
 * a helper that materializes a concrete navigation target.
 *
 * Adding a destination here requires three follow-ups:
 *   1) Register it inside StackNavHost.
 *   2) Decide whether it belongs to [PrimaryTab] (bottom-bar visible).
 *   3) Cover it in StackNavHostTest.
 */
sealed class Route(
    val path: String,
) {
    data object Login : Route("login")

    data object Lobby : Route("lobby")

    data object Wallet : Route("wallet")

    data object HouseWallet : Route("house_wallet")

    data object History : Route("history")

    data object News : Route("news")

    data object Profile : Route("profile")

    data object Kyc : Route("kyc")

    data object Assistant : Route("assistant")

    data object Coinflip : Route("game/coinflip")

    data object Roulette : Route("game/roulette")

    data object Crash : Route("game/crash")

    data object Mines : Route("game/mines")

    data object Blackjack : Route("game/blackjack")

    data object RoundDetail : Route("round/{roundId}") {
        const val ARG_ROUND_ID = "roundId"

        fun build(roundId: String): String = "round/$roundId"
    }

    data object NewsDetail : Route("news/{articleId}") {
        const val ARG_ARTICLE_ID = "articleId"

        fun build(articleId: String): String = "news/$articleId"
    }
}

/**
 * Bottom-navigation tabs. The bar is rendered only when the current
 * destination matches one of these; deep screens (Splash, Login,
 * RoundDetail, NewsDetail, KYC, HouseWallet, Assistant and the five
 * games) take the full viewport.
 */
enum class PrimaryTab(
    val route: Route,
    val label: String,
) {
    Lobby(Route.Lobby, "Lobby"),
    Wallet(Route.Wallet, "Wallet"),
    History(Route.History, "History"),
    News(Route.News, "News"),
    Profile(Route.Profile, "Profile"),
}
