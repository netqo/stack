package com.plainstudio.stackcasino.ui.theme

import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks the design tokens that downstream screens consume.
 *
 * The exact hex values come from the mockup design tokens
 * (mockup/js/config.js and mockup/styles.css). Any accidental drift
 * here would change the brand identity, so the test fails loud.
 */
class StackcasinoThemeTest {
    @Test
    fun `AccentViolet matches mockup token #8B5CF6`() {
        assertEquals(0xFF8B5CF6.toInt(), AccentViolet.toArgb())
    }

    @Test
    fun `AccentVioletSoft matches mockup token #A78BFA`() {
        assertEquals(0xFFA78BFA.toInt(), AccentVioletSoft.toArgb())
    }

    @Test
    fun `SurfaceBase matches mockup token #0B0B12`() {
        assertEquals(0xFF0B0B12.toInt(), SurfaceBase.toArgb())
    }

    @Test
    fun `DarkColorScheme primary is AccentViolet`() {
        assertEquals(AccentViolet, DarkColorScheme.primary)
    }

    @Test
    fun `DarkColorScheme background is SurfaceBase`() {
        assertEquals(SurfaceBase, DarkColorScheme.background)
    }

    @Test
    fun `DarkColorScheme error is SemanticDanger`() {
        assertEquals(SemanticDanger, DarkColorScheme.error)
    }
}
