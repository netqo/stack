package com.plainstudio.stackcasino.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.plainstudio.stackcasino.navigation.PrimaryTab

/**
 * Material 3 bottom navigation bar bound to the five top-level
 * destinations defined in [PrimaryTab]. Tab visibility is owned by the
 * caller: render this only when the current destination is one of
 * [PrimaryTab.route].
 */
@Composable
fun StackBottomBar(
    currentRoute: String?,
    onTabSelected: (PrimaryTab) -> Unit,
) {
    NavigationBar {
        PrimaryTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route.path,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

private val PrimaryTab.icon: ImageVector
    get() =
        when (this) {
            PrimaryTab.Lobby -> Icons.Outlined.Home
            PrimaryTab.Wallet -> Icons.Outlined.AccountBalanceWallet
            PrimaryTab.History -> Icons.Outlined.History
            PrimaryTab.News -> Icons.Outlined.Newspaper
            PrimaryTab.Profile -> Icons.Outlined.Person
        }
