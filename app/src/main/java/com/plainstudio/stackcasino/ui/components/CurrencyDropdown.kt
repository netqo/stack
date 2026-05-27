package com.plainstudio.stackcasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow

/**
 * Currency picker shown under the balance hero on the lobby and the
 * wallet. Tapping toggles a DropdownMenu listing the supplied [options];
 * picking one updates the displayed code.
 *
 * Mockup spec (mockup/js/screens/{lobby,wallet}.js, `data-ccy-toggle`):
 *
 *   trigger:  tracked text-[10px] text-txt-lo with chevron-down
 *   menu:     min-w-[120px], border-violet/40, bg-elev
 *   items:    flex gap-2 + 6dp dot (violet on active, transparent
 *             otherwise) + tracked text-[10px] font-semibold
 *
 * [networkLabel] is shown next to the code (e.g. "Polygon") and stays
 * static because the supported currencies all live on the same network
 * in the product today.
 */
@Composable
fun CurrencyDropdown(
    initialCurrency: String,
    networkLabel: String,
    modifier: Modifier = Modifier,
    options: List<String> = DefaultOptions,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(initialCurrency) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "$selected · $networkLabel",
                color = TextLow,
                fontSize = LabelFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = TextLow,
                modifier = Modifier.size(ChevronSize),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceElevated),
        ) {
            options.forEach { code ->
                DropdownMenuItem(
                    text = { CurrencyMenuRow(code = code, isSelected = code == selected) },
                    onClick = {
                        selected = code
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CurrencyMenuRow(
    code: String,
    isSelected: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(DotSize)
                    .background(if (isSelected) AccentViolet else Color.Transparent),
        )
        Text(
            text = code,
            color = if (isSelected) AccentViolet else TextHigh,
            fontSize = LabelFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

private val DefaultOptions = listOf("USDC", "USDT")
private val LabelFontSize = 10.sp
private val TrackedLetterSpacing = 1.2.sp
private val ChevronSize = 10.dp
private val DotSize = 6.dp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun CurrencyDropdownPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CurrencyDropdown(initialCurrency = "USDC", networkLabel = "Polygon")
        }
    }
}
