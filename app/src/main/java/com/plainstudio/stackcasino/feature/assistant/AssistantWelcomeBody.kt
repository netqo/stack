package com.plainstudio.stackcasino.feature.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Welcome state shown until the user sends the first message: Nep
 * hero card, intro copy, the four canned suggestions, and the
 * memory-only footer note.
 *
 * Suggestions live as a const list so the wording matches the mockup
 * one-for-one and so unit tests can pin them.
 */
@Composable
internal fun AssistantWelcomeBody(onSuggestion: (String) -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenHorizontalPadding)
                .padding(top = WelcomeTopPadding, bottom = WelcomeBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NepAvatar(size = HeroAvatarSize)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Hey hey! I'm Nep~",
            color = TextHigh,
            fontSize = HeroTitleFontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ask me anything about game rules, payouts, or how Provably Fair works!",
            color = TextMedium,
            fontSize = HeroBodyFontSize,
            lineHeight = HeroBodyLineHeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = HeroBodyMaxWidth),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "TRY ASKING ME~",
            color = TextLow,
            fontSize = SectionLabelFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(12.dp))
        WELCOME_SUGGESTIONS.forEach { suggestion ->
            SuggestionRow(suggestion = suggestion, onClick = { onSuggestion(suggestion.prompt) })
            Spacer(modifier = Modifier.height(SuggestionGap))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CONVERSATION IS KEPT IN MEMORY · DISCARDED ON CLOSE",
            color = TextLow,
            fontSize = SectionLabelFontSize,
            letterSpacing = TrackedLetterSpacing,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SuggestionRow(
    suggestion: NepSuggestion,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick)
                .padding(SuggestionPadding),
    ) {
        Column {
            Text(
                text = suggestion.prompt,
                color = TextHigh,
                fontSize = SuggestionPromptFontSize,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = suggestion.tag.uppercase(),
                color = TextLow,
                fontSize = SectionLabelFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
    }
}

private data class NepSuggestion(
    val prompt: String,
    val tag: String,
)

private val WELCOME_SUGGESTIONS =
    listOf(
        NepSuggestion(prompt = "How does Blackjack splitting work?", tag = "Rules · Blackjack"),
        NepSuggestion(prompt = "What are the Roulette odds?", tag = "Odds · Roulette"),
        NepSuggestion(prompt = "Explain the Mines multiplier table", tag = "Payouts · Mines"),
        NepSuggestion(prompt = "How do I verify a round?", tag = "Verification · All games"),
    )

private val WelcomeTopPadding = 32.dp
private val WelcomeBottomPadding = 24.dp
private val HeroAvatarSize = 128.dp
private val HeroTitleFontSize = 18.sp
private val HeroBodyFontSize = 13.sp
private val HeroBodyLineHeight = 20.sp
private val HeroBodyMaxWidth = 280.dp
private val SectionLabelFontSize = 9.sp

private val SuggestionGap = 8.dp
private val SuggestionPadding = 14.dp
private val SuggestionPromptFontSize = 14.sp
