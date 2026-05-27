package com.plainstudio.stackcasino.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.navigation.Route
import com.plainstudio.stackcasino.ui.components.CurrencyDropdown
import com.plainstudio.stackcasino.ui.components.gridBackground
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Wallet screen reproducing the cu-04 mockup
 * (mockup/js/screens/wallet.js). Owns the screen-level scaffold:
 *
 *   * Title header.
 *   * Balance hero (mirrors Lobby's, minus the eye toggle).
 *   * Tab strip (Deposit / Withdraw / Transactions).
 *   * Tab pane, dispatched on the currently selected [WalletTab].
 *
 * Pane content lives in dedicated files
 * (WalletDepositTab / WalletWithdrawTab / WalletTransactionsTab) so
 * each section stays focused and the detekt function-count budget
 * is respected.
 */
@Composable
fun WalletScreen(
    data: WalletData,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(WalletTab.Deposit) }
    Surface(modifier = modifier.fillMaxSize(), color = SurfaceBase) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .gridBackground()
                    .verticalScroll(rememberScrollState()),
        ) {
            WalletHeader()
            HorizontalDivider()
            BalanceBlock(data = data)
            HorizontalDivider()
            WalletTabStrip(selected = selectedTab, onSelect = { selectedTab = it })
            when (selectedTab) {
                WalletTab.Deposit -> WalletDepositTab(address = data.depositAddress)
                WalletTab.Withdraw ->
                    WalletWithdrawTab(
                        availableLabel = data.availableLabel,
                        currencyCode = data.currencyCode,
                        onVerifyIdentity = { onNavigate(Route.Kyc) },
                    )
                WalletTab.Transactions ->
                    WalletTransactionsTab(
                        transactions = data.transactions,
                        onGoToDeposit = { selectedTab = WalletTab.Deposit },
                    )
            }
            Spacer(modifier = Modifier.height(BottomScrollPadding))
        }
    }
}

@Composable
private fun WalletHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenHorizontalPadding,
                    end = ScreenHorizontalPadding,
                    top = HeaderTopPadding,
                    bottom = HeaderBottomPadding,
                ),
    ) {
        Text(
            text = "Wallet",
            color = TextHigh,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BalanceBlock(data: WalletData) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "AVAILABLE",
                color = TextMedium,
                fontSize = MetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.availableLabel,
                color = TextHigh,
                fontSize = AmountFontSize,
                fontWeight = FontWeight.Bold,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            CurrencyDropdown(
                initialCurrency = data.currencyCode,
                networkLabel = data.networkLabel,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "LOCKED",
                color = TextMedium,
                fontSize = MetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.lockedLabel,
                color = TextMedium,
                fontSize = LockedAmountFontSize,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
        }
    }
}

@Composable
private fun WalletTabStrip(
    selected: WalletTab,
    onSelect: (WalletTab) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(TabGap),
    ) {
        WalletTab.entries.forEach { tab ->
            WalletTabButton(
                label = tab.label,
                isSelected = tab == selected,
                onClick = { onSelect(tab) },
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun WalletTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (isSelected) AccentViolet else TextMedium
    // drawBehind paints the underline directly under the text without
    // wrapping the Text in a Column. Wrapping with a fillMaxWidth Box
    // forces the parent Row to give the whole row to the first tab,
    // collapsing the other two to a single character of width.
    Text(
        text = label.uppercase(),
        color = textColor,
        fontSize = TabFontSize,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = TrackedLetterSpacing,
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .padding(vertical = TabVerticalPadding)
                .drawBehind {
                    if (!isSelected) return@drawBehind
                    val strokePx = TabIndicatorHeight.toPx()
                    drawRect(
                        color = AccentViolet,
                        topLeft = Offset(0f, size.height - strokePx),
                        size = Size(size.width, strokePx),
                    )
                },
    )
}

@Composable
internal fun HorizontalDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceOutline),
    )
}

// ---------------------------------------------------------------------------
// Tokens shared with the tab files via the `internal` modifier.
// ---------------------------------------------------------------------------

internal val ScreenHorizontalPadding = 16.dp
internal val SectionVerticalPadding = 20.dp
internal val SectionGap = 16.dp

private val HeaderTopPadding = 24.dp
private val HeaderBottomPadding = 16.dp
private val BottomScrollPadding = 96.dp

private val TabGap = 24.dp
private val TabVerticalPadding = 12.dp
private val TabFontSize = 11.sp
private val TabIndicatorHeight = 2.dp

private val AmountFontSize = 36.sp
private val LockedAmountFontSize = 18.sp
private val MetaFontSize = 10.sp
internal val TrackedLetterSpacing = 1.2.sp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 1200)
@Composable
private fun WalletScreenDepositPreview() {
    StackcasinoTheme {
        WalletScreen(data = previewWalletData(), onNavigate = {})
    }
}
