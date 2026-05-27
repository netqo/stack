package com.plainstudio.stackcasino.model

/**
 * Catalogue of playable games. Shared between any feature that needs
 * to reference a game without coupling to the others' state types
 * (lobby's recent activity, history's round filter, future game
 * launcher routes, etc).
 */
enum class GameKey {
    Roulette,
    Blackjack,
    Crash,
    Mines,
    Coinflip,
}

/**
 * Outcome of a single round. Push (tie) is not a real outcome in the
 * supported games, so the model stays binary; if a game adds Push
 * later the enum extends here and both lobby and history surface it
 * uniformly.
 */
enum class RoundOutcome { Win, Loss }
