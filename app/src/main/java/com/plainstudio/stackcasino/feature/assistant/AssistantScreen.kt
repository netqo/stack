package com.plainstudio.stackcasino.feature.assistant

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.ui.components.PulsingDot
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow

/**
 * Nep assistant chat reproducing the cu-14 mockup
 * (mockup/js/screens/assistant.js). Owns:
 *
 *   * a top header (back button, Nep avatar, "your casino guide~"
 *     subtitle with the pulsing dot, clear-conversation button),
 *   * the body that dispatches on [AssistantUiState] -> welcome /
 *     conversation,
 *   * the input bar pinned to the bottom (Modifier.imePadding so it
 *     rides the soft keyboard).
 *
 * The system-prompt-active indicator + char counter live in the
 * footer of the input bar. The send button is disabled while a draft
 * is empty, over the 300-char budget, or while Nep is mid-reply.
 */
@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AssistantContent(
        state = state,
        onBack = onBack,
        onClear = viewModel::clear,
        onSend = viewModel::sendMessage,
    )
}

@Composable
private fun AssistantContent(
    state: AssistantUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onSend: (String) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val isTyping = (state as? AssistantUiState.Conversation)?.isNepTyping == true

    Surface(modifier = Modifier.fillMaxSize(), color = SurfaceBase) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            AssistantHeader(onBack = onBack, onClear = onClear)
            Box(modifier = Modifier.weight(1f)) {
                when (state) {
                    AssistantUiState.Welcome ->
                        AssistantWelcomeBody(onSuggestion = { suggestion -> onSend(suggestion) })
                    is AssistantUiState.Conversation ->
                        AssistantConversationBody(
                            messages = state.messages,
                            isNepTyping = state.isNepTyping,
                        )
                }
            }
            AssistantInputBar(
                draft = draft,
                onDraftChange = { draft = it.take(AssistantViewModel.MAX_USER_MESSAGE_LENGTH) },
                isSendEnabled = draft.isNotBlank() && !isTyping,
                onSend = {
                    onSend(draft)
                    draft = ""
                },
            )
        }
    }
}

@Composable
private fun AssistantHeader(
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceBase)
                .padding(horizontal = HeaderHorizontalPadding, vertical = HeaderVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconChip(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            onClick = onBack,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NepAvatar(size = HeaderAvatarSize)
                Text(
                    text = "Nep",
                    color = TextHigh,
                    fontSize = HeaderTitleFontSize,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PulsingDot()
                Text(
                    text = "YOUR CASINO GUIDE~",
                    color = SemanticOk,
                    fontSize = SubtitleFontSize,
                    letterSpacing = TrackedLetterSpacing,
                )
            }
        }
        IconChip(
            icon = Icons.Outlined.Delete,
            contentDescription = "Clear conversation",
            onClick = onClear,
        )
    }
    Divider()
}

@Composable
private fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(IconChipSize)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextHigh,
            modifier = Modifier.size(IconChipIconSize),
        )
    }
}

@Composable
internal fun NepAvatar(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier =
            Modifier
                .size(size)
                .border(width = 1.dp, color = AccentViolet),
    ) {
        Image(
            painter = painterResource(R.drawable.nep_nerd),
            contentDescription = "Nep",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AssistantInputBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    isSendEnabled: Boolean,
    onSend: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceBase)
                .navigationBarsPadding()
                .padding(InputBarOuterPadding),
    ) {
        Divider()
        Spacer(modifier = Modifier.height(InputBarTopGap))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(InputBarChildGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            DraftField(value = draft, onValueChange = onDraftChange, onSend = onSend, modifier = Modifier.weight(1f))
            SendButton(enabled = isSendEnabled, onClick = onSend)
        }
        Spacer(modifier = Modifier.height(8.dp))
        InputBarFooter(charCount = draft.length)
    }
}

@Composable
private fun DraftField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            maxLines = DRAFT_MAX_LINES,
            textStyle = TextStyle(color = TextHigh, fontSize = DraftFontSize, lineHeight = DraftLineHeight),
            cursorBrush = SolidColor(AccentViolet),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send,
                ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier.fillMaxWidth(),
        )
        if (value.isEmpty()) {
            Text(
                text = "Ask Nep about game rules, odds, payouts...",
                color = TextLow,
                fontSize = DraftFontSize,
                lineHeight = DraftLineHeight,
            )
        }
    }
}

@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background = if (enabled) AccentViolet else AccentViolet.copy(alpha = DISABLED_SEND_ALPHA)
    val tint = if (enabled) Color.White else Color.White.copy(alpha = DISABLED_SEND_ALPHA)
    Box(
        modifier =
            Modifier
                .size(SendButtonSize)
                .background(background)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = "Send message",
            tint = tint,
            modifier = Modifier.size(SendIconSize),
        )
    }
}

@Composable
private fun InputBarFooter(charCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(SystemDotSize).background(AccentViolet))
            Text(
                text = "SYSTEM PROMPT ACTIVE",
                color = TextLow,
                fontSize = FooterFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
        Text(
            text = "$charCount / ${AssistantViewModel.MAX_USER_MESSAGE_LENGTH}",
            color = TextLow,
            fontSize = FooterFontSize,
            letterSpacing = TrackedLetterSpacing,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
internal fun Divider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceOutline),
    )
}

// ---------------------------------------------------------------------------
// Tokens shared across the assistant files.
// ---------------------------------------------------------------------------

internal val ScreenHorizontalPadding = 16.dp
internal val TrackedLetterSpacing = 1.2.sp
internal val ConversationGap = 12.dp
internal val NepBubbleAuthorFontSize = 10.sp
internal val MessageBodyFontSize = 13.sp
internal val MessageBodyLineHeight = 20.sp
internal val MessageMetaFontSize = 9.sp

private val HeaderHorizontalPadding = 16.dp
private val HeaderVerticalPadding = 12.dp
private val HeaderAvatarSize = 28.dp
private val HeaderTitleFontSize = 16.sp
private val SubtitleFontSize = 9.sp
private val IconChipSize = 36.dp
private val IconChipIconSize = 16.dp

private val InputBarOuterPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
private val InputBarTopGap = 12.dp
private val InputBarChildGap = 8.dp
private val DraftFontSize = 14.sp
private val DraftLineHeight = 20.sp
private const val DRAFT_MAX_LINES = 5
private val SendButtonSize = 48.dp
private val SendIconSize = 20.dp
private const val DISABLED_SEND_ALPHA = 0.40f
private val SystemDotSize = 4.dp
private val FooterFontSize = 9.sp

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun AssistantContentWelcomePreview() {
    StackcasinoTheme {
        AssistantContent(
            state = AssistantUiState.Welcome,
            onBack = {},
            onClear = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun AssistantContentConversationPreview() {
    StackcasinoTheme {
        AssistantContent(
            state =
                AssistantUiState.Conversation(
                    messages =
                        listOf(
                            ChatMessage(
                                id = "preview-user-1",
                                author = Author.User,
                                body = "How does Blackjack splitting work?",
                                timestampLabel = "2:14 PM",
                            ),
                            ChatMessage(
                                id = "preview-nep-1",
                                author = Author.Nep,
                                body =
                                    "Hey hey! Splitting kicks in when your first two cards " +
                                        "share a rank~ Tap the split button and Nep will deal a " +
                                        "second card to each hand.\n\n" +
                                        "- You pay another bet equal to the original.\n" +
                                        "- Aces only get one card.\n" +
                                        "- You can't re-split the same hand.",
                                timestampLabel = "2:14 PM",
                            ),
                        ),
                    isNepTyping = true,
                ),
            onBack = {},
            onClear = {},
            onSend = {},
        )
    }
}
