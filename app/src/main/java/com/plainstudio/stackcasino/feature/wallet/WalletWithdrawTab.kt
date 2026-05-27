package com.plainstudio.stackcasino.feature.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticWarn
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Withdraw tab. Renders the destination + amount form, the inline
 * available-balance hint, the network-fee summary, the KYC gate and
 * the (disabled) Withdraw button.
 *
 * Validation, signing and submission are owned by the wallet
 * repository in a later card; this composable only models visual
 * state. The Max button still snaps the amount to [availableLabel]
 * so the form behaviour is testable.
 */
@Composable
internal fun WalletWithdrawTab(
    availableLabel: String,
    currencyCode: String,
    onVerifyIdentity: () -> Unit,
) {
    var address by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        WithdrawForm(
            address = address,
            onAddressChange = { address = it },
            amount = amount,
            onAmountChange = { amount = it },
            availableLabel = availableLabel,
            currencyCode = currencyCode,
            onMaxClick = { amount = availableLabel.removePrefix("$") },
        )
        KycGate(onVerifyIdentity = onVerifyIdentity)
        WithdrawButton(enabled = false, onClick = {})
    }
}

@Composable
private fun WithdrawForm(
    address: String,
    onAddressChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    availableLabel: String,
    currencyCode: String,
    onMaxClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(FormPadding),
        verticalArrangement = Arrangement.spacedBy(FieldGap),
    ) {
        AddressField(value = address, onValueChange = onAddressChange)
        AmountField(
            amount = amount,
            onAmountChange = onAmountChange,
            availableLabel = availableLabel,
            currencyCode = currencyCode,
            onMaxClick = onMaxClick,
        )
        FeeSummary()
    }
}

@Composable
private fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        FieldLabel(text = "Destination Address")
        Spacer(modifier = Modifier.height(8.dp))
        WithdrawTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "0x…",
            fontFamily = FontFamily.Monospace,
            keyboardType = KeyboardType.Ascii,
        )
    }
}

@Composable
private fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    availableLabel: String,
    currencyCode: String,
    onMaxClick: () -> Unit,
) {
    Column {
        FieldLabel(text = "Amount ($currencyCode)")
        Spacer(modifier = Modifier.height(8.dp))
        AmountChipsRow(onPresetClick = onAmountChange, onMaxClick = onMaxClick)
        Spacer(modifier = Modifier.height(8.dp))
        WithdrawTextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = "0.00",
            fontFamily = FontFamily.Default,
            keyboardType = KeyboardType.Decimal,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "AVAILABLE: $availableLabel $currencyCode",
            color = TextLow,
            fontSize = SmallLabelFontSize,
            letterSpacing = TrackedLetterSpacing,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun AmountChipsRow(
    onPresetClick: (String) -> Unit,
    onMaxClick: () -> Unit,
) {
    // All five chips share the row equally (mockup `flex-1`); the violet
    // border on MAX is what differentiates it visually.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AmountChipGap),
    ) {
        AMOUNT_PRESETS.forEach { preset ->
            AmountChip(
                label = "$$preset",
                onClick = { onPresetClick(preset) },
                modifier = Modifier.weight(1f),
            )
        }
        MaxAmountChip(onClick = onMaxClick, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AmountChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(SurfaceBase)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick)
                .padding(vertical = AmountChipVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = TextMedium,
            fontSize = AmountChipFontSize,
            lineHeight = AmountChipFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun MaxAmountChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(SurfaceBase)
                .border(width = 1.dp, color = AccentViolet)
                .clickable(onClick = onClick)
                .padding(vertical = AmountChipVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        // Mirror AmountChip exactly so MAX has the same intrinsic
        // height; the only visible difference must come from the
        // violet border + text colour. Without an explicit lineHeight,
        // this Text falls back to the theme's body lineHeight and the
        // chip ends up visibly taller than its dollar-amount siblings.
        Text(
            text = "MAX",
            color = AccentViolet,
            fontSize = AmountChipFontSize,
            lineHeight = AmountChipFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun WithdrawTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontFamily: FontFamily,
    keyboardType: KeyboardType,
) {
    val textStyle =
        TextStyle(
            color = TextHigh,
            fontSize = FieldFontSize,
            fontFamily = fontFamily,
            fontFeatureSettings = if (keyboardType == KeyboardType.Decimal) "tnum" else "",
        )
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceBase)
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(AccentViolet),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
        )
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextLow,
                fontSize = FieldFontSize,
                fontFamily = fontFamily,
            )
        }
    }
}

@Composable
private fun FeeSummary() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceOutline),
        )
        Spacer(modifier = Modifier.height(2.dp))
        FeeRow(label = "Network Fee", amount = "$0.50")
        FeeReceiveRow()
    }
}

@Composable
private fun FeeRow(
    label: String,
    amount: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = TextMedium,
            fontSize = FeeFontSize,
        )
        Text(
            text = amount,
            color = TextHigh,
            fontSize = FeeFontSize,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun FeeReceiveRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "You'll Receive",
            color = TextHigh,
            fontSize = FeeFontSize,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "$0.00",
            color = AccentViolet,
            fontSize = FeeFontSize,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun KycGate(onVerifyIdentity: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SemanticWarn.copy(alpha = KYC_BG_ALPHA))
                .border(width = 1.dp, color = SemanticWarn.copy(alpha = KYC_BORDER_ALPHA))
                .padding(KycPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = SemanticWarn,
            modifier = Modifier.size(KycIconSize),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "KYC required for withdrawals over $100",
                color = SemanticWarn,
                fontSize = KycTitleFontSize,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Verify your identity to unlock larger withdrawals.",
                color = TextMedium,
                fontSize = KycBodyFontSize,
                lineHeight = KycBodyLineHeight,
            )
            Row(
                modifier = Modifier.clickable(onClick = onVerifyIdentity),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "VERIFY IDENTITY",
                    color = AccentViolet,
                    fontSize = KycCtaFontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = TrackedLetterSpacing,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = AccentViolet,
                    modifier = Modifier.size(KycCtaChevronSize),
                )
            }
        }
    }
}

@Composable
private fun WithdrawButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val container = if (enabled) AccentViolet else AccentViolet.copy(alpha = DISABLED_BUTTON_ALPHA)
    val content = if (enabled) Color.White else Color.White.copy(alpha = DISABLED_BUTTON_ALPHA)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(container)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = ButtonVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "WITHDRAW",
            color = content,
            fontSize = ButtonFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextMedium,
        fontSize = SmallLabelFontSize,
        letterSpacing = TrackedLetterSpacing,
    )
}

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

private val FormPadding = PaddingValues(16.dp)
private val FieldGap = 16.dp

private val FieldFontSize = 14.sp
private val SmallLabelFontSize = 9.sp

private val AmountChipGap = 6.dp
private val AmountChipVerticalPadding = 8.dp
private val AmountChipFontSize = 10.sp

private val FeeFontSize = 13.sp

private val KycPadding = PaddingValues(16.dp)
private val KycIconSize = 18.dp
private val KycTitleFontSize = 14.sp
private val KycBodyFontSize = 12.sp
private val KycBodyLineHeight = 18.sp
private val KycCtaFontSize = 11.sp
private val KycCtaChevronSize = 12.dp
private const val KYC_BG_ALPHA = 0.05f
private const val KYC_BORDER_ALPHA = 0.50f

private val ButtonVerticalPadding = 16.dp
private val ButtonFontSize = 12.sp
private const val DISABLED_BUTTON_ALPHA = 0.40f

private val AMOUNT_PRESETS = listOf("10", "25", "50", "100")
