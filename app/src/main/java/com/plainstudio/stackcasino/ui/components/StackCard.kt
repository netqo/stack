package com.plainstudio.stackcasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh

/**
 * Generic bordered container used across the app's primary screens.
 *
 * Mirrors the mockup `border border-line bg-surface` recipe
 * (mockup/js/screens/{lobby,wallet,history,profile}.js). Sharp corners
 * are enforced project-wide by the theme — the mockup applies
 * `* { border-radius:0 !important }` globally.
 *
 * Three orthogonal variations capture every observed usage:
 *
 *   * [leftAccent] paints a 3dp left strip in the given color. Used
 *     by history round items and wallet transactions to encode
 *     win/loss / deposit/withdrawal in a single glance.
 *   * [elevated] swaps the surface tone (SurfaceRaised -> SurfaceElevated)
 *     to match the mockup `bg-elev` variant.
 *   * [onClick] enables ripple feedback when the card is interactive.
 *
 * Content padding defaults to 16dp (the mockup's `p-4`). Callers can
 * override via [contentPadding] for the rare `p-5` variant.
 */
@Composable
fun StackCard(
    modifier: Modifier = Modifier,
    leftAccent: Color? = null,
    elevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = StackCardDefaults.contentPadding,
    content: @Composable () -> Unit,
) {
    val surfaceColor = if (elevated) SurfaceElevated else SurfaceRaised
    val clickModifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    Row(
        modifier =
            modifier
                .background(surfaceColor)
                .border(width = 1.dp, color = SurfaceOutline)
                .then(clickModifier),
    ) {
        if (leftAccent != null) {
            Box(
                modifier =
                    Modifier
                        .width(StackCardDefaults.accentWidth)
                        .fillMaxHeight()
                        .background(leftAccent),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().padding(contentPadding)) {
            content()
        }
    }
}

object StackCardDefaults {
    val contentPadding: PaddingValues = PaddingValues(16.dp)
    val accentWidth = 3.dp
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun StackCardDefaultPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            StackCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Plain card", color = TextHigh)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun StackCardWithAccentsPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.padding(top = 0.dp))
            StackCard(
                modifier = Modifier.fillMaxWidth(),
                leftAccent = SemanticOk,
                onClick = {},
            ) {
                Text(text = "Win round (clickable)", color = TextHigh)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun StackCardElevatedDangerPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            StackCard(
                modifier = Modifier.fillMaxWidth(),
                leftAccent = SemanticDanger,
                elevated = true,
            ) {
                Text(text = "Loss round (elevated)", color = TextHigh)
            }
        }
    }
}
