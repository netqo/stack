package com.plainstudio.stackcasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Single-select horizontal chip row used by history filters, wallet
 * transaction tabs and news source filters.
 *
 * Mockup spec (mockup/js/screens/{history,wallet,news}.js,
 * mockup `.fchip` recipe):
 *
 *   chip:     px-3 py-1.5 tracked text-[10px] font-semibold
 *   active:   bg-violet text-white border-violet
 *   inactive: text-txt-mid border-line, hover:border-violet
 *   gap:      8dp between chips
 *
 * Generic over the chip key type so callers stay type-safe (an enum,
 * sealed type, or domain id works equally well).
 *
 * Layout flags (mutually exclusive; precedence order is the same as the
 * parameter list):
 *   * [scrollable] -> horizontal-scroll lane (news source filters).
 *   * [wrap]       -> FlowRow that wraps onto multiple lines when the
 *                     chip set overflows (history game filter).
 *   * default      -> single Row (wallet transaction tabs, history
 *                     result filter, and any tight chip set).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilterChipRow(
    chips: List<FilterChip<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    wrap: Boolean = false,
) {
    when {
        scrollable -> {
            Row(
                modifier = modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { chip ->
                    Chip(label = chip.label, isActive = chip.key == selected, onClick = { onSelect(chip.key) })
                }
            }
        }
        wrap -> {
            FlowRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalArrangement = Arrangement.spacedBy(ChipGap),
            ) {
                chips.forEach { chip ->
                    Chip(label = chip.label, isActive = chip.key == selected, onClick = { onSelect(chip.key) })
                }
            }
        }
        else -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { chip ->
                    Chip(label = chip.label, isActive = chip.key == selected, onClick = { onSelect(chip.key) })
                }
            }
        }
    }
}

data class FilterChip<T>(
    val key: T,
    val label: String,
)

@Composable
private fun Chip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isActive) AccentViolet else Color.Transparent
    val borderColor = if (isActive) AccentViolet else SurfaceOutline
    val textColor = if (isActive) Color.White else TextMedium
    Box(
        modifier =
            Modifier
                .background(backgroundColor)
                .border(width = 1.dp, color = borderColor)
                .clickable(onClick = onClick)
                .padding(horizontal = ChipPaddingHorizontal, vertical = ChipPaddingVertical),
    ) {
        Text(
            text = label.uppercase(),
            color = textColor,
            fontSize = ChipFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = ChipLetterSpacing,
        )
    }
}

private val ChipGap = 8.dp
private val ChipPaddingHorizontal = 12.dp
private val ChipPaddingVertical = 6.dp
private val ChipFontSize = 10.sp
private val ChipLetterSpacing = 1.2.sp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun FilterChipRowPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            val chips =
                listOf(
                    FilterChip(key = "all", label = "All"),
                    FilterChip(key = "wins", label = "Wins"),
                    FilterChip(key = "losses", label = "Losses"),
                )
            FilterChipRow(
                chips = chips,
                selected = "wins",
                onSelect = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun FilterChipRowScrollablePreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            val chips =
                listOf(
                    FilterChip(key = "all", label = "All sources"),
                    FilterChip(key = "crypto", label = "CryptoNews"),
                    FilterChip(key = "defi", label = "Defi Daily"),
                    FilterChip(key = "polygon", label = "Polygon Post"),
                    FilterChip(key = "block", label = "Blockchain News"),
                )
            FilterChipRow(
                chips = chips,
                selected = "all",
                onSelect = {},
                scrollable = true,
            )
        }
    }
}
