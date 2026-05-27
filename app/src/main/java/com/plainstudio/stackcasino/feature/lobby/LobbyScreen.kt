package com.plainstudio.stackcasino.feature.lobby

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plainstudio.stackcasino.R
import com.plainstudio.stackcasino.navigation.Route
import com.plainstudio.stackcasino.ui.components.BalancePill
import com.plainstudio.stackcasino.ui.components.CurrencyDropdown
import com.plainstudio.stackcasino.ui.components.ErrorState
import com.plainstudio.stackcasino.ui.components.ErrorStateDefaults
import com.plainstudio.stackcasino.ui.components.Skeleton
import com.plainstudio.stackcasino.ui.components.StackCard
import com.plainstudio.stackcasino.ui.components.gridBackground
import com.plainstudio.stackcasino.ui.theme.AccentViolet
import com.plainstudio.stackcasino.ui.theme.SemanticDanger
import com.plainstudio.stackcasino.ui.theme.SemanticOk
import com.plainstudio.stackcasino.ui.theme.SemanticWarn
import com.plainstudio.stackcasino.ui.theme.StackcasinoTheme
import com.plainstudio.stackcasino.ui.theme.SurfaceBase
import com.plainstudio.stackcasino.ui.theme.SurfaceOutline
import com.plainstudio.stackcasino.ui.theme.SurfaceRaised
import com.plainstudio.stackcasino.ui.theme.TextHigh
import com.plainstudio.stackcasino.ui.theme.TextLow
import com.plainstudio.stackcasino.ui.theme.TextMedium

/**
 * Lobby screen reproducing the cu-03 mockup
 * (mockup/js/screens/lobby.js, all three states).
 *
 * State branching is rendered by [LobbyContent]:
 *   * [LobbyUiState.Success] -> header + balance + games + actions + recent + FAB.
 *   * [LobbyUiState.Loading] -> the skeleton placeholders only.
 *   * [LobbyUiState.Error]   -> centered ErrorState card with retry / use-cache.
 *
 * The screen takes a single [onNavigate] callback so the caller (nav
 * host) owns the routing decisions. Game-card taps map to game routes
 * inside the screen because the mapping is one-to-one and stable. The
 * eye-toggle for balance visibility lives as local UI-only state
 * (it is purely presentational and survives configuration changes via
 * rememberSaveable).
 */
@Composable
fun LobbyScreen(
    state: LobbyUiState,
    onNavigate: (Route) -> Unit,
    onRetry: () -> Unit,
    onUseCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LobbyContent(
        state = state,
        onNavigate = onNavigate,
        onRetry = onRetry,
        onUseCache = onUseCache,
        modifier = modifier,
    )
}

@Composable
private fun LobbyContent(
    state: LobbyUiState,
    onNavigate: (Route) -> Unit,
    onRetry: () -> Unit,
    onUseCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = SurfaceBase) {
        Box(modifier = Modifier.fillMaxSize().gridBackground()) {
            when (state) {
                is LobbyUiState.Success ->
                    SuccessContent(
                        data = state.data,
                        onNavigate = onNavigate,
                    )
                LobbyUiState.Loading -> LoadingContent()
                is LobbyUiState.Error ->
                    ErrorContent(
                        state = state,
                        onRetry = onRetry,
                        onUseCache = onUseCache,
                    )
            }
            if (state is LobbyUiState.Success) {
                NepFab(
                    onClick = { onNavigate(Route.Assistant) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(NepFabPadding),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Success state
// ---------------------------------------------------------------------------

@Composable
private fun SuccessContent(
    data: LobbyData,
    onNavigate: (Route) -> Unit,
) {
    var isBalanceHidden by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        LobbyHeader(
            user = data.user,
            onOpenNotifications = {},
        )
        Divider()
        BalanceHero(
            balance = data.balance,
            session = data.session,
            isHidden = isBalanceHidden,
            onToggleHidden = { isBalanceHidden = !isBalanceHidden },
        )
        Divider()
        GamesSection(
            games = data.games,
            onSelectGame = { onNavigate(it.toRoute()) },
        )
        Divider()
        QuickActionsSection(onOpenWallet = { onNavigate(Route.Wallet) })
        Divider()
        RecentActivitySection(
            rounds = data.recentActivity,
            onViewAll = { onNavigate(Route.History) },
        )
        Spacer(modifier = Modifier.height(BottomScrollPadding))
    }
}

@Composable
private fun LobbyHeader(
    user: UserSummary,
    onOpenNotifications: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenHorizontalPadding,
                    end = ScreenHorizontalPadding,
                    top = HeaderTopPadding,
                    bottom = HeaderBottomPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AvatarTile(displayName = user.displayName)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.greeting.uppercase(),
                color = TextMedium,
                fontSize = MetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.displayName,
                color = TextHigh,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        NotificationsButton(
            hasUnread = user.hasUnreadNotifications,
            onClick = onOpenNotifications,
        )
    }
}

@Composable
private fun AvatarTile(displayName: String) {
    Box(
        modifier = Modifier.size(AvatarSize).background(AccentViolet),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(displayName),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NotificationsButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(NotificationsButtonSize)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = TextHigh,
            modifier = Modifier.size(NotificationsIconSize),
        )
        if (hasUnread) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(NotificationsBadgeInset)
                        .size(NotificationsBadgeSize)
                        .background(SemanticDanger),
            )
        }
    }
}

@Composable
private fun BalanceHero(
    balance: BalanceSummary,
    session: SessionStats,
    isHidden: Boolean,
    onToggleHidden: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Top meta row: AVAILABLE label + PnL chip (chip fades when
            // balance is hidden but keeps its slot to avoid layout shift).
            BalanceMetaRow(
                pnLLabel = balance.todayPnLLabel,
                isHidden = isHidden,
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Session stats live on their own row below the chip
            // (mockup line 52: tracked text-[9px] tabnum following the
            // AVAILABLE/chip line via flex-wrap).
            Text(
                text = "· ${session.rounds} rounds · ${session.wins}W / ${session.losses}L".uppercase(),
                color = TextLow,
                fontSize = SessionStatsFontSize,
                letterSpacing = TrackedLetterSpacing,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
            Spacer(modifier = Modifier.height(4.dp))
            BalancePill(
                label = "Available",
                amount = balance.amountLabel,
                isHidden = isHidden,
                onToggleVisibility = onToggleHidden,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CurrencyDropdown(
                initialCurrency = balance.currencyCode,
                networkLabel = balance.networkLabel,
            )
        }
        LockedColumn(subtitle = balance.lockedSubtitle)
    }
}

@Composable
private fun BalanceMetaRow(
    pnLLabel: String?,
    isHidden: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "AVAILABLE",
            color = TextMedium,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        // The PnL chip fades to alpha 0 when balance is hidden (mockup
        // line 46: data-bal-fade) so the row width never shifts.
        if (pnLLabel != null) {
            PnLChip(
                label = pnLLabel,
                modifier = Modifier.alpha(if (isHidden) 0f else 1f),
            )
        }
    }
}

@Composable
private fun PnLChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(SemanticOk.copy(alpha = PNL_CHIP_BACKGROUND_ALPHA))
                .border(width = 1.dp, color = SemanticOk.copy(alpha = PNL_CHIP_BORDER_ALPHA))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = SemanticOk,
            fontSize = PnlChipFontSize,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = TrackedLetterSpacing,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

@Composable
private fun LockedColumn(subtitle: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "LOCKED",
            color = TextMedium,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle.uppercase(),
            color = TextLow,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

// ---------------------------------------------------------------------------
// Games section
// ---------------------------------------------------------------------------

@Composable
private fun GamesSection(
    games: List<GameCardData>,
    onSelectGame: (GameKey) -> Unit,
) {
    val gridGames = games.filter { it.key != GameKey.Coinflip }
    val coinflip = games.firstOrNull { it.key == GameKey.Coinflip }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
    ) {
        SectionTitleRow(title = "Games", trailing = "${games.size} available")
        Spacer(modifier = Modifier.height(12.dp))
        gridGames.chunked(GAMES_PER_ROW).forEach { rowGames ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GameCardGap),
            ) {
                rowGames.forEach { card ->
                    GameCard(
                        card = card,
                        onClick = { onSelectGame(card.key) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill the remaining cell when the last row is uneven.
                repeat(GAMES_PER_ROW - rowGames.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(GameCardGap))
        }
        if (coinflip != null) {
            CoinflipCard(card = coinflip, onClick = { onSelectGame(coinflip.key) })
        }
    }
}

@Composable
private fun GameCard(
    card: GameCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (card.isLastPlayed) AccentViolet.copy(alpha = LAST_PLAYED_BACKGROUND_ALPHA) else SurfaceRaised
    val borderColor = if (card.isLastPlayed) AccentViolet.copy(alpha = LAST_PLAYED_BORDER_ALPHA) else SurfaceOutline
    // Outer box owns the border + clickable so the LAST PLAYED badge
    // can sit at an inset smaller than the inner content padding
    // (mockup: badge at top-2 left-2 vs content at p-4).
    Box(
        modifier =
            modifier
                .background(background)
                .border(width = 1.dp, color = borderColor)
                .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(GameCardPadding)) {
            if (card.isLastPlayed) {
                // Push the icon below the badge. Compensated by a smaller
                // icon-to-title gap (see LastPlayedTitleGap below) so the
                // Crash card stays the same total height as its siblings
                // and the grid row does not stretch unevenly.
                Spacer(modifier = Modifier.height(LastPlayedIconTopGap))
            }
            Icon(
                painter = painterResource(card.key.iconRes()),
                contentDescription = null,
                tint = AccentViolet,
                modifier = Modifier.size(GameCardIconSize),
            )
            Spacer(
                modifier =
                    Modifier.height(if (card.isLastPlayed) LastPlayedTitleGap else GameCardTitleGap),
            )
            Text(
                text = card.title,
                color = TextHigh,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            // alignByBaseline keeps the two labels typographically level
            // even though the right side uses `tnum` digits that report
            // slightly different line metrics than the plain-letter left.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = card.subtitle.uppercase(),
                    color = TextLow,
                    fontSize = SmallMetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                    modifier = Modifier.alignByBaseline(),
                )
                Text(
                    text = card.infoRight.uppercase(),
                    color = TextLow,
                    fontSize = SmallMetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
        if (card.isLastPlayed) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(LastPlayedBadgeInset)
                        .background(AccentViolet)
                        .padding(
                            horizontal = LastPlayedBadgeHorizontalPadding,
                            vertical = LastPlayedBadgeVerticalPadding,
                        ),
            ) {
                Text(
                    text = "LAST PLAYED",
                    color = Color.White,
                    fontSize = LastPlayedBadgeFontSize,
                    lineHeight = LastPlayedBadgeFontSize,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = LastPlayedBadgeLetterSpacing,
                )
            }
        }
    }
}

@Composable
private fun CoinflipCard(
    card: GameCardData,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick)
                .padding(GameCardPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(CoinflipBadgeSize)
                        .border(width = 1.dp, color = AccentViolet),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "x2",
                    color = AccentViolet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    color = TextHigh,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.subtitle.uppercase(),
                    color = TextLow,
                    fontSize = SmallMetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                )
            }
            // Aligns with the subtitle line of the left column instead of
            // the row centre so the meta label sits on the same baseline
            // as X2 PAYOUT (mockup line 173 leaves both labels at the
            // foot of the card).
            Text(
                text = card.infoRight.uppercase(),
                color = TextLow,
                fontSize = MetaFontSize,
                letterSpacing = TrackedLetterSpacing,
                modifier = Modifier.align(Alignment.Bottom),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick actions
// ---------------------------------------------------------------------------

@Composable
private fun QuickActionsSection(onOpenWallet: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
    ) {
        Text(
            text = "QUICK ACTIONS",
            color = TextMedium,
            fontSize = SectionTitleFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GameCardGap),
        ) {
            QuickActionCard(
                title = "Deposit",
                subtitle = "Receive crypto",
                accent = SemanticOk,
                isDeposit = true,
                onClick = onOpenWallet,
                modifier = Modifier.weight(1f),
            )
            QuickActionCard(
                title = "Withdraw",
                subtitle = "Send to address",
                accent = SemanticWarn,
                isDeposit = false,
                onClick = onOpenWallet,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    accent: Color,
    isDeposit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(SurfaceRaised)
                .border(width = 1.dp, color = SurfaceOutline)
                .clickable(onClick = onClick)
                .padding(GameCardPadding),
    ) {
        Column {
            // Mockup deposits land downward (incoming) and withdrawals
            // leave upward (outgoing); see mockup/js/screens/lobby.js
            // lines 186 and 191.
            Icon(
                imageVector =
                    if (isDeposit) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(QuickActionIconSize),
            )
            Spacer(modifier = Modifier.height(GameCardTitleGap))
            Text(
                text = title,
                color = TextHigh,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle.uppercase(),
                color = TextLow,
                fontSize = SmallMetaFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Recent activity
// ---------------------------------------------------------------------------

@Composable
private fun RecentActivitySection(
    rounds: List<RecentRound>,
    onViewAll: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "RECENT",
                color = TextMedium,
                fontSize = SectionTitleFontSize,
                letterSpacing = TrackedLetterSpacing,
            )
            Row(
                modifier = Modifier.clickable(onClick = onViewAll),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "VIEW ALL",
                    color = TextLow,
                    fontSize = MetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = TextLow,
                    modifier = Modifier.size(ViewAllChevronSize),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(RecentRowGap)) {
            rounds.forEach { round ->
                RecentRoundRow(round = round)
            }
        }
    }
}

@Composable
private fun RecentRoundRow(round: RecentRound) {
    val accent = if (round.outcome == RoundOutcome.Win) SemanticOk else SemanticDanger
    val amountColor = if (round.outcome == RoundOutcome.Win) SemanticOk else TextLow
    StackCard(
        modifier = Modifier.fillMaxWidth(),
        leftAccent = accent,
        contentPadding = PaddingValues(RecentRowPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(RecentRowIconBoxSize)
                        .border(width = 1.dp, color = SurfaceOutline),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(round.game.iconRes()),
                    contentDescription = null,
                    tint = AccentViolet,
                    modifier = Modifier.size(RecentRowIconSize),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = round.gameLabel,
                    color = TextHigh,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${round.agoLabel} · ${round.resultLabel}".uppercase(),
                    color = TextLow,
                    fontSize = SmallMetaFontSize,
                    letterSpacing = TrackedLetterSpacing,
                    style = TextStyle(fontFeatureSettings = "tnum"),
                )
            }
            Text(
                text = round.amountLabel,
                color = amountColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(fontFeatureSettings = "tnum"),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Loading state
// ---------------------------------------------------------------------------

@Composable
private fun LoadingContent() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Balance skeleton.
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Skeleton(modifier = Modifier.size(width = 96.dp, height = 8.dp))
                Skeleton(modifier = Modifier.size(width = 224.dp, height = 40.dp))
                Skeleton(modifier = Modifier.size(width = 112.dp, height = 8.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                Skeleton(modifier = Modifier.size(width = 64.dp, height = 8.dp))
                Skeleton(modifier = Modifier.size(width = 80.dp, height = 8.dp))
            }
        }
        Divider()
        // Games skeleton.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenHorizontalPadding, vertical = SectionVerticalPadding),
        ) {
            Skeleton(modifier = Modifier.size(width = 64.dp, height = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GameCardGap),
                ) {
                    Skeleton(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(GameCardSkeletonHeight),
                    )
                    Skeleton(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(GameCardSkeletonHeight),
                    )
                }
                Spacer(modifier = Modifier.height(GameCardGap))
            }
            Skeleton(modifier = Modifier.fillMaxWidth().height(CoinflipSkeletonHeight))
        }
        Divider()
        // Recent skeleton.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ScreenHorizontalPadding,
                        end = ScreenHorizontalPadding,
                        top = SectionVerticalPadding,
                        bottom = BottomScrollPadding,
                    ),
        ) {
            Skeleton(modifier = Modifier.size(width = 64.dp, height = 8.dp))
            Spacer(modifier = Modifier.height(12.dp))
            repeat(RECENT_SKELETON_COUNT) {
                Skeleton(modifier = Modifier.fillMaxWidth().height(RecentSkeletonHeight))
                if (it < RECENT_SKELETON_COUNT - 1) {
                    Spacer(modifier = Modifier.height(RecentRowGap))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Error state
// ---------------------------------------------------------------------------

@Composable
private fun ErrorContent(
    state: LobbyUiState.Error,
    onRetry: () -> Unit,
    onUseCache: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        ErrorState(
            icon = { ErrorStateDefaults.OfflineIcon() },
            title = "Connection Lost",
            message = state.message,
            primaryActionLabel = "Retry",
            onPrimaryAction = onRetry,
            secondaryActionLabel = "Use cache",
            onSecondaryAction = onUseCache,
            footer = state.lastSyncedLabel,
        )
    }
}

// ---------------------------------------------------------------------------
// Shared building blocks
// ---------------------------------------------------------------------------

@Composable
private fun Divider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SurfaceOutline),
    )
}

@Composable
private fun SectionTitleRow(
    title: String,
    trailing: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            color = TextMedium,
            fontSize = SectionTitleFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
        Text(
            text = trailing.uppercase(),
            color = TextLow,
            fontSize = MetaFontSize,
            letterSpacing = TrackedLetterSpacing,
        )
    }
}

@Composable
private fun NepFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(NepFabSize)
                .border(width = NepFabBorderWidth, color = AccentViolet)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.nep_nerd),
            contentDescription = "Ask Nep",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun GameKey.toRoute(): Route =
    when (this) {
        GameKey.Roulette -> Route.Roulette
        GameKey.Blackjack -> Route.Blackjack
        GameKey.Crash -> Route.Crash
        GameKey.Mines -> Route.Mines
        GameKey.Coinflip -> Route.Coinflip
    }

private fun GameKey.iconRes(): Int =
    when (this) {
        GameKey.Roulette -> R.drawable.ic_game_roulette
        GameKey.Blackjack -> R.drawable.ic_game_blackjack
        GameKey.Crash -> R.drawable.ic_game_crash
        GameKey.Mines -> R.drawable.ic_game_mines
        GameKey.Coinflip -> R.drawable.ic_game_coinflip
    }

private fun initialsOf(name: String): String {
    val tokens = name.split(' ').filter { it.isNotBlank() }
    return when (tokens.size) {
        0 -> ""
        1 -> tokens[0].first().uppercase()
        else -> "${tokens[0].first().uppercase()}${tokens[1].first().uppercase()}"
    }
}

// ---------------------------------------------------------------------------
// Tokens
// ---------------------------------------------------------------------------

private val ScreenHorizontalPadding = 16.dp
private val SectionVerticalPadding = 20.dp
private val HeaderTopPadding = 24.dp
private val HeaderBottomPadding = 16.dp
private val BottomScrollPadding = 96.dp

private val AvatarSize = 40.dp
private val NotificationsButtonSize = 40.dp
private val NotificationsIconSize = 18.dp
private val NotificationsBadgeSize = 8.dp
private val NotificationsBadgeInset = 6.dp

private val GameCardPadding = 16.dp
private val GameCardGap = 8.dp
private val GameCardIconSize = 28.dp
private val GameCardTitleGap = 32.dp
private val GameCardSkeletonHeight = 112.dp
private val LastPlayedIconTopGap = 16.dp
private val LastPlayedTitleGap = 16.dp
private val LastPlayedBadgeInset = 8.dp
private val CoinflipSkeletonHeight = 80.dp
private val CoinflipBadgeSize = 40.dp

private val QuickActionIconSize = 20.dp

private val RecentRowGap = 8.dp
private val RecentRowPadding = 12.dp
private val RecentRowIconBoxSize = 36.dp
private val RecentRowIconSize = 16.dp
private val RecentSkeletonHeight = 56.dp
private const val RECENT_SKELETON_COUNT = 3

private val ViewAllChevronSize = 12.dp

private val NepFabSize = 48.dp
private val NepFabBorderWidth = 2.dp
private val NepFabPadding = PaddingValues(end = 16.dp, bottom = 16.dp)

private const val GAMES_PER_ROW = 2

private val MetaFontSize = 10.sp
private val SmallMetaFontSize = 9.sp
private val SectionTitleFontSize = 11.sp
private val SessionStatsFontSize = 9.sp
private val PnlChipFontSize = 9.sp
private val LastPlayedBadgeFontSize = 7.sp
private val LastPlayedBadgeLetterSpacing = 0.6.sp
private val LastPlayedBadgeHorizontalPadding = 4.dp
private val LastPlayedBadgeVerticalPadding = 1.dp
private val TrackedLetterSpacing = 1.2.sp

private const val PNL_CHIP_BACKGROUND_ALPHA = 0.15f
private const val PNL_CHIP_BORDER_ALPHA = 0.40f
private const val LAST_PLAYED_BACKGROUND_ALPHA = 0.05f
private const val LAST_PLAYED_BORDER_ALPHA = 0.50f

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun LobbyScreenSuccessPreview() {
    StackcasinoTheme {
        LobbyScreen(
            state = LobbyUiState.Success(previewLobbyData()),
            onNavigate = {},
            onRetry = {},
            onUseCache = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun LobbyScreenLoadingPreview() {
    StackcasinoTheme {
        LobbyScreen(
            state = LobbyUiState.Loading,
            onNavigate = {},
            onRetry = {},
            onUseCache = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0B12, heightDp = 900)
@Composable
private fun LobbyScreenErrorPreview() {
    StackcasinoTheme {
        LobbyScreen(
            state =
                LobbyUiState.Error(
                    message = "Failed to sync wallet and rounds from Firestore. Showing last known state.",
                    lastSyncedLabel = "Last synced 4 minutes ago",
                ),
            onNavigate = {},
            onRetry = {},
            onUseCache = {},
        )
    }
}
