package com.plainstudio.stackcasino.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Centered error card with retry. Used by Lobby ("Connection Lost"
 * with RETRY + USE CACHE), News ("Couldn't load articles" with RETRY
 * + LAST FETCH timestamp), and Profile ("Couldn't load profile" with
 * RETRY).
 *
 * Mockup spec (mockup/js/screens/{lobby,news,profile}.js):
 *
 *   container: 1px solid danger/50 border, rgba(danger,0.05) bg, p-6
 *   icon:      48dp container with danger border, danger tint
 *   title:     16sp semibold, danger color, mt-4
 *   message:   12sp txt-mid, line-height 1.25, mt-2
 *   primary:   px-4 py-2 violet outline button, tracked uppercase
 *   secondary: same as primary but transparent border
 *   footer:    text-[9px] tracked txt-lo, separated by border-t mt-4 pt-4
 *
 * The [secondaryAction] is optional (lobby uses it for USE CACHE; the
 * other screens omit it). The [footer] is optional (news uses it for
 * "LAST SUCCESSFUL FETCH 2H AGO").
 */
@Composable
fun ErrorState(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    footer: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SemanticDanger.copy(alpha = CONTAINER_TINT_ALPHA))
                .border(width = 1.dp, color = SemanticDanger.copy(alpha = CONTAINER_BORDER_ALPHA))
                .padding(ContainerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        Box(
            modifier =
                Modifier
                    .size(IconContainerSize)
                    .background(SemanticDanger.copy(alpha = CONTAINER_TINT_ALPHA))
                    .border(width = 1.dp, color = SemanticDanger.copy(alpha = CONTAINER_BORDER_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = title,
            color = SemanticDanger,
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
        ActionRow(
            primaryLabel = primaryActionLabel,
            onPrimary = onPrimaryAction,
            secondaryLabel = secondaryActionLabel,
            onSecondary = onSecondaryAction,
        )
        if (footer != null) {
            Spacer(modifier = Modifier.height(FooterGap))
            FooterRow(text = footer)
        }
    }
}

@Composable
private fun ActionRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(ActionGap)) {
        ErrorActionButton(label = primaryLabel, onClick = onPrimary, filled = true)
        if (secondaryLabel != null && onSecondary != null) {
            ErrorActionButton(label = secondaryLabel, onClick = onSecondary, filled = false)
        }
    }
}

@Composable
private fun ErrorActionButton(
    label: String,
    onClick: () -> Unit,
    filled: Boolean,
) {
    val background = if (filled) AccentViolet else Color.Transparent
    val border = if (filled) AccentViolet else SurfaceOutline
    val textColor = if (filled) Color.White else TextHigh
    Box(
        modifier =
            Modifier
                .background(background)
                .border(width = 1.dp, color = border)
                .clickable(onClick = onClick)
                .padding(ActionPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            color = textColor,
            fontSize = ActionFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = ActionLetterSpacing,
        )
    }
}

@Composable
private fun FooterRow(text: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceOutline))
        Spacer(modifier = Modifier.height(FooterGap))
        Text(
            text = text.uppercase(),
            color = TextLow,
            fontSize = FooterFontSize,
            letterSpacing = FooterLetterSpacing,
            textAlign = TextAlign.Center,
        )
    }
}

object ErrorStateDefaults {
    @Composable
    fun OfflineIcon() {
        Icon(
            imageVector = Icons.Outlined.WifiOff,
            contentDescription = null,
            tint = SemanticDanger,
            modifier = Modifier.size(IconImageSize),
        )
    }
}

private const val CONTAINER_TINT_ALPHA = 0.05f
private const val CONTAINER_BORDER_ALPHA = 0.5f
private val ContainerPadding = PaddingValues(24.dp)
private val IconContainerSize = 48.dp
private val IconImageSize = 24.dp
private val SectionGap = 12.dp
private val ActionGap = 8.dp
private val ActionPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
private val FooterGap = 8.dp
private val TitleFontSize = 16.sp
private val MessageFontSize = 12.sp
private val MessageLineHeight = 18.sp
private val ActionFontSize = 11.sp
private val ActionLetterSpacing = 1.2.sp
private val FooterFontSize = 9.sp
private val FooterLetterSpacing = 1.2.sp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ErrorStateBasicPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ErrorState(
                icon = { ErrorStateDefaults.OfflineIcon() },
                title = "Couldn't load profile",
                message = "Sync with Firestore failed. Pull to retry.",
                primaryActionLabel = "Retry",
                onPrimaryAction = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ErrorStateLobbyPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ErrorState(
                icon = { ErrorStateDefaults.OfflineIcon() },
                title = "Connection Lost",
                message = "Couldn't sync wallet and rounds from Firestore. Showing last known state.",
                primaryActionLabel = "Retry",
                onPrimaryAction = {},
                secondaryActionLabel = "Use cache",
                onSecondaryAction = {},
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun ErrorStateWithFooterPreview() {
    StackcasinoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ErrorState(
                icon = { ErrorStateDefaults.OfflineIcon() },
                title = "Couldn't load articles",
                message = "NewsAPI is unreachable. Cached articles still available below.",
                primaryActionLabel = "Retry",
                onPrimaryAction = {},
                footer = "Last successful fetch 2h ago",
            )
        }
    }
}
