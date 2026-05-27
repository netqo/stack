package com.plainstudio.stackcasino.feature.auth

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticOk

// ---------------------------------------------------------------------------
// Backgrounds
// ---------------------------------------------------------------------------

/**
 * Two stacked radial violet gradients reproducing the mockup body
 * background (see mockup/js/screens/login.js, `style="background: ..."`).
 * The top gradient frames the hero; the smaller bottom-left one adds
 * depth toward the call-to-action.
 */
internal fun Modifier.backgroundGlow(): Modifier =
    drawBehind {
        val topGlow =
            Brush.radialGradient(
                colors = listOf(AccentViolet.copy(alpha = TOP_GLOW_ALPHA), Color.Transparent),
                center = Offset(size.width / 2f, size.height * TOP_GLOW_CENTER_Y_FRACTION),
                radius = size.minDimension * TOP_GLOW_RADIUS_FRACTION,
            )
        val bottomGlow =
            Brush.radialGradient(
                colors = listOf(AccentViolet.copy(alpha = BOTTOM_GLOW_ALPHA), Color.Transparent),
                center =
                    Offset(
                        size.width * BOTTOM_GLOW_CENTER_X_FRACTION,
                        size.height * BOTTOM_GLOW_CENTER_Y_FRACTION,
                    ),
                radius = size.minDimension * BOTTOM_GLOW_RADIUS_FRACTION,
            )
        drawRect(brush = topGlow)
        drawRect(brush = bottomGlow)
    }

/**
 * Subtle 24dp dotted grid. Mockup CSS (styles.css):
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
 * the violet glow saturates the surface and a slightly stronger grid
 * stays perceptible.
 */
internal fun Modifier.gridBackground(): Modifier =
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

// ---------------------------------------------------------------------------
// Brand glow shadow (sign-in / loading button)
// ---------------------------------------------------------------------------

/**
 * Approximates the mockup design token (mockup/js/config.js):
 *
 *   boxShadow.glow = '0 0 24px rgba(139,92,246,0.35)'
 *
 * Compose has no direct CSS box-shadow, so we use the ambient/spot
 * colored-shadow API. Elevation is tuned so the blur spread on a 52dp
 * tall button reads close to the 24px radius of the source.
 * `clip = false` lets the glow leak outside the button bounds.
 */
internal fun Modifier.glowShadow(): Modifier =
    shadow(
        elevation = ButtonGlowElevation,
        shape = RectangleShape,
        ambientColor = ButtonGlowColor,
        spotColor = ButtonGlowColor,
        clip = false,
    )

// ---------------------------------------------------------------------------
// Brand stack logo + breathing halo
// ---------------------------------------------------------------------------

/**
 * Ports mockup `.logo-breathe` (styles.css):
 *
 *   @keyframes logo-breathe {
 *     0%, 100% { opacity: 0.25; transform: scale(1.4); }
 *     50%      { opacity: 0.45; transform: scale(1.6); }
 *   }
 *   animation: 3s ease-in-out infinite
 *
 * Rendered as a radial-gradient violet halo behind the brand stack
 * glyph. The glyph itself is rendered by [content], so the caller
 * controls its shape.
 */
@Composable
internal fun BreathingLogo(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "logo-breathe")
    val glowAlpha by transition.animateFloat(
        initialValue = LOGO_GLOW_ALPHA_MIN,
        targetValue = LOGO_GLOW_ALPHA_MAX,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = LOGO_BREATHE_MILLIS, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "logo-breathe-alpha",
    )
    val glowScale by transition.animateFloat(
        initialValue = LOGO_GLOW_SCALE_MIN,
        targetValue = LOGO_GLOW_SCALE_MAX,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = LOGO_BREATHE_MILLIS, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "logo-breathe-scale",
    )

    Box(
        modifier = Modifier.size(LogoHaloBoxSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            AccentViolet.copy(alpha = glowAlpha),
                            Color.Transparent,
                        ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * LOGO_HALO_RADIUS_FRACTION * glowScale,
                )
            drawRect(brush = brush)
        }
        content()
    }
}

// ---------------------------------------------------------------------------
// Polygon Mainnet pulsing dot
// ---------------------------------------------------------------------------

/**
 * Ports mockup `.pulse-dot` (styles.css):
 *
 *   @keyframes dot-pulse {
 *     0%, 100% { box-shadow: 0 0 0 0 rgba(34,197,94,0.55); transform: scale(1); }
 *     50%      { box-shadow: 0 0 0 6px rgba(34,197,94,0);   transform: scale(1.15); }
 *   }
 *   animation: 1.8s ease-in-out infinite
 *
 * The 6px box-shadow spread is rendered as an expanding green square
 * behind the solid dot; the inner dot scales 1.0 -> 1.15 in step.
 */
@Composable
internal fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "dot-pulse")
    val haloSize by transition.animateFloat(
        initialValue = DOT_SIZE_PX,
        targetValue = DOT_SIZE_PX + DOT_HALO_SPREAD_PX * 2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MILLIS, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dot-halo-size",
    )
    val haloAlpha by transition.animateFloat(
        initialValue = DOT_HALO_ALPHA_MAX,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MILLIS, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dot-halo-alpha",
    )
    val dotScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = DOT_SCALE_PEAK,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MILLIS / 2, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "dot-scale",
    )

    Box(
        modifier = Modifier.size(DotContainerSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(haloSize.dp)
                    .background(SemanticOk.copy(alpha = haloAlpha)),
        )
        Box(
            modifier =
                Modifier
                    .size(DOT_SIZE_PX.dp)
                    .graphicsLayer {
                        scaleX = dotScale
                        scaleY = dotScale
                    }.background(SemanticOk),
        )
    }
}

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

// Radial glow tuning. Centers and radii expressed as fractions of the
// drawing surface so the result scales with viewport size.
private const val TOP_GLOW_ALPHA = 0.22f
private const val TOP_GLOW_CENTER_Y_FRACTION = 0.32f
private const val TOP_GLOW_RADIUS_FRACTION = 0.65f
private const val BOTTOM_GLOW_ALPHA = 0.08f
private const val BOTTOM_GLOW_CENTER_X_FRACTION = 0.2f
private const val BOTTOM_GLOW_CENTER_Y_FRACTION = 0.9f
private const val BOTTOM_GLOW_RADIUS_FRACTION = 0.55f

// Grid background.
private val GridLineColor = Color.White.copy(alpha = 0.035f)
private val GridCellSize = 24.dp
private val GridLineWidth = 1.dp

// Brand glow shadow.
private val ButtonGlowColor = AccentViolet.copy(alpha = 0.35f)
private val ButtonGlowElevation = 12.dp

// Breathing logo halo.
private const val LOGO_GLOW_ALPHA_MIN = 0.25f
private const val LOGO_GLOW_ALPHA_MAX = 0.45f
private const val LOGO_GLOW_SCALE_MIN = 1.4f
private const val LOGO_GLOW_SCALE_MAX = 1.6f
private const val LOGO_BREATHE_MILLIS = 3000
private const val LOGO_HALO_RADIUS_FRACTION = 0.5f
private val LogoHaloBoxSize = 160.dp

// Pulsing dot.
private const val DOT_SIZE_PX = 6f
private const val DOT_HALO_SPREAD_PX = 6f
private const val DOT_HALO_ALPHA_MAX = 0.55f
private const val DOT_SCALE_PEAK = 1.15f
private const val DOT_PULSE_MILLIS = 1800
private val DotContainerSize = 18.dp
