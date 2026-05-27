package com.plainstudio.stackcasino.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Subtle 24dp dotted grid drawn behind a screen. Used by every primary
 * screen wearing the mockup `.grid-bg` recipe (login, lobby, wallet,
 * history, news, profile).
 *
 * Source (mockup/styles.css):
 *
 *   .grid-bg {
 *     background-image:
 *       linear-gradient(rgba(255,255,255,0.035) 1px, transparent 1px),
 *       linear-gradient(90deg, rgba(255,255,255,0.035) 1px, transparent 1px);
 *     background-size: 24px 24px;
 *   }
 *
 * The mockup wraps the grid div with `opacity-40` which would bring the
 * effective alpha down to ~0.014; we keep the underlying 0.035 because
 * the dark surfaces hide a fainter grid entirely.
 */
fun Modifier.gridBackground(): Modifier =
    drawBehind {
        val cellPx = GridCellSize.toPx()
        val strokePx = GridLineWidth.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = GridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokePx,
            )
            x += cellPx
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = GridLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokePx,
            )
            y += cellPx
        }
    }

private val GridLineColor = Color.White.copy(alpha = 0.035f)
private val GridCellSize = 24.dp
private val GridLineWidth = 1.dp
