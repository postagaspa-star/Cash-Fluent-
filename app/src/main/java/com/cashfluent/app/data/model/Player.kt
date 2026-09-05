package com.cashfluent.app.data.model

import com.cashfluent.app.domain.league.LeagueCard
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.WeekOutcome

/**
 * Everything the games and the league know about the person holding the phone. There
 * is no account behind it: [id] is eight random hex characters minted on first use, and
 * [name] is whatever nickname they typed.
 */
data class Player(
    val id: String = "",
    val name: String = "",
    val totalPoints: Int = 0,
    /** The week index the points below belong to — always the current one once read. */
    val week: Int = 0,
    val weekPoints: Int = 0,
    val gamesPlayed: Int = 0,
    /** The rung of the ladder you are on this week. */
    val tier: Tier = Tier.FIRST,
    /** How last week ended, kept until it has been shown once. */
    val lastOutcome: WeekOutcome? = null,
    /** Best score per mini-game, by game id, out of [com.cashfluent.app.domain.game.GameRules.MAX_SCORE]. */
    val bests: Map<String, Int> = emptyMap(),
    val friends: List<LeagueCard> = emptyList(),
) {
    val hasName: Boolean get() = name.isNotBlank()

    fun bestFor(gameId: String): Int = bests[gameId] ?: 0

    val gamesWithBest: Int get() = bests.count { it.value > 0 }

    /** The card that stands for you on every board. */
    val card: LeagueCard get() = LeagueCard(id, name, totalPoints, week, weekPoints, tier)
}

/** What a finished game did to the record. */
data class GameOutcome(
    val score: Int,
    val best: Int,
    val newBest: Boolean,
    val weekPoints: Int,
    val totalPoints: Int,
)
