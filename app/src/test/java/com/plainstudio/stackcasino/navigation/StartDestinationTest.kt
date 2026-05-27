package com.plainstudio.stackcasino.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks the mapping from [StartDestination] to the [Route] paths the
 * NavHost knows about. If either entry drifts (e.g. a rename of the
 * lobby route), the splash flow would emit an unreachable destination
 * and the activity would crash on first frame; this test fails first.
 */
class StartDestinationTest {
    @Test
    fun login_maps_to_login_route_path() {
        assertEquals(Route.Login.path, StartDestination.Login.route)
    }

    @Test
    fun lobby_maps_to_lobby_route_path() {
        assertEquals(Route.Lobby.path, StartDestination.Lobby.route)
    }
}
