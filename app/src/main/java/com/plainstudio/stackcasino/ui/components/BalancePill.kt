package com.plainstudio.stackcasino.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Balance hero block used by the Lobby header and the Wallet header.
 *
 * Mockup spec (mockup/js/screens/lobby.js, line 40+ and
 * mockup/js/screens/wallet.js, line 13+):
 *
 *   label:  tracked 10sp, txt-mid color
 *   amount: 36sp bold, tabular-nums for stable layout
 *   eye:    18dp outlined icon, txt-lo (hover violet)
 *   hidden: amount replaced by 6 dots (••••••)
 *
 * The eye toggle is optional. When [onToggleVisibility] is null the
 * eye control is hidden and the pill always shows [amount] (used by
 * the wallet's locked-balance secondary row).
 */
@Composable
fun BalancePill(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    isHidden: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label.uppercase(),
                color = TextMedium,
                fontSize = LabelFontSize,
                letterSpacing = LabelLetterSpacing,
            )
            // Both versions share the same slot so toggling visibility
            // never shifts surrounding layout. Mirrors the mockup recipe
            // (mockup/js/screens/lobby.js line 57: bal-stack inline-grid
            // col-start-1 row-start-1).
            Box {
                Text(
                    text = amount,
                    modifier = Modifier.alpha(if (isHidden) 0f else 1f),
                    color = TextHigh,
                    fontSize = AmountFontSize,
                    fontWeight = FontWeight.Bold,
                    // tabular-nums keeps the digits monospaced so amounts
                    // don't shift width when a single digit changes.
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
                Text(
                    text = HIDDEN_PLACEHOLDER,
                    modifier = Modifier.alpha(if (isHidden) 1f else 0f),
                    color = TextLow,
                    fontSize = AmountFontSize,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        if (onToggleVisibility != null) {
            Box(
                modifier =
                    Modifier
                        .padding(bottom = 8.dp)
                        .size(EyeHitArea)
                        .clickable(onClick = onToggleVisibility),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (isHidden) "Show balance" else "Hide balance",
                    tint = TextLow,
                    modifier = Modifier.size(EyeIconSize),
                )
            }
        }
    }
}

private const val HIDDEN_PLACEHOLDER = "••••••"

private val LabelFontSize = 10.sp
private val LabelLetterSpacing = 1.2.sp
private val AmountFontSize = 36.sp
private val EyeIconSize = 18.dp
private val EyeHitArea = 36.dp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun BalancePillShownPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            BalancePill(
                label = "Available",
                amount = "$1,248.50",
                onToggleVisibility = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun BalancePillHiddenPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            BalancePill(
                label = "Available",
                amount = "$1,248.50",
                isHidden = true,
                onToggleVisibility = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun BalancePillReadOnlyPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BalancePill(label = "Available", amount = "$1,248.50")
                Spacer(modifier = Modifier.height(4.dp))
                BalancePill(label = "Locked", amount = "$0.00")
            }
        }
    }
}
