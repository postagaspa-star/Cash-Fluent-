package com.cashfluent.app.data.league

import com.cashfluent.app.data.model.Player
import com.cashfluent.app.domain.league.Entrant
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueBackend
import com.cashfluent.app.domain.league.Tier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** The player's record, in memory. The real one is DataStore and does the same thing. */
class FakePlayerStore : PlayerStore {

    private val state = MutableStateFlow(Player())

    override val player: Flow<Player> = state

    override suspend fun update(transform: (Player) -> Player): Player {
        state.value = transform(state.value)
        return state.value
    }

    val current: Player get() = state.value
}

/**
 * Firestore's behaviour, in a HashMap: ids minted on sign-in, one queue per rung and
 * week, boards of twenty. [reachable] turns the network off, exactly where the real
 * thing throws — the suspending calls. Writes are accepted either way, because Firestore
 * queues them offline and sends them later, which is the behaviour worth testing.
 */
class FakeLeagueBackend : LeagueBackend {

    var reachable = true
    var signIns = 0
        private set

    private data class Lobby(var league: String, var seats: Int, var opened: Int)

    private val lobbies = mutableMapOf<String, Lobby>()
    private val boards = mutableMapOf<String, MutableStateFlow<List<Entrant>>>()

    override suspend fun signIn(): String {
        demandNetwork()
        signIns++
        return "uid-$signIns"
    }

    override suspend fun board(leagueId: String): List<Entrant> {
        demandNetwork()
        return rows(leagueId).value
    }

    override fun watch(leagueId: String): Flow<List<Entrant>> = rows(leagueId)

    override suspend fun takeSeat(week: Int, tier: Tier, me: Entrant): String {
        demandNetwork()
        val lobby = lobbies[League.lobbyId(week, tier)]
        val leagueId = when {
            lobby == null -> League.boardId(week, tier, 1).also {
                lobbies[League.lobbyId(week, tier)] = Lobby(it, seats = 1, opened = 1)
            }
            lobby.seats >= League.SIZE -> League.boardId(week, tier, lobby.opened + 1).also {
                lobby.league = it
                lobby.seats = 1
                lobby.opened++
            }
            else -> lobby.league.also { lobby.seats++ }
        }
        publish(leagueId, me)
        return leagueId
    }

    override fun publish(leagueId: String, me: Entrant) {
        val board = rows(leagueId)
        board.value = board.value.filterNot { it.id == me.id } + me
    }

    override fun leave(leagueId: String, id: String) {
        val board = rows(leagueId)
        board.value = board.value.filterNot { it.id == id }
    }

    fun rows(leagueId: String): MutableStateFlow<List<Entrant>> =
        boards.getOrPut(leagueId) { MutableStateFlow(emptyList()) }

    private fun demandNetwork() {
        if (!reachable) throw IOException("no network")
    }
}
