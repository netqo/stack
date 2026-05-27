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
    // Used by call sites that just want to "go to this destination"
    // without supplying an argument. Defaults to [path] so static
    // routes stay one-liners; parametric routes whose pattern carries
    // optional query args (e.g. wallet?tab={tab}) override it to the
    // bare path that produces the default landing state.
    val defaultPath: String = path,
) {
    data object Login : Route("login")

    data object Lobby : Route("lobby")

    /**
     * Wallet has three top-level tabs (Deposit / Withdraw / Transactions)
     * and the lobby's quick actions deep-link to one of the first two.
     * The optional `tab` query arg surfaces that choice without spinning
     * up a second route; navigating to `"wallet"` lands on the default
     * Deposit tab.
     */
    data object Wallet : Route(
        path = "wallet?tab={tab}",
        defaultPath = "wallet",
    ) {
        const val ARG_TAB = "tab"

        fun build(tab: String? = null): String = if (tab.isNullOrBlank()) defaultPath else "wallet?tab=$tab"
    }

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

        // NewsAPI returns the article URL as the only stable id, so the
        // path arg has to survive the colons + slashes of a real URL.
        // URLEncoder is used (vs android.net.Uri.encode) so the helper
        // also works under plain JVM unit tests where the Android stub
        // would return null. The +-to-%20 swap brings the output to
        // RFC 3986 path encoding because URLEncoder encodes spaces as
        // `+` (form-encoding) by default.
        fun build(articleId: String): String {
            val encoded =
                java.net.URLEncoder
                    .encode(articleId, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20")
            return "news/$encoded"
        }
    }
}

/**
 * Bottom-navigation tabs. The bar is rendered only when the current
 * destination matches one of these; deep screens (Login, RoundDetail,
 * NewsDetail, KYC, HouseWallet, Assistant and the five games) take
 * the full viewport.
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
    ;

    companion object {
        /**
         * Pre-computed set of route paths owned by a tab, used by the
         * top-level Scaffold to decide whether the bottom bar should
         * be visible for the current destination.
         */
        val routePaths: Set<String> = entries.map { it.route.path }.toSet()
    }
}
