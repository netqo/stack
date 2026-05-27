package com.plainstudio.stackcasino.feature.auth

import android.app.Activity
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.AccentVioletSoft
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceElevated
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Login screen reproducing the cu-02 mockup (default / returning /
 * loading / error states).
 *
 * Layout invariant: the column (meta row, hero, bottom action) keeps
 * the same vertical position across every state. The error banner is
 * an OVERLAY (Box.align(TopCenter)) so it sits over the meta row
 * instead of being part of the flow, which would push the hero down.
 *
 * Backdrop sources:
 *   * radial violet glow from the mockup style block
 *   * 24dp dotted grid (rgba(255,255,255,0.035) * opacity 0.4)
 */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToLobby -> onLoggedIn()
            }
        }
    }

    LoginScreenContent(
        state = state,
        onSignInClick = { activity?.let(viewModel::signIn) },
        onUseAnotherAccountClick = { activity?.let(viewModel::useAnotherAccount) },
        onDismissError = viewModel::dismissError,
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginUiState,
    onSignInClick: () -> Unit,
    onUseAnotherAccountClick: () -> Unit,
    onDismissError: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceBase,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .backgroundGlow()
                    .gridBackground(),
        ) {
            // Main column. Same layout across every state; the error
            // banner is rendered as an overlay below so the hero does
            // not shift.
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 24.dp),
            ) {
                TopMetaRow()
                Spacer(modifier = Modifier.height(8.dp))
                Hero(modifier = Modifier.weight(1f))
                BottomActionBlock(
                    state = state,
                    onSignInClick = onSignInClick,
                    onUseAnotherAccountClick = onUseAnotherAccountClick,
                )
            }

            if (state is LoginUiState.Error) {
                ErrorBanner(
                    message = state.message,
                    onDismiss = onDismissError,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TopMetaRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "PLAIN STUDIO",
            color = TextLow,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PulsingDot()
            Text(
                text = "POLYGON MAINNET",
                color = SemanticOk,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
private fun Hero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BreathingLogo()
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "STACK",
            color = TextHigh,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 7.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "CASINO",
            color = AccentVioletSoft,
            fontSize = 10.sp,
            letterSpacing = 3.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Every round is verifiable.\nEvery payout is on-chain.",
            color = TextMedium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        TrustBadgesRow()
    }
}

@Composable
private fun StackLogoGlyph() {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .border(width = 4.dp, color = AccentViolet),
        )
        Box(
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxSize()
                    .border(width = 2.dp, color = AccentVioletSoft),
        )
        Box(
            modifier =
                Modifier
                    .padding(20.dp)
                    .fillMaxSize()
                    .background(AccentViolet),
        )
    }
}

/**
 * Ports mockup `.logo-breathe` (styles.css):
 *   @keyframes logo-breathe {
 *     0%, 100% { opacity: 0.25; transform: scale(1.4); }
 *     50%      { opacity: 0.45; transform: scale(1.6); }
 *   }
 *   animation: 3s ease-in-out infinite
 *
 * Rendered as a radial-gradient violet halo behind [StackLogoGlyph].
 */
@Composable
private fun BreathingLogo() {
    val transition = rememberInfiniteTransition(label = "logo-breathe")
    val glowAlpha by transition.animateFloat(
        initialValue = LOGO_GLOW_ALPHA_MIN,
        targetValue = LOGO_GLOW_ALPHA_MAX,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = LOGO_BREATHE_MS, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "logo-breathe-alpha",
    )
    val glowScale by transition.animateFloat(
        initialValue = LOGO_GLOW_SCALE_MIN,
        targetValue = LOGO_GLOW_SCALE_MAX,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = LOGO_BREATHE_MS, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "logo-breathe-scale",
    )

    Box(
        modifier = Modifier.size(LOGO_HALO_BOX),
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
                    radius = size.minDimension * 0.5f * glowScale,
                )
            drawRect(brush = brush)
        }
        StackLogoGlyph()
    }
}

/**
 * Ports mockup `.pulse-dot` (styles.css):
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
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "dot-pulse")
    val haloSize by transition.animateFloat(
        initialValue = DOT_SIZE_PX,
        targetValue = DOT_SIZE_PX + DOT_HALO_SPREAD_PX * 2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MS, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dot-halo-size",
    )
    val haloAlpha by transition.animateFloat(
        initialValue = DOT_HALO_ALPHA_MAX,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MS, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "dot-halo-alpha",
    )
    val dotScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = DOT_SCALE_PEAK,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = DOT_PULSE_MS / 2, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "dot-scale",
    )

    Box(
        modifier = Modifier.size(DOT_CONTAINER),
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
private const val DOT_PULSE_MS = 1800
private val DOT_CONTAINER = 18.dp

private const val LOGO_GLOW_ALPHA_MIN = 0.25f
private const val LOGO_GLOW_ALPHA_MAX = 0.45f
private const val LOGO_GLOW_SCALE_MIN = 1.4f
private const val LOGO_GLOW_SCALE_MAX = 1.6f
private const val LOGO_BREATHE_MS = 3000
private val LOGO_HALO_BOX = 160.dp

@Composable
private fun TrustBadgesRow() {
    Row(
        modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TrustBadge(
            icon = Icons.Outlined.Shield,
            label = "PROVABLY FAIR",
            modifier = Modifier.weight(1f),
        )
        TrustBadge(
            icon = Icons.Outlined.Lock,
            label = "NON-CUSTODIAL",
            modifier = Modifier.weight(1f),
        )
        TrustBadge(
            icon = Icons.Outlined.Hexagon,
            label = "POLYGON",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TrustBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(SurfaceElevated.copy(alpha = 0.6f))
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentViolet,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            color = TextMedium,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BottomActionBlock(
    state: LoginUiState,
    onSignInClick: () -> Unit,
    onUseAnotherAccountClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (state) {
            LoginUiState.Loading -> LoadingButton()
            is LoginUiState.Returning -> {
                ContinueAsCard(state = state, onClick = onSignInClick)
                Spacer(modifier = Modifier.height(12.dp))
                UseAnotherAccountLink(onClick = onUseAnotherAccountClick)
            }
            else -> SignInButton(onClick = onSignInClick)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LegalFootnote()
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Banner styling approximates the mockup's `backdrop-blur` look:
    // an opaque-ish dark surface tinted red plus a strong red border.
    // True backdrop blur in Compose needs RenderEffect (API 31+) and
    // is not worth the conditional wiring at this stage.
    Surface(
        modifier =
            modifier
                .border(width = 1.dp, color = SemanticDanger),
        color = SurfaceBase.copy(alpha = 0.92f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(SemanticDanger.copy(alpha = 0.10f))
                    .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SIGN-IN FAILED",
                    color = SemanticDanger,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    color = TextHigh,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = SemanticDanger,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ContinueAsCard(
    state: LoginUiState.Returning,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = AccentViolet.copy(alpha = 0.5f))
                .clickable(onClick = onClick),
        color = AccentViolet.copy(alpha = 0.10f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(AccentViolet),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initialsOf(state.displayName),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CONTINUE AS",
                    color = AccentVioletSoft,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.displayName,
                    color = TextHigh,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = state.email,
                    color = TextMedium,
                    fontSize = 11.sp,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AccentViolet,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun UseAnotherAccountLink(onClick: () -> Unit) {
    Text(
        text = "USE ANOTHER ACCOUNT",
        color = TextMedium,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
    )
}

@Composable
private fun SignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .glowShadow(),
        shape = RectangleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = AccentViolet,
                contentColor = Color.White,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_google_g),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Sign in with Google",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LoadingButton() {
    Button(
        onClick = {},
        enabled = false,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
                .glowShadow(),
        shape = RectangleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = AccentViolet.copy(alpha = 0.6f),
                contentColor = Color.White,
                disabledContainerColor = AccentViolet.copy(alpha = 0.6f),
                disabledContentColor = Color.White,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
            Text(
                text = "Connecting to Google...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private val BUTTON_HEIGHT = 52.dp

// Mockup design token (mockup/js/config.js):
//   boxShadow.glow = '0 0 24px rgba(139,92,246,0.35)'
// Compose has no direct box-shadow primitive, so we emulate it with
// the ambient/spot colored shadow API. Elevation is tuned so the
// blur spread on a 52dp tall button reads close to the 24px radius
// of the source. clip=false lets the glow leak outside the button
// bounds, which is what the mockup expects.
private val ButtonGlowColor = Color(0xFF8B5CF6).copy(alpha = 0.35f)
private val ButtonGlowElevation = 12.dp

private fun Modifier.glowShadow(): Modifier =
    shadow(
        elevation = ButtonGlowElevation,
        shape = RectangleShape,
        ambientColor = ButtonGlowColor,
        spotColor = ButtonGlowColor,
        clip = false,
    )

@Composable
private fun LegalFootnote() {
    Text(
        text =
            "By continuing you agree to the Terms of Service and Privacy Policy.\n" +
                "Must be 18+ to play.",
        color = TextLow,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Splits the display name on whitespace and joins the first letter of
 * the first two tokens for the avatar tile (e.g. "John Doe" -> "JD").
 * Falls back to the first character when only a single word is present.
 */
private fun initialsOf(name: String): String {
    val tokens = name.split(' ').filter { it.isNotBlank() }
    return when (tokens.size) {
        0 -> ""
        1 -> tokens[0].first().uppercase()
        else -> "${tokens[0].first().uppercase()}${tokens[1].first().uppercase()}"
    }
}

// Radial glow tuning. Matches mockup/js/screens/login.js where the
// background style is composed from two radial gradients seeded at the
// hero (top) and bottom-left corners.
private const val TOP_GLOW_ALPHA = 0.22f
private const val TOP_GLOW_CENTER_Y_FRACTION = 0.32f
private const val TOP_GLOW_RADIUS_FRACTION = 0.65f
private const val BOTTOM_GLOW_ALPHA = 0.08f
private const val BOTTOM_GLOW_CENTER_X_FRACTION = 0.2f
private const val BOTTOM_GLOW_CENTER_Y_FRACTION = 0.9f
private const val BOTTOM_GLOW_RADIUS_FRACTION = 0.55f

// Grid background. Mockup styles.css uses
//   linear-gradient(rgba(255,255,255,0.035) 1px, transparent 1px)
//   * the parent has opacity-40, so the effective alpha is ~0.014
// We round up slightly so the grid stays perceptible after the violet
// glow saturates the surface.
private val GridLineColor = Color.White.copy(alpha = 0.035f)
private val GridCellSize = 24.dp
private val GridLineWidth = 1.dp

private fun Modifier.backgroundGlow(): Modifier =
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

private fun Modifier.gridBackground(): Modifier =
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

/**
 * Walks the ContextWrapper chain until it lands on an Activity.
 * Required because Credential Manager's getCredential expects an
 * Activity instance, but Compose only exposes LocalContext.
 */
private tailrec fun android.content.Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun LoginScreenIdlePreview() {
    StackcasinoTheme {
        LoginScreenContent(
            state = LoginUiState.Idle,
            onSignInClick = {},
            onUseAnotherAccountClick = {},
            onDismissError = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun LoginScreenReturningPreview() {
    StackcasinoTheme {
        LoginScreenContent(
            state =
                LoginUiState.Returning(
                    displayName = "John Doe",
                    email = "john.doe@gmail.com",
                    photoUrl = null,
                ),
            onSignInClick = {},
            onUseAnotherAccountClick = {},
            onDismissError = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun LoginScreenLoadingPreview() {
    StackcasinoTheme {
        LoginScreenContent(
            state = LoginUiState.Loading,
            onSignInClick = {},
            onUseAnotherAccountClick = {},
            onDismissError = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12)
@Composable
private fun LoginScreenErrorPreview() {
    StackcasinoTheme {
        LoginScreenContent(
            state =
                LoginUiState.Error(
                    "Google returned an error. Check your connection and try again.",
                ),
            onSignInClick = {},
            onUseAnotherAccountClick = {},
            onDismissError = {},
        )
    }
}
