package com.plainstudio.stackcasino.feature.assistant

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow

/**
 * Conversation state: the scrollable message list (LazyColumn so long
 * histories don't allocate every bubble) plus the typing indicator
 * that appears below the last message while Nep is mid-reply.
 *
 * The list auto-scrolls to the bottom on every message append or
 * typing-state flip so the user always sees the freshest content
 * without having to scroll manually.
 */
@Composable
internal fun AssistantConversationBody(
    messages: List<ChatMessage>,
    isNepTyping: Boolean,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, isNepTyping) {
        val target = messages.size + if (isNepTyping) 1 else 0
        if (target > 0) listState.animateScrollToItem(target - 1)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = ConversationGap),
        verticalArrangement = Arrangement.spacedBy(ConversationGap),
    ) {
        items(items = messages, key = { it.id }) { message ->
            ChatBubble(message = message)
        }
        if (isNepTyping) {
            item { TypingIndicator() }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    if (message.author == Author.User) {
        UserBubble(message)
    } else {
        NepBubble(message)
    }
}

@Composable
private fun UserBubble(message: ChatMessage) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(
            modifier = Modifier.widthIn(max = BubbleMaxWidth),
            horizontalAlignment = Alignment.End,
        ) {
            Box(modifier = Modifier.background(AccentViolet).padding(BubblePadding)) {
                Text(
                    text = message.body,
                    color = Color.White,
                    fontSize = MessageBodyFontSize,
                    lineHeight = MessageBodyLineHeight,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestampLabel,
                color = TextLow,
                fontSize = MessageMetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
    }
}

@Composable
private fun NepBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NepAvatar(size = BubbleAvatarSize)
        Column(modifier = Modifier.widthIn(max = BubbleMaxWidth)) {
            Text(
                text = "Nep",
                color = if (message.isError) SemanticDanger else AccentViolet,
                fontSize = NepBubbleAuthorFontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val background = if (message.isError) SemanticDanger.copy(alpha = ERROR_BG_ALPHA) else SurfaceRaised
            val borderColor = if (message.isError) SemanticDanger.copy(alpha = ERROR_BORDER_ALPHA) else SurfaceOutline
            Box(
                modifier =
                    Modifier
                        .background(background)
                        .border(width = 1.dp, color = borderColor)
                        .padding(BubblePadding),
            ) {
                Text(
                    text = message.body,
                    color = TextHigh,
                    fontSize = MessageBodyFontSize,
                    lineHeight = MessageBodyLineHeight,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestampLabel,
                color = TextLow,
                fontSize = MessageMetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NepAvatar(size = BubbleAvatarSize)
        Column(modifier = Modifier.widthIn(max = BubbleMaxWidth)) {
            Text(
                text = "Nep",
                color = AccentViolet,
                fontSize = NepBubbleAuthorFontSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier =
                    Modifier
                        .background(SurfaceRaised)
                        .border(width = 1.dp, color = SurfaceOutline)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TypingDot(delayMillis = 0)
                TypingDot(delayMillis = TYPING_DOT_STAGGER_MILLIS)
                TypingDot(delayMillis = TYPING_DOT_STAGGER_MILLIS * 2)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "NEP IS TYPING...",
                    color = TextLow,
                    fontSize = MessageMetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                )
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    // Stagger three pulsing dots so the typing animation has the same
    // cadence as the mockup's `animate-pulse` row.
    val transition = rememberInfiniteTransition(label = "typing-dot")
    val alpha by transition.animateFloat(
        initialValue = TYPING_DOT_ALPHA_MAX,
        targetValue = TYPING_DOT_ALPHA_MIN,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = TYPING_DOT_DURATION_MILLIS,
                        delayMillis = delayMillis,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "typing-dot-alpha",
    )
    Box(
        modifier =
            Modifier
                .size(TypingDotSize)
                .alpha(alpha)
                .background(AccentViolet),
    )
}

private val BubbleMaxWidth = 280.dp
private val BubbleAvatarSize = 28.dp
private val BubblePadding = 12.dp

private val TypingDotSize = 6.dp
private const val TYPING_DOT_STAGGER_MILLIS = 200
private const val TYPING_DOT_DURATION_MILLIS = 600
private const val TYPING_DOT_ALPHA_MIN = 0.3f
private const val TYPING_DOT_ALPHA_MAX = 1f

private const val ERROR_BG_ALPHA = 0.05f
private const val ERROR_BORDER_ALPHA = 0.50f
