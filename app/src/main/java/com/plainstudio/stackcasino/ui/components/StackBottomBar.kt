package com.plainstudio.stackcasino.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.navigation.PrimaryTab
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Bottom navigation bar mirroring the mockup spec
 * (mockup/js/components.js, `bottomNav`):
 *
 *   nav: h-16, border-t border-line, bg-[#0B0B12], grid 5 cols
 *   button: stacked icon + 9px tracked label, violet when active,
 *           muted otherwise
 *   icon: 18dp, stroked (no fill), stroke-width 2
 *
 * Drawn as a custom [Row] instead of `androidx.compose.material3.NavigationBar`
 * because the Material 3 default surface, indicator pill and icon
 * tinting would all need overrides; a plain row matches the mockup
 * exactly with less ceremony.
 *
 * Tab visibility is owned by the caller: render this only when the
 * current destination is one of [PrimaryTab.route].
 */
@Composable
fun StackBottomBar(
    currentRoute: String?,
    onTabSelected: (PrimaryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BarHeight),
        color = SurfaceBase,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val strokePx = TopBorderWidth.toPx()
                        // drawLine centers the stroke on the given coordinate;
                        // offsetting by half its width keeps the entire border
                        // visible inside the row instead of being clipped at
                        // the top edge.
                        val centerY = strokePx / 2f
                        drawLine(
                            color = SurfaceOutline,
                            start = Offset(0f, centerY),
                            end = Offset(size.width, centerY),
                            strokeWidth = strokePx,
                        )
                    },
        ) {
            PrimaryTab.entries.forEach { tab ->
                BottomNavTab(
                    tab = tab,
                    isActive = currentRoute == tab.route.path,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    tab: PrimaryTab,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (isActive) AccentViolet else TextMedium
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            // The label below already announces the tab to TalkBack; a
            // contentDescription on the icon would cause it to read twice.
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(IconSize),
        )
        Spacer(modifier = Modifier.height(IconLabelGap))
        Text(
            text = tab.label.uppercase(),
            color = tint,
            fontSize = LabelFontSize,
            letterSpacing = LabelLetterSpacing,
        )
    }
}

@get:DrawableRes
private val PrimaryTab.iconRes: Int
    get() =
        when (this) {
            PrimaryTab.Lobby -> R.drawable.ic_tab_lobby
            PrimaryTab.Wallet -> R.drawable.ic_tab_wallet
            PrimaryTab.History -> R.drawable.ic_tab_history
            PrimaryTab.News -> R.drawable.ic_tab_news
            PrimaryTab.Profile -> R.drawable.ic_tab_profile
        }

private val BarHeight = 64.dp
private val TopBorderWidth = 1.dp
private val IconSize = 18.dp
private val IconLabelGap = 4.dp
private val LabelFontSize = 9.sp
private val LabelLetterSpacing = 1.2.sp
