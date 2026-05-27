package com.plainstudio.stackcasino.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM-side smoke for the navigation surface. The full reachability test
 * lives in androidTest (it needs the Compose runtime + NavController),
 * but path patterns and tab membership can be locked here so a typo
 * fails fast on every PR even before instrumented runs are wired in.
 */
class RouteTest {
    @Test
    fun roundDetail_build_inserts_id_into_path_pattern() {
        assertEquals("round/round-42", Route.RoundDetail.build("round-42"))
        assertTrue(
            "Pattern must contain the argument placeholder.",
            Route.RoundDetail.path.contains("{${Route.RoundDetail.ARG_ROUND_ID}}"),
        )
    }

    @Test
    fun newsDetail_build_inserts_id_into_path_pattern() {
        assertEquals("news/article-7", Route.NewsDetail.build("article-7"))
        assertTrue(
            "Pattern must contain the argument placeholder.",
            Route.NewsDetail.path.contains("{${Route.NewsDetail.ARG_ARTICLE_ID}}"),
        )
    }

    @Test
    fun primaryTab_exposes_five_bottom_bar_destinations() {
        val routes = PrimaryTab.entries.map { it.route }
        assertEquals(
            listOf(Route.Lobby, Route.Wallet, Route.History, Route.News, Route.Profile),
            routes,
        )
    }

    @Test
    fun primaryTab_routePaths_matches_entries_in_order() {
        assertEquals(
            setOf(
                Route.Lobby.path,
                Route.Wallet.path,
                Route.History.path,
                Route.News.path,
                Route.Profile.path,
            ),
            PrimaryTab.routePaths,
        )
    }
}
