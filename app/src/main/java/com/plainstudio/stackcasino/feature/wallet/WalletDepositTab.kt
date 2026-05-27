package com.plainstudio.stackcasino.feature.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.AccentVioletSoft
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.SemanticWarn
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Deposit tab. Renders the mock QR card, the deposit address row with
 * copy / share actions, the Polygon network chip and the lossy-network
 * warning. Copy / share are visual stubs; real clipboard + share
 * intents ship with the wallet repository in a later card.
 */
@Composable
internal fun WalletDepositTab(address: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        DepositCard(address = address)
        DepositWarning()
    }
}

@Composable
private fun DepositCard(address: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(DepositCardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QrCodeMock()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "YOUR DEPOSIT ADDRESS",
            color = TextMedium,
            fontSize = LabelFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(12.dp))
        AddressRow(address = address)
        Spacer(modifier = Modifier.height(16.dp))
        NetworkChip()
    }
}

/**
 * Mock QR. The mockup draws a dot grid plus three finder squares and a
 * STACK glyph in the centre. The drawing is decorative; the actual
 * address lives in [AddressRow] right below it.
 */
@Composable
private fun QrCodeMock() {
    Box(
        modifier =
            Modifier
                .size(QrSize)
                .background(SurfaceElevated)
                .padding(QrInnerPadding),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = QR_DOT_STEP_PX
            val radius = QR_DOT_RADIUS_PX
            var y = step / 2f
            while (y < size.height) {
                var x = step / 2f
                while (x < size.width) {
                    drawCircle(
                        color = AccentViolet,
                        radius = radius,
                        center = Offset(x, y),
                    )
                    x += step
                }
                y += step
            }
        }
        QrFinderSquare(modifier = Modifier.align(Alignment.TopStart))
        QrFinderSquare(modifier = Modifier.align(Alignment.TopEnd))
        QrFinderSquare(modifier = Modifier.align(Alignment.BottomStart))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            QrCenterGlyph()
        }
    }
}

@Composable
private fun QrFinderSquare(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(QrFinderSize)
                .background(SurfaceElevated)
                .border(width = QrFinderBorderWidth, color = AccentViolet),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(QrFinderDotSize)
                    .background(AccentViolet),
        )
    }
}

@Composable
private fun QrCenterGlyph() {
    Box(
        modifier =
            Modifier
                .size(QrGlyphOuterSize)
                .background(SurfaceElevated)
                .padding(QrGlyphInnerPadding),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .border(width = QrGlyphOuterBorder, color = AccentViolet),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(QrGlyphMiddleInset)
                    .border(width = QrGlyphMiddleBorder, color = AccentVioletSoft),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(QrGlyphCoreInset)
                    .background(AccentViolet),
        )
    }
}

@Composable
private fun AddressRow(address: String) {
    // height(IntrinsicSize.Max) pins the row height to its tallest
    // intrinsic child (the wrapped address), letting the action
    // buttons' fillMaxHeight actually resolve. Without it, Row defers
    // to wrap-content and fillMaxHeight collapses to 0.
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .border(width = 1.dp, color = SurfaceOutline)
                .background(SurfaceBase),
    ) {
        Text(
            text = address,
            color = TextHigh,
            fontSize = AddressFontSize,
            fontFamily = FontFamily.Monospace,
            lineHeight = AddressLineHeight,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
        )
        AddressActionButton(
            icon = Icons.Outlined.IosShare,
            contentDescription = "Share address",
            background = SurfaceElevated,
            tint = TextMedium,
            onClick = { /* share intent ships with wallet repository */ },
        )
        AddressActionButton(
            icon = Icons.Outlined.ContentCopy,
            contentDescription = "Copy address",
            background = AccentViolet,
            tint = Color.White,
            onClick = { /* clipboard write ships with wallet repository */ },
        )
    }
}

@Composable
private fun AddressActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    // fillMaxHeight + aspectRatio(1f) keeps each button square at
    // whatever height the wrapped mono address pushes the row to,
    // instead of being a fixed 44dp width that ends up rectangular.
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .background(background)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(AddressButtonIconSize),
        )
    }
}

@Composable
private fun NetworkChip() {
    Row(
        modifier =
            Modifier
                .background(SurfaceElevated)
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(NetworkDotSize)
                    .background(SemanticOk),
        )
        Text(
            text = "POLYGON · USDC/USDT",
            color = TextHigh,
            fontSize = LabelFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

@Composable
private fun DepositWarning() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SemanticWarn.copy(alpha = WARNING_BG_ALPHA))
                .border(width = 1.dp, color = SemanticWarn.copy(alpha = WARNING_BORDER_ALPHA))
                .padding(WarningPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = SemanticWarn,
            modifier =
                Modifier
                    .size(WarningIconSize)
                    .padding(top = 2.dp),
        )
        Text(
            text = depositWarningMessage(),
            color = TextMedium,
            fontSize = WarningTextSize,
            lineHeight = WarningTextLineHeight,
            textAlign = TextAlign.Start,
        )
    }
}

private fun depositWarningMessage(): AnnotatedString =
    buildAnnotatedString {
        append("Send only ")
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) { append("USDC") }
        append(" or ")
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold)) { append("USDT") }
        append(
            " on the Polygon network to this address. Other tokens or " +
                "networks may result in loss of funds.",
        )
    }

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

private val DepositCardPadding = PaddingValues(20.dp)

// Mock QR sizing. Hardcoded to a 192dp box so the dot grid + finder
// squares + glyph all fit the proportions the mockup uses.
private val QrSize = 192.dp
private val QrInnerPadding = PaddingValues(16.dp)
private const val QR_DOT_STEP_PX = 14f
private const val QR_DOT_RADIUS_PX = 2.8f
private val QrFinderSize = 36.dp
private val QrFinderBorderWidth = 2.dp
private val QrFinderDotSize = 12.dp
private val QrGlyphOuterSize = 48.dp
private val QrGlyphInnerPadding = PaddingValues(4.dp)
private val QrGlyphOuterBorder = 2.dp
private val QrGlyphMiddleBorder = 1.dp
private val QrGlyphMiddleInset = 6.dp
private val QrGlyphCoreInset = 10.dp

private val AddressFontSize = 11.sp
private val AddressLineHeight = 14.sp
private val AddressButtonIconSize = 16.dp

private val NetworkDotSize = 6.dp

private val WarningPadding = PaddingValues(16.dp)
private val WarningIconSize = 18.dp
private val WarningTextSize = 12.sp
private val WarningTextLineHeight = 18.sp
private const val WARNING_BG_ALPHA = 0.05f
private const val WARNING_BORDER_ALPHA = 0.40f

private val LabelFontSize = 10.sp
