package com.cashfluent.app.domain.league

import kotlinx.coroutines.flow.Flow

/**
 * Everything the league asks of a server, and nothing else. Firestore implements it in
 * the app; a fake implements it in the tests, which is how the rules of the week are
 * verified without a network. Nothing but an [Entrant] ever crosses this boundary.
 */
interface LeagueBackend {

    /** Signs this phone in silently and returns the id the server knows it by. */
    suspend fun signIn(): String

    /** The board of one league as it stands. A finished week's board no longer changes. */
    suspend fun board(leagueId: String): List<Entrant>

    /** The same board, kept current for as long as it is watched. */
    fun watch(leagueId: String): Flow<List<Entrant>>

    /**
     * Seats [me] in a league on [tier] for [week] — the one still filling up, or a new
     * one when that is full — and returns its id. Needs the network.
     */
    suspend fun takeSeat(week: Int, tier: Tier, me: Entrant): String

    /** Writes [me] on the board of [leagueId]. Best effort: queued if offline, never waited for. */
    fun publish(leagueId: String, me: Entrant)

    /** Takes your row off a board you are giving up. Best effort, like [publish]. */
    fun leave(leagueId: String, id: String)
}
