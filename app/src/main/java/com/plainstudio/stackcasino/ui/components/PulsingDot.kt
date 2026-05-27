package com.plainstudio.stackcasino.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.theme.SemanticOk

/**
 * Pulsing green dot used as the connection indicator on the login
 * meta row and the Nep assistant header.
 *
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
fun PulsingDot() {
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

private const val DOT_SIZE_PX = 6f
private const val DOT_HALO_SPREAD_PX = 6f
private const val DOT_HALO_ALPHA_MAX = 0.55f
private const val DOT_SCALE_PEAK = 1.15f
private const val DOT_PULSE_MILLIS = 1800
private val DotContainerSize = 18.dp
