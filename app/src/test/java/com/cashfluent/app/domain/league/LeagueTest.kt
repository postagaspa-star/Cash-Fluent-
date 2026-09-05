package com.cashfluent.app.domain.league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueTest {

    private val day = 86_400_000L

    private fun entrant(id: String, name: String, week: Int, total: Int = week) =
        Entrant(id, name, totalPoints = total, weekPoints = week)

    @Test
    fun `weeks start on a Monday and every phone counts them the same way`() {
        assertEquals(0, Week.index(0))                 // Thursday 1 January 1970
        assertEquals(0, Week.index(3 * day))           // Sunday 4 January 1970
        assertEquals(1, Week.index(4 * day))           // Monday 5 January 1970
        // Saturday 5 September 2026 is epoch day 20701; the Monday before it is 20696.
        assertEquals(2957, Week.index(20_701 * day))
        assertEquals(Week.index(20_696 * day), Week.index(20_701 * day))
        assertEquals(2958, Week.index(20_703 * day))
        assertEquals(20_696, Week.mondayEpochDay(2957))
    }

    @Test
    fun `a week is seven days long on Monday and one day long on Sunday`() {
        assertEquals(7, Week.daysUntilNext(20_696 * day))
        assertEquals(4, Week.daysUntilNext(20_699 * day))
        assertEquals(1, Week.daysUntilNext(20_702 * day))
    }

    @Test
    fun `the board ranks this week first, then everything ever, then the name`() {
        val board = League.standings(
            listOf(
                entrant("a", "Marco", week = 320, total = 1_240),
                entrant("b", "Giulia", week = 410, total = 900),
                entrant("c", "Luca", week = 0, total = 5_000),
                entrant("d", "Ada", week = 320, total = 1_240),
            ),
            yourId = "a",
            tier = Tier.GOLD,
        )
        assertEquals(listOf("Giulia", "Ada", "Marco", "Luca"), board.map { it.entrant.name })
        assertEquals(listOf(1, 2, 3, 4), board.map { it.position })
        assertEquals(listOf(false, false, true, false), board.map { it.isYou })
        // Three played, so three are climbing; Luca, with nothing this week, is in the drop.
        assertEquals(listOf(Zone.PROMOTION, Zone.PROMOTION, Zone.PROMOTION, Zone.DEMOTION), board.map { it.zone })
    }

    @Test
    fun `the same person twice on one board is one row`() {
        val twice = listOf(entrant("a", "Marco", 100), entrant("a", "Marco", 100))
        assertEquals(1, League.standings(twice, yourId = "a", tier = Tier.WOOD).size)
    }

    @Test
    fun `the verdict on a finished week is read off its board`() {
        val board = (1..12).map { entrant("p$it", "P$it", week = it * 100) }
        val top = League.outcome(board, yourId = "p12", week = 2957, tier = Tier.BRONZE)
        assertEquals(Movement.PROMOTED, top.movement)
        assertEquals(1, top.position)
        assertEquals(12, top.size)
        val bottom = League.outcome(board, yourId = "p1", week = 2957, tier = Tier.BRONZE)
        assertEquals(Movement.DEMOTED, bottom.movement)
        assertEquals(12, bottom.position)
    }

    @Test
    fun `someone who never reached the board finished last with nothing`() {
        val board = (1..5).map { entrant("p$it", "P$it", week = it * 100) }
        val missing = League.outcome(board, yourId = "ghost", week = 2957, tier = Tier.SILVER)
        assertEquals(Movement.DEMOTED, missing.movement)
        assertEquals(0, missing.weekPoints)
        assertEquals(6, missing.position)
    }

    @Test
    fun `the gap to the promotion zone is what it takes to pass fifth place`() {
        val board = (1..12).map { entrant("p$it", "P$it", week = it * 100) }
        val standings = League.standings(board, yourId = "p6", tier = Tier.BRONZE)
        // p6 has 600 and sits 7th; fifth place is p8 with 800, so 201 points pass it.
        assertEquals(7, standings.first { it.isYou }.position)
        assertEquals(201, League.gapToPromotion(standings, "p6"))
        // Already climbing, so there is no gap to report.
        assertEquals(null, League.gapToPromotion(standings, "p12"))
        assertEquals(null, League.gapToPromotion(standings, "p8"))
        // Nobody by that name on this board.
        assertEquals(null, League.gapToPromotion(standings, "ghost"))
    }

    @Test
    fun `a small board has nobody to climb past`() {
        val board = (1..3).map { entrant("p$it", "P$it", week = it * 100) }
        val standings = League.standings(board, yourId = "p1", tier = Tier.BRONZE)
        assertEquals(null, League.gapToPromotion(standings, "p1"))
    }

    @Test
    fun `your neighbours are three rows that keep their height at either end`() {
        val board = (1..12).map { entrant("p$it", "P$it", week = it * 100) }
        val standings = League.standings(board, yourId = "p6", tier = Tier.BRONZE)
        // Seventh of twelve: the person above, you, the person below.
        assertEquals(listOf(6, 7, 8), League.around(standings, "p6").map { it.position })
        // Top of the board: still three rows, sliding down instead of off the edge.
        assertEquals(listOf(1, 2, 3), League.around(standings, "p12").map { it.position })
        assertEquals(listOf(10, 11, 12), League.around(standings, "p1").map { it.position })
        // A board smaller than the window is shown whole.
        val two = League.standings(board.take(2), yourId = "p1", tier = Tier.BRONZE)
        assertEquals(2, League.around(two, "p1").size)
    }

    @Test
    fun `every league on a rung has its own name, and every rung its own queue`() {
        assertEquals("w2957-gold", League.lobbyId(2957, Tier.GOLD))
        assertEquals("w2957-gold-3", League.boardId(2957, Tier.GOLD, 3))
        assertTrue(League.boardId(2957, Tier.GOLD, 1).startsWith(League.lobbyId(2957, Tier.GOLD)))
        assertEquals(20, League.SIZE)
    }

    @Test
    fun `a nickname is trimmed, capped and kept to one line`() {
        assertEquals("Marco Antonio Salvat", Nickname.clean("  Marco Antonio Salvatore Rossi  "))
        // A control character is dropped rather than becoming a space: a nickname is one line.
        assertEquals("ZoeONeil-Rossi", Nickname.clean("Zoe\tONeil-Rossi"))
        assertEquals("", Nickname.clean("   "))
        assertEquals(Nickname.MAX, Nickname.clean("x".repeat(100)).length)
    }
}
