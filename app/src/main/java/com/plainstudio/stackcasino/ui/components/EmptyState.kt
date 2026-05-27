package com.plainstudio.stackcasino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Centered "nothing here yet" state for empty lists and zero-data
 * screens (history with no rounds yet, news filtered to nothing, KYC
 * intro before any document is uploaded).
 *
 * Mockup spec (mockup/js/screens/wallet.js, kyc.js, history empty):
 *
 *   icon container: 56dp square, border-line, bg-surface
 *   icon color:     txt-lo
 *   title:          16sp semibold, txt-hi, mt-4
 *   message:        12sp regular, txt-mid, mt-1, line-height 1.25
 *   optional CTA:   violet button below message
 *   layout:         centered, padded py-12, max width ~280dp
 *
 * [icon] is a slot so the caller picks the imageVector / drawable
 * appropriate to the context.
 */
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = VerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IconToTitleGap, Alignment.Top),
    ) {
        Box(
            modifier =
                Modifier
                    .size(IconContainerSize)
                    .background(SurfaceRaised)
                    .border(width = 1.dp, color = SurfaceOutline),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TitleToMessageGap),
            modifier = Modifier.widthIn(max = MessageMaxWidth),
        ) {
            Text(
                text = title,
                color = TextHigh,
                fontSize = TitleFontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                color = TextMedium,
                fontSize = MessageFontSize,
                lineHeight = MessageLineHeight,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel.uppercase(),
                color = AccentViolet,
                fontSize = ActionFontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = ActionLetterSpacing,
                modifier =
                    Modifier
                        .border(width = 1.dp, color = AccentViolet)
                        .clickable(onClick = onAction)
                        .padding(ActionPadding),
            )
        }
    }
}

object EmptyStateDefaults {
    @Composable
    fun PlaceholderIcon() {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = TextLow,
            modifier = Modifier.size(IconImageSize),
        )
    }
}

private val IconContainerSize = 56.dp
private val IconImageSize = 28.dp
private val IconToTitleGap = 16.dp
private val TitleToMessageGap = 8.dp
private val VerticalPadding = 48.dp
private val MessageMaxWidth = 280.dp
private val TitleFontSize = 16.sp
private val MessageFontSize = 12.sp
private val MessageLineHeight = 18.sp
private val ActionFontSize = 11.sp
private val ActionLetterSpacing = 1.2.sp
private val ActionPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun EmptyStateBasicPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EmptyState(
                icon = { EmptyStateDefaults.PlaceholderIcon() },
                title = "No rounds yet",
                message = "Play your first round and it will show up here.",
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun EmptyStateWithActionPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EmptyState(
                icon = { EmptyStateDefaults.PlaceholderIcon() },
                title = "No articles match your search",
                message = "Try a different keyword or clear the source filters.",
                actionLabel = "Clear filters",
                onAction = {},
            )
        }
    }
}
