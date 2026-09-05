package com.cashfluent.app.data.model

import com.cashfluent.app.domain.game.Medal
import com.cashfluent.app.domain.league.LeagueCard

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
    /** Best game score per lesson, out of [com.cashfluent.app.domain.game.GameRules.MAX_SCORE]. */
    val bests: Map<String, Int> = emptyMap(),
    val friends: List<LeagueCard> = emptyList(),
) {
    val hasName: Boolean get() = name.isNotBlank()

    fun bestFor(moduleId: String): Int = bests[moduleId] ?: 0

    fun medalFor(moduleId: String): Medal = Medal.forScore(bestFor(moduleId))

    fun medalCount(moduleIds: List<String>): Int = moduleIds.count { medalFor(it) != Medal.NONE }

    /** The card that stands for you on every board, medals in lesson order. */
    fun card(moduleIds: List<String>): LeagueCard =
        LeagueCard(id, name, totalPoints, week, weekPoints, moduleIds.map(::medalFor))
}

/** What a finished game did to the record. */
data class GameOutcome(
    val score: Int,
    val best: Int,
    val newBest: Boolean,
    val medal: Medal,
    val medalBefore: Medal,
) {
    val newMedal: Boolean get() = medal > medalBefore
}
