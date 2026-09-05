package com.cashfluent.app.data.league

import com.cashfluent.app.data.model.GameOutcome
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueBackend
import com.cashfluent.app.domain.league.Nickname
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.Week
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Whether the last word from the league got through. */
enum class Connection { UNKNOWN, ONLINE, OFFLINE }

/**
 * The record on the phone and the board on the server, kept in step.
 *
 * The phone is the source of truth for points: a game is scored and saved locally first,
 * then declared to the board. The server is the source of truth for the league: who sits
 * with whom this week, and how last week's board closed. Every step that needs the
 * network can fail, and failing leaves the record exactly as it was — so [settle] is
 * safe to call whenever a screen opens, and it is.
 */
class LeagueService(
    private val store: PlayerStore,
    private val backend: LeagueBackend,
    private val now: () -> Long = System::currentTimeMillis,
) {

    val player: Flow<Player> get() = store.player

    private val _connection = MutableStateFlow(Connection.UNKNOWN)
    val connection: StateFlow<Connection> = _connection

    /** Three screens open on a Monday morning must not close the same week three times. */
    private val settling = Mutex()

    /** The board you sit on this week, live and ranked. Empty until you have a seat. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val standings: Flow<List<Standing>> = store.player
        .map { Seat(it.id, it.leagueId, it.tier) }
        .distinctUntilChanged()
        .flatMapLatest { seat ->
            val leagueId = seat.leagueId ?: return@flatMapLatest flowOf(emptyList())
            backend.watch(leagueId)
                .map { League.standings(it, seat.id, seat.tier) }
                .catch { _connection.value = Connection.OFFLINE }
        }

    private data class Seat(val id: String, val leagueId: String?, val tier: Tier)

    /**
     * Brings the record up to date with the calendar and the league, in order: close a
     * finished week, sign in if this phone never has, read the verdict on the week that
     * ended, take this week's seat, and say where you stand. Stops at the first step the
     * network refuses and picks up from there next time.
     */
    suspend fun settle(): Player = settling.withLock {
        val week = Week.index(now())
        var player = store.update { it.rollOver(week) }

        if (!player.signedIn) {
            val id = attempt { backend.signIn() } ?: return player
            player = store.update { it.copy(id = id) }
        }

        player.unsettled?.let { due ->
            val board = attempt { backend.board(due.leagueId) } ?: return player
            val outcome = League.outcome(board, player.id, due.week, player.tier)
            player = store.update { it.copy(tier = outcome.to, lastOutcome = outcome, unsettled = null) }
        }

        if (!player.seated) {
            val leagueId = attempt { backend.takeSeat(week, player.tier, player.entrant) } ?: return player
            player = store.update { it.copy(leagueId = leagueId) }
        }
        // One place writes rows, and it is this one — including the first row on a board that
        // was opened a moment ago, which is why taking a seat does not write it.
        publish(player)
        player
    }

    /** Adds a finished game to the record, declares it to the board, and says what changed. */
    suspend fun recordGame(gameId: String, score: Int): GameOutcome {
        val week = Week.index(now())
        var previousBest = 0
        val player = store.update { before ->
            val current = before.rollOver(week)
            previousBest = current.bestFor(gameId)
            current.scored(gameId, score)
        }
        publish(player)
        return GameOutcome(
            score = score,
            best = player.bestFor(gameId),
            newBest = score > previousBest,
            weekPoints = player.weekPoints,
            totalPoints = player.totalPoints,
        )
    }

    suspend fun rename(name: String) {
        publish(store.update { it.copy(name = Nickname.clean(name)) })
    }

    /** Last week's verdict has been read; stop showing it. */
    suspend fun dismissOutcome() {
        store.update { it.copy(lastOutcome = null) }
    }

    /** Points, bests and the rung go, and so does this week's seat. The id and the name stay. */
    suspend fun reset() {
        val before = store.player.first()
        if (before.signedIn) before.leagueId?.let { backend.leave(it, before.id) }
        store.update {
            it.copy(
                totalPoints = 0,
                weekPoints = 0,
                gamesPlayed = 0,
                tier = Tier.FIRST,
                lastOutcome = null,
                bests = emptyMap(),
                leagueId = null,
                unsettled = null,
            )
        }
    }

    private fun publish(player: Player) {
        val leagueId = player.leagueId ?: return
        if (player.signedIn) backend.publish(leagueId, player.entrant)
    }

    /** Runs one step against the server; null, and a note of it, if the server could not be reached. */
    private suspend inline fun <T> attempt(block: () -> T): T? = try {
        block().also { _connection.value = Connection.ONLINE }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _connection.value = Connection.OFFLINE
        null
    }
}
