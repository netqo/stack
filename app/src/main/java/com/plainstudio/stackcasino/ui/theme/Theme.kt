package com.plainstudio.stackcasino.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark-only color scheme. The mockup commits to a single dark identity
// (see styles.css and js/config.js): no light variant, no dynamic color,
// so brand recognition stays consistent across devices and Android
// versions.
internal val DarkColorScheme =
    darkColorScheme(
        primary = AccentViolet,
        onPrimary = Color.White,
        primaryContainer = AccentVioletSoft,
        onPrimaryContainer = SurfaceBase,
        secondary = AccentVioletSoft,
        onSecondary = SurfaceBase,
        tertiary = SemanticInfo,
        onTertiary = Color.White,
        background = SurfaceBase,
        onBackground = TextHigh,
        surface = SurfaceRaised,
        onSurface = TextHigh,
        surfaceVariant = SurfaceElevated,
        onSurfaceVariant = TextMedium,
        outline = TextLow,
        outlineVariant = SurfaceOutline,
        error = SemanticDanger,
        onError = Color.White,
    )

@Composable
fun StackcasinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
