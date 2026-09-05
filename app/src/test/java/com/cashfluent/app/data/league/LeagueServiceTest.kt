package com.cashfluent.app.data.league

import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Tier
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The week, end to end, with the network replaced by a HashMap: signing in, taking a
 * seat, scoring, Monday, and every one of those with the network missing at the moment
 * it is needed. What the phone owes the board and what the board owes the phone.
 */
class LeagueServiceTest {

    private val day = 86_400_000L

    /** Monday 31 August 2026, the first day of week 2957. */
    private var clock = 20_696 * day

    private val backend = FakeLeagueBackend()

    private fun newPlayer(): Pair<LeagueService, FakePlayerStore> {
        val store = FakePlayerStore()
        return LeagueService(store, backend) { clock } to store
    }

    private fun mondayAfter() {
        clock += 7 * day
    }

    @Test
    fun `a phone signs in, takes a seat, and is on the board`() = runTest {
        val (league, store) = newPlayer()
        league.settle()

        assertTrue(store.current.signedIn)
        assertEquals("w2957-wood-1", store.current.leagueId)
        assertEquals(listOf(store.current.id), backend.board("w2957-wood-1").map { it.id })
    }

    @Test
    fun `opening the screen twice does not take two seats`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        league.settle()

        assertEquals(1, backend.signIns)
        assertEquals(1, backend.board(store.current.leagueId!!).size)
    }

    @Test
    fun `a score reaches the board, and a better one replaces the best`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        league.rename("Andrea")

        val first = league.recordGame("budget-share", 320)
        assertEquals(320, first.totalPoints)
        assertTrue(first.newBest)

        val worse = league.recordGame("budget-share", 100)
        assertFalse(worse.newBest)
        assertEquals(320, worse.best)
        assertEquals(420, worse.weekPoints)

        val row = backend.board(store.current.leagueId!!).single()
        assertEquals("Andrea", row.name)
        assertEquals(420, row.weekPoints)
        assertEquals(420, row.totalPoints)
    }

    @Test
    fun `twenty people fill a league and the twenty-first opens the next one`() = runTest {
        val seats = (1..21).map { number ->
            val (league, store) = newPlayer()
            league.settle()
            league.rename("P$number")
            store.current.leagueId
        }
        assertEquals(List(20) { "w2957-wood-1" }, seats.take(20))
        assertEquals("w2957-wood-2", seats.last())
        assertEquals(20, backend.board("w2957-wood-1").size)
    }

    @Test
    fun `nothing is lost while the network is away`() = runTest {
        backend.reachable = false
        val (league, store) = newPlayer()
        league.settle()

        assertFalse(store.current.signedIn)
        assertFalse(store.current.seated)
        assertEquals(Connection.OFFLINE, league.connection.value)

        // The games still work, and the points are still counted.
        league.recordGame("budget-share", 400)
        assertEquals(400, store.current.weekPoints)

        backend.reachable = true
        league.settle()

        assertEquals(Connection.ONLINE, league.connection.value)
        assertEquals(400, backend.board(store.current.leagueId!!).single().weekPoints)
    }

    @Test
    fun `Monday closes the board, moves the rung and starts again from zero`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        league.recordGame("budget-share", 400)
        val (rival, _) = newPlayer()
        rival.settle()

        mondayAfter()
        league.settle()

        val player = store.current
        assertEquals(Tier.BRONZE, player.tier)
        assertEquals(Movement.PROMOTED, player.lastOutcome!!.movement)
        assertEquals(1, player.lastOutcome!!.position)
        assertEquals(400, player.lastOutcome!!.weekPoints)
        assertEquals(0, player.weekPoints)
        assertEquals(400, player.totalPoints)
        assertEquals("w2958-bronze-1", player.leagueId)
        assertNull(player.unsettled)
    }

    @Test
    fun `a week with no points costs a rung, and Wood is the floor`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        store.update { it.copy(tier = Tier.SILVER) }
        league.settle()

        mondayAfter()
        league.settle()
        assertEquals(Tier.BRONZE, store.current.tier)

        mondayAfter()
        league.settle()
        assertEquals(Tier.WOOD, store.current.tier)

        mondayAfter()
        league.settle()
        assertEquals(Tier.WOOD, store.current.tier)
        assertEquals(Movement.STAYED, store.current.lastOutcome!!.movement)
    }

    @Test
    fun `the verdict waits for the board it has to be read off`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        league.recordGame("budget-share", 400)

        mondayAfter()
        backend.reachable = false
        league.settle()

        assertEquals(Tier.WOOD, store.current.tier)
        assertNotNull(store.current.unsettled)
        assertFalse(store.current.seated)
        // The points for the new week are already being counted on the phone.
        league.recordGame("budget-share", 100)
        assertEquals(100, store.current.weekPoints)

        backend.reachable = true
        league.settle()

        assertEquals(Tier.BRONZE, store.current.tier)
        assertNull(store.current.unsettled)
        assertEquals("w2958-bronze-1", store.current.leagueId)
        assertEquals(100, backend.board("w2958-bronze-1").single().weekPoints)
    }

    @Test
    fun `a rung is only ever one step from the one before it`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        store.update { it.copy(tier = Tier.DIAMOND) }
        league.recordGame("budget-share", 400)

        // Four weeks pass with the phone switched off, and only the last one is settled.
        clock += 28 * day
        league.settle()

        assertEquals(Tier.ELITE, store.current.tier)
    }

    @Test
    fun `resetting takes you off the board and back to the bottom of the ladder`() = runTest {
        val (league, store) = newPlayer()
        league.settle()
        league.rename("Andrea")
        league.recordGame("budget-share", 400)
        store.update { it.copy(tier = Tier.GOLD) }
        val leagueId = store.current.leagueId!!

        league.reset()

        assertEquals(0, store.current.totalPoints)
        assertEquals(Tier.WOOD, store.current.tier)
        assertTrue(store.current.bests.isEmpty())
        assertEquals("Andrea", store.current.name)
        assertTrue(store.current.signedIn)
        assertNull(store.current.leagueId)
        assertEquals(emptyList<String>(), backend.board(leagueId).map { it.id })
    }
}
