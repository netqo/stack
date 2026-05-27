package com.plainstudio.stackcasino.feature.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.components.EmptyState
import com.plainstudio.stackcasino.ui.components.FilterChip
import com.plainstudio.stackcasino.ui.components.FilterChipRow
import com.plainstudio.stackcasino.ui.components.StackCard
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.SemanticWarn
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Transactions tab. Renders a FilterChipRow plus the filtered ledger.
 * When the active filter matches no rows, an EmptyState card with a
 * "Deposit Now" CTA pushes the user back to the Deposit tab.
 */
@Composable
internal fun WalletTransactionsTab(
    transactions: List<WalletTransaction>,
    onGoToDeposit: () -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf(TransactionFilter.All) }
    val visible = transactions.filter { it.matches(filter) }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
    ) {
        FilterChipRow(
            chips = filterChips(),
            selected = filter,
            onSelect = { filter = it },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (visible.isEmpty()) {
            EmptyTransactionsState(onGoToDeposit = onGoToDeposit)
        } else {
            TransactionList(visible)
        }
    }
}

@Composable
private fun TransactionList(transactions: List<WalletTransaction>) {
    Column(verticalArrangement = Arrangement.spacedBy(RowGap)) {
        transactions.forEach { tx -> TransactionRow(tx) }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    StackCard(
        modifier = Modifier.fillMaxWidth(),
        leftAccent = tx.accentColor(),
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            TransactionPrimaryColumn(tx)
            TransactionAmountColumn(tx)
        }
    }
}

@Composable
private fun TransactionPrimaryColumn(tx: WalletTransaction) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = tx.icon(),
                contentDescription = null,
                tint = tx.iconTint(),
                modifier = Modifier.size(RowIconSize),
            )
            Text(
                text = tx.type.label(),
                color = TextHigh,
                fontSize = RowTitleFontSize,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = tx.timestampLabel,
            color = TextLow,
            fontSize = RowMetaFontSize,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
        HashRow(shortHash = tx.shortHash)
    }
}

@Composable
private fun HashRow(shortHash: String) {
    Row(
        modifier = Modifier.clickable { /* explorer deep-link ships with wallet repo */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = shortHash,
            color = TextLow,
            fontSize = HashFontSize,
            fontFamily = FontFamily.Monospace,
        )
        Icon(
            imageVector = Icons.Outlined.OpenInNew,
            contentDescription = "Open in block explorer",
            tint = TextLow,
            modifier = Modifier.size(HashIconSize),
        )
    }
}

@Composable
private fun TransactionAmountColumn(tx: WalletTransaction) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = tx.amountLabel,
            color = tx.amountColor(),
            fontSize = AmountFontSize,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
        Text(
            text = tx.currencyCode,
            color = TextLow,
            fontSize = CurrencyFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

@Composable
private fun EmptyTransactionsState(onGoToDeposit: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        EmptyState(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = TextLow,
                    modifier = Modifier.size(EmptyIconSize),
                )
            },
            title = "No transactions yet",
            message = "Deposit crypto to get started.",
            actionLabel = "Deposit Now",
            onAction = onGoToDeposit,
        )
    }
}

private fun WalletTransaction.matches(filter: TransactionFilter): Boolean =
    when (filter) {
        TransactionFilter.All -> true
        TransactionFilter.Deposits -> type == TransactionType.Deposit
        TransactionFilter.Withdrawals -> type == TransactionType.Withdraw
    }

private fun WalletTransaction.accentColor(): Color =
    when (status) {
        TransactionStatus.Confirmed ->
            if (type == TransactionType.Deposit) SemanticOk else TextMedium
        TransactionStatus.Pending -> SemanticWarn
        TransactionStatus.Failed -> SemanticDanger
    }

private fun WalletTransaction.amountColor(): Color =
    when {
        type == TransactionType.Deposit && status == TransactionStatus.Confirmed -> SemanticOk
        status == TransactionStatus.Failed -> SemanticDanger
        else -> TextHigh
    }

private fun WalletTransaction.icon() =
    when (type) {
        TransactionType.Deposit -> Icons.Outlined.ArrowDownward
        TransactionType.Withdraw -> Icons.Outlined.ArrowUpward
    }

private fun WalletTransaction.iconTint(): Color =
    when (status) {
        TransactionStatus.Confirmed ->
            if (type == TransactionType.Deposit) SemanticOk else TextMedium
        TransactionStatus.Pending -> SemanticWarn
        TransactionStatus.Failed -> SemanticDanger
    }

private fun TransactionType.label(): String =
    when (this) {
        TransactionType.Deposit -> "Deposit"
        TransactionType.Withdraw -> "Withdraw"
    }

private fun filterChips(): List<FilterChip<TransactionFilter>> =
    TransactionFilter.entries.map { FilterChip(key = it, label = it.label) }

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

private val RowGap = 8.dp
private val RowIconSize = 14.dp
private val RowTitleFontSize = 15.sp
private val RowMetaFontSize = 11.sp
private val HashFontSize = 10.sp
private val HashIconSize = 10.dp
private val AmountFontSize = 15.sp
private val CurrencyFontSize = 9.sp
private val EmptyIconSize = 24.dp
