package com.plainstudio.stackcasino.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated

/**
 * Loading placeholder primitive. The caller decides shape via the
 * passed [Modifier] (`.size(...)`, `.fillMaxWidth().height(...)`,
 * `.fillMaxWidth().aspectRatio(...)` etc.), which matches the
 * mockup's inline `animate-pulse` blocks where every screen sizes the
 * placeholder to the slot it occupies.
 *
 * Ports Tailwind `animate-pulse` from mockup/styles.css:
 *
 *   @keyframes pulse {
 *     0%, 100% { opacity: 1; }
 *     50%      { opacity: 0.5; }
 *   }
 *   animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite
 *
 * The background tone is SurfaceElevated so the placeholder reads as
 * a raised block on the SurfaceBase / SurfaceRaised parents the
 * screens use.
 */
@Composable
fun Skeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton-pulse")
    val alpha by transition.animateFloat(
        initialValue = PULSE_ALPHA_MAX,
        targetValue = PULSE_ALPHA_MIN,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = PULSE_DURATION_MILLIS, easing = PulseEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "skeleton-alpha",
    )
    Box(
        modifier =
            modifier
                .alpha(alpha)
                .background(SurfaceElevated),
    )
}

private const val PULSE_ALPHA_MIN = 0.5f
private const val PULSE_ALPHA_MAX = 1f
private const val PULSE_DURATION_MILLIS = 1000

// Mirrors Tailwind's `animate-pulse` cubic-bezier(0.4, 0, 0.6, 1).
private val PulseEasing = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun SkeletonShapesPreview() {
    StackcasinoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Lines (e.g. text placeholders in lobby/news loading).
            Skeleton(modifier = Modifier.width(96.dp).height(8.dp))
            Skeleton(modifier = Modifier.width(224.dp).height(8.dp))
            Skeleton(modifier = Modifier.width(112.dp).height(8.dp))

            // Block (e.g. balance hero, news featured image).
            Skeleton(modifier = Modifier.fillMaxWidth().height(40.dp))

            // Avatar circle equivalent (square because the theme bans
            // border radius globally).
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Skeleton(modifier = Modifier.size(64.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Skeleton(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
                    Skeleton(modifier = Modifier.fillMaxWidth(0.4f).height(8.dp))
                }
            }
        }
    }
}
