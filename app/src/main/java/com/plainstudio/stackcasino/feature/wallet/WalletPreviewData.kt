package com.plainstudio.stackcasino.feature.wallet

/**
 * Static seed used by the wallet @Preview composables and the live
 * navigation entry until a Firestore-backed VM ships. Numbers and
 * labels mirror mockup/js/screens/wallet.js so the rendered screen
 * matches the design source one-for-one.
 */
internal fun previewWalletData(): WalletData =
    WalletData(
        availableLabel = "$1,234.56",
        lockedLabel = "$0.00",
        currencyCode = "USDC",
        networkLabel = "Polygon",
        depositAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
        transactions = previewTransactions(),
    )

private fun previewTransactions(): List<WalletTransaction> =
    listOf(
        WalletTransaction(
            type = TransactionType.Deposit,
            status = TransactionStatus.Confirmed,
            timestampLabel = "4/16/2026 · 7:30 AM",
            shortHash = "0x3f9a…c7e2",
            amountLabel = "+$500.00",
            currencyCode = "USDC",
        ),
        WalletTransaction(
            type = TransactionType.Withdraw,
            status = TransactionStatus.Pending,
            timestampLabel = "4/15/2026 · 11:20 AM",
            shortHash = "0x8b2d…a1f0",
            amountLabel = "-$120.00",
            currencyCode = "USDC",
        ),
        WalletTransaction(
            type = TransactionType.Deposit,
            status = TransactionStatus.Confirmed,
            timestampLabel = "4/14/2026 · 6:15 AM",
            shortHash = "0xd41c…5e89",
            amountLabel = "+$200.00",
            currencyCode = "USDC",
        ),
        WalletTransaction(
            type = TransactionType.Withdraw,
            status = TransactionStatus.Failed,
            timestampLabel = "4/13/2026 · 1:45 PM",
            shortHash = "0x17ef…b304",
            amountLabel = "-$50.00",
            currencyCode = "USDC",
        ),
    )
