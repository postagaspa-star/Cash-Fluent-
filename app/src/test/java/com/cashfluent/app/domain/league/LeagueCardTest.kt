package com.cashfluent.app.domain.league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueCardTest {

    private val marco = LeagueCard("0badf00d", "Marco", 1_240, 2957, 320, Tier.GOLD)
    private val giulia = LeagueCard("cafe1234", "Giulia B.", 900, 2957, 410, Tier.GOLD)

    @Test
    fun `a card survives the trip through text`() {
        val token = LeagueCards.encode(marco)
        // Gold is the fourth rung, digit 3.
        assertTrue(token, token.startsWith("CF1|0badf00d|Marco|1240|2957|320|3|"))
        assertEquals(marco, LeagueCards.decode(token))
    }

    @Test
    fun `a nickname with accents, spaces and quotes comes back intact`() {
        val zoe = marco.copy(name = "Zoë O'Neil-Rossi")
        assertEquals(zoe, LeagueCards.decode(LeagueCards.encode(zoe)))
    }

    @Test
    fun `a nickname is trimmed, capped and cannot carry the separator`() {
        val messy = marco.copy(name = "  Ma|rco Antonio Salvatore Rossi  ")
        assertEquals("Marco Antonio Salvat", LeagueCards.decode(LeagueCards.encode(messy))!!.name)
    }

    @Test
    fun `a card edited in transit is refused`() {
        val token = LeagueCards.encode(marco)
        assertNull(LeagueCards.decode(token.replace("|1240|", "|9240|")))
        assertNull(LeagueCards.decode(token.replace("Marco", "Marc0")))
        assertNull(LeagueCards.decode(token.replace("|3|", "|7|")))
        assertNull(LeagueCards.decode(token.dropLast(1)))
    }

    @Test
    fun `nonsense is refused, never thrown`() {
        listOf("", "CF1", "CF1|hello", "CF1|0badf00d|x|1|1|1|0|00000000", "hello world", "CF1|||||||", "CF1|0badf00d|x|1|1|1|9|00000000")
            .forEach { assertNull(it, LeagueCards.decode(it)) }
    }

    @Test
    fun `more points this week than in total is not a real card`() {
        val impossible = marco.copy(totalPoints = 100, weekPoints = 200)
        assertNull(LeagueCards.decode(LeagueCards.encode(impossible)))
    }

    @Test
    fun `every card in a group chat is found, once each, and the chatter is ignored`() {
        val chat = """
            Marco: here's mine
            Cashfluent league card — Marco, 320 this week
            ${LeagueCards.encode(marco)}
            Giulia: ${LeagueCards.encode(giulia)} beat that
            Marco: again ${LeagueCards.encode(marco)}
            some random CF1|junk that is not a card
        """.trimIndent()
        assertEquals(listOf(marco, giulia), LeagueCards.findAll(chat))
    }

    @Test
    fun `a wall of junk yields nothing and takes no time`() {
        val junk = "CF1|".repeat(20_000) + "x".repeat(50_000)
        assertEquals(emptyList<LeagueCard>(), LeagueCards.findAll(junk))
    }

    @Test
    fun `weeks start on a Monday and every phone counts them the same way`() {
        val day = 86_400_000L
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
    fun `the board ranks this week first, then everything ever, and a stale card scores nothing`() {
        val stale = LeagueCard("deadbeef", "Luca", 5_000, 2950, 900, Tier.GOLD)
        val board = League.standings(marco, listOf(giulia, stale), currentWeek = 2957, tier = Tier.GOLD)
        assertEquals(listOf("Giulia B.", "Marco", "Luca"), board.map { it.card.name })
        assertEquals(listOf(1, 2, 3), board.map { it.position })
        assertEquals(listOf(410, 320, 0), board.map { it.weekPoints })
        assertEquals(listOf(false, true, false), board.map { it.isYou })
        // Two in the promotion zone, and Luca, with nothing this week, in the drop.
        assertEquals(listOf(Zone.PROMOTION, Zone.PROMOTION, Zone.DEMOTION), board.map { it.zone })
    }

    @Test
    fun `merging adds new friends, refreshes newer cards and keeps older ones out`() {
        val first = League.merge(emptyList(), listOf(giulia), yourId = marco.id)
        assertEquals(1, first.added)
        val newer = giulia.copy(totalPoints = 1_000, weekPoints = 510)
        val second = League.merge(first.friends, listOf(newer, marco), yourId = marco.id)
        assertEquals(1, second.updated)
        assertEquals(1, second.yourself)
        assertEquals(listOf(newer), second.friends)
        val older = giulia.copy(week = 2900, totalPoints = 10, weekPoints = 5)
        val third = League.merge(second.friends, listOf(older), yourId = marco.id)
        assertEquals(1, third.unchanged)
        assertEquals(listOf(newer), third.friends)
    }

    @Test
    fun `a league holds twenty people and not one more`() {
        val crowd = (1..25).map { LeagueCard("%08x".format(it), "P$it", it, 2957, it, Tier.WOOD) }
        val result = League.merge(emptyList(), crowd, yourId = marco.id)
        assertEquals(League.MAX_FRIENDS, result.friends.size)
        assertEquals(19, result.added)
        assertEquals(6, result.refusedFull)
        assertFalse(League.standings(marco, result.friends, 2957, Tier.WOOD).size > League.SIZE)
    }
}
