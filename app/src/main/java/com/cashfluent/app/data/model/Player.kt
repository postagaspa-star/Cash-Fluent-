package com.cashfluent.app.data.model

import com.cashfluent.app.domain.league.Entrant
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.WeekOutcome

/**
 * Everything the games and the league know about the person holding the phone. There is
 * still no account behind it: [id] is the anonymous id the server hands the phone on
 * first contact, and [name] is whatever nickname they typed. All of this lives on the
 * phone; the league sees [entrant] and nothing more.
 */
data class Player(
    val id: String = "",
    val name: String = "",
    val totalPoints: Int = 0,
    /** The week index the points below belong to. */
    val week: Int = 0,
    val weekPoints: Int = 0,
    val gamesPlayed: Int = 0,
    /** The rung of the ladder you are on this week. */
    val tier: Tier = Tier.FIRST,
    /** How last week ended, kept until it has been shown once. */
    val lastOutcome: WeekOutcome? = null,
    /** Best score per mini-game, by game id, out of [com.cashfluent.app.domain.game.GameRules.MAX_SCORE]. */
    val bests: Map<String, Int> = emptyMap(),
    /** The league you sit in this week, once a seat has been taken; null until then. */
    val leagueId: String? = null,
    /** A week that ended before its verdict on you could be read, because its board was out of reach. */
    val unsettled: Unsettled? = null,
) {
    val hasName: Boolean get() = name.isNotBlank()

    val signedIn: Boolean get() = id.isNotBlank()

    val seated: Boolean get() = leagueId != null

    fun bestFor(gameId: String): Int = bests[gameId] ?: 0

    val gamesWithBest: Int get() = bests.count { it.value > 0 }

    /** You, as the board sees you. */
    val entrant: Entrant get() = Entrant(id, name, totalPoints, weekPoints)

    /**
     * Closes the stored week if a newer one has begun: the points go back to zero and the
     * seat is given up. The verdict on the week that ended is owed, not decided — only its
     * board knows where you finished, and that is read when the board can be reached.
     */
    fun rollOver(currentWeek: Int): Player {
        if (week >= currentWeek) return this
        return copy(
            week = currentWeek,
            weekPoints = 0,
            leagueId = null,
            unsettled = leagueId?.let { Unsettled(it, week) } ?: unsettled,
        )
    }

    /** A finished game, added to the record. */
    fun scored(gameId: String, score: Int): Player = copy(
        totalPoints = totalPoints + score,
        weekPoints = weekPoints + score,
        gamesPlayed = gamesPlayed + 1,
        bests = bests + (gameId to maxOf(bestFor(gameId), score)),
    )
}

/** A league whose week has ended before its verdict on you could be read. */
data class Unsettled(val leagueId: String, val week: Int)

/** What a finished game did to the record. */
data class GameOutcome(
    val score: Int,
    val best: Int,
    val newBest: Boolean,
    val weekPoints: Int,
    val totalPoints: Int,
)
