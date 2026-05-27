package com.plainstudio.stackcasino.feature.wallet

/**
 * Aggregate snapshot the wallet screen renders. The screen has no
 * Firestore-backed ViewModel yet, so a static [WalletPreviewData]
 * seeds both the @Preview composables and the live nav-host entry.
 */
data class WalletData(
    val availableLabel: String,
    val lockedLabel: String,
    val currencyCode: String,
    val networkLabel: String,
    val depositAddress: String,
    val transactions: List<WalletTransaction>,
)

/**
 * Tab segments rendered by the wallet header strip. Order in the enum
 * matches the visual order in the mockup (deposit first / default).
 */
enum class WalletTab(
    val label: String,
) {
    Deposit("Deposit"),
    Withdraw("Withdraw"),
    Transactions("Transactions"),
}

/**
 * Transaction list filter scope. `All` is the default; the other two
 * mirror the `data-filter` chips in the mockup.
 */
enum class TransactionFilter(
    val label: String,
) {
    All("All"),
    Deposits("Deposits"),
    Withdrawals("Withdrawals"),
}

/**
 * A single ledger entry. [amountLabel] is pre-signed ("+$500.00") and
 * the colour of the label is decided by [type] + [status] (a confirmed
 * withdraw and a failed withdraw differ visually).
 */
data class WalletTransaction(
    val type: TransactionType,
    val status: TransactionStatus,
    val timestampLabel: String,
    val shortHash: String,
    val amountLabel: String,
    val currencyCode: String,
)

enum class TransactionType { Deposit, Withdraw }

/**
 * Tri-state lifecycle for a transaction. [Confirmed] paints the row
 * with the matching outcome accent (ok / txt-mid), [Pending] keeps the
 * warn-amber stripe from the mockup, [Failed] uses danger-red.
 */
enum class TransactionStatus { Confirmed, Pending, Failed }
