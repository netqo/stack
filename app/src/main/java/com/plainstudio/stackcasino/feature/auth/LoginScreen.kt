package com.plainstudio.stackcasino.feature.auth

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.plainstudio.stackcasino.ui.components.PulsingDot
import com.plainstudio.stackcasino.ui.components.gridBackground
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
 * Backdrop sources live in LoginScreenEffects.kt:
 *   * Modifier.backgroundGlow - radial violet glow
 *   * Modifier.gridBackground - 24dp dotted grid
 *   * BreathingLogo / PulsingDot - infinite-transition animations
 *   * Modifier.glowShadow - sign-in button violet glow
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
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = ScreenHorizontalPadding)
                        .padding(top = ScreenVerticalPadding, bottom = ScreenVerticalPadding),
            ) {
                TopMetaRow()
                Spacer(modifier = Modifier.height(MetaRowToHeroGap))
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
                            .padding(horizontal = ErrorBannerHorizontalPadding)
                            .padding(top = ErrorBannerTopPadding),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Top meta row + hero
// ---------------------------------------------------------------------------

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
            letterSpacing = LetterSpacingMeta,
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
                letterSpacing = LetterSpacingMeta,
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
        BreathingLogo { StackLogoGlyph() }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "STACK",
            color = TextHigh,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = LetterSpacingHero,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "CASINO",
            color = AccentVioletSoft,
            fontSize = 10.sp,
            letterSpacing = LetterSpacingSubtitle,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Every round is verifiable.\nEvery payout is on-chain.",
            color = TextMedium,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.widthIn(max = HeroTaglineMaxWidth),
        )
        Spacer(modifier = Modifier.height(28.dp))
        TrustBadgesRow()
    }
}

@Composable
private fun StackLogoGlyph() {
    Box(
        modifier = Modifier.size(StackLogoSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .border(width = StackLogoOuterBorder, color = AccentViolet),
        )
        Box(
            modifier =
                Modifier
                    .padding(StackLogoMiddleInset)
                    .fillMaxSize()
                    .border(width = StackLogoMiddleBorder, color = AccentVioletSoft),
        )
        Box(
            modifier =
                Modifier
                    .padding(StackLogoCoreInset)
                    .fillMaxSize()
                    .background(AccentViolet),
        )
    }
}

@Composable
private fun TrustBadgesRow() {
    Row(
        modifier = Modifier.widthIn(max = TrustBadgesMaxWidth),
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
                .background(SurfaceElevated.copy(alpha = BADGE_BACKGROUND_ALPHA))
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentViolet,
            modifier = Modifier.size(TrustBadgeIconSize),
        )
        Text(
            text = label,
            color = TextMedium,
            fontSize = 8.sp,
            letterSpacing = LetterSpacingTight,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom action block (sign-in / loading / continue as)
// ---------------------------------------------------------------------------

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
private fun SignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ButtonHeight)
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
                modifier = Modifier.size(GoogleGlyphSize),
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
                .height(ButtonHeight)
                .glowShadow(),
        shape = RectangleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = AccentViolet.copy(alpha = LOADING_BUTTON_ALPHA),
                contentColor = Color.White,
                disabledContainerColor = AccentViolet.copy(alpha = LOADING_BUTTON_ALPHA),
                disabledContentColor = Color.White,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(LoadingSpinnerSize),
                color = Color.White,
                strokeWidth = LoadingSpinnerStroke,
            )
            Text(
                text = "Connecting to Google...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
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
                .border(width = 1.dp, color = AccentViolet.copy(alpha = RETURNING_CARD_BORDER_ALPHA))
                .clickable(onClick = onClick),
        color = AccentViolet.copy(alpha = RETURNING_CARD_BACKGROUND_ALPHA),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.background(AccentViolet).size(ContinueAsAvatarSize),
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
                    letterSpacing = LetterSpacingTracked,
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
                modifier = Modifier.size(ContinueAsChevronSize),
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
        letterSpacing = LetterSpacingTracked,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
    )
}

@Composable
private fun LegalFootnote() {
    Text(
        text =
            "By continuing you agree to the Terms of Service and Privacy Policy.\n" +
                "Must be 18+ to play.",
        color = TextLow,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        letterSpacing = LetterSpacingLegal,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ---------------------------------------------------------------------------
// Error banner (overlay so the hero never shifts)
// ---------------------------------------------------------------------------

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
        modifier = modifier.border(width = 1.dp, color = SemanticDanger),
        color = SurfaceBase.copy(alpha = ERROR_BANNER_SURFACE_ALPHA),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(SemanticDanger.copy(alpha = ERROR_BANNER_TINT_ALPHA))
                    .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SIGN-IN FAILED",
                    color = SemanticDanger,
                    fontSize = 10.sp,
                    letterSpacing = LetterSpacingTracked,
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
                modifier = Modifier.size(ErrorDismissHitArea).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = SemanticDanger,
                    modifier = Modifier.size(ErrorDismissIconSize),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

// Screen frame.
private val ScreenHorizontalPadding = 20.dp
private val ScreenVerticalPadding = 24.dp
private val MetaRowToHeroGap = 8.dp
private val ErrorBannerHorizontalPadding = 12.dp
private val ErrorBannerTopPadding = 12.dp

// Hero / brand logo.
private val StackLogoSize = 80.dp
private val StackLogoOuterBorder = 4.dp
private val StackLogoMiddleInset = 8.dp
private val StackLogoMiddleBorder = 2.dp
private val StackLogoCoreInset = 20.dp
private val HeroTaglineMaxWidth = 280.dp
private val TrustBadgesMaxWidth = 320.dp
private val TrustBadgeIconSize = 16.dp

// Bottom action block.
private val ButtonHeight = 52.dp
private val GoogleGlyphSize = 20.dp
private val LoadingSpinnerSize = 18.dp
private val LoadingSpinnerStroke = 2.dp
private val ContinueAsAvatarSize = 44.dp
private val ContinueAsChevronSize = 18.dp

// Error banner.
private val ErrorDismissHitArea = 36.dp
private val ErrorDismissIconSize = 20.dp

// Semantic surface alphas. Tied to the mockup compositing recipe; the
// names describe the role on the screen, not the numeric value, so a
// future polish pass can re-tune them without scanning the file.
private const val BADGE_BACKGROUND_ALPHA = 0.6f
private const val LOADING_BUTTON_ALPHA = 0.6f
private const val ERROR_BANNER_SURFACE_ALPHA = 0.92f
private const val ERROR_BANNER_TINT_ALPHA = 0.10f
private const val RETURNING_CARD_BACKGROUND_ALPHA = 0.10f
private const val RETURNING_CARD_BORDER_ALPHA = 0.5f

// Tracked letter-spacings (uppercase tracked text in the mockup).
private val LetterSpacingMeta = 1.5.sp
private val LetterSpacingTracked = 1.2.sp
private val LetterSpacingTight = 1.sp
private val LetterSpacingSubtitle = 3.sp
private val LetterSpacingHero = 7.sp
private val LetterSpacingLegal = 0.5.sp

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

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
