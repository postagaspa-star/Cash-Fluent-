package com.cashfluent.app.domain.league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TierTest {

    @Test
    fun `the ladder runs from wood to elite in eight rungs`() {
        assertEquals(8, Tier.entries.size)
        assertEquals(Tier.WOOD, Tier.entries.first())
        assertEquals(Tier.ELITE, Tier.entries.last())
        assertEquals(Tier.BRONZE, Tier.WOOD.next)
        assertNull(Tier.ELITE.next)
        assertNull(Tier.WOOD.previous)
        assertEquals(Tier.DIAMOND, Tier.ELITE.previous)
    }

    @Test
    fun `a rung survives being stored, and nonsense reads as wood`() {
        Tier.entries.forEach { assertEquals(it, Tier.fromName(it.name)) }
        Tier.entries.forEach { assertEquals(it, Tier.fromOrdinal(it.ordinal)) }
        assertEquals(Tier.WOOD, Tier.fromName(null))
        assertEquals(Tier.WOOD, Tier.fromName("PLATINUM"))
        assertEquals(Tier.WOOD, Tier.fromOrdinal(99))
        assertEquals(Tier.RUBY, Tier.fromName("RUBY"))
    }

    @Test
    fun `top five go up, bottom five of a big league go down, the middle stays`() {
        assertEquals(Zone.PROMOTION, Promotion.zone(position = 1, size = 20, tier = Tier.SILVER, weekPoints = 300))
        assertEquals(Zone.PROMOTION, Promotion.zone(position = 5, size = 20, tier = Tier.SILVER, weekPoints = 10))
        assertEquals(Zone.SAFE, Promotion.zone(position = 6, size = 20, tier = Tier.SILVER, weekPoints = 10))
        assertEquals(Zone.SAFE, Promotion.zone(position = 15, size = 20, tier = Tier.SILVER, weekPoints = 10))
        assertEquals(Zone.DEMOTION, Promotion.zone(position = 16, size = 20, tier = Tier.SILVER, weekPoints = 10))
        assertEquals(Zone.DEMOTION, Promotion.zone(position = 20, size = 20, tier = Tier.SILVER, weekPoints = 10))
    }

    @Test
    fun `a small league has no relegation zone, but nobody holds a place with nothing`() {
        assertEquals(Zone.SAFE, Promotion.zone(position = 8, size = 8, tier = Tier.SILVER, weekPoints = 10))
        assertEquals(Zone.DEMOTION, Promotion.zone(position = 8, size = 8, tier = Tier.SILVER, weekPoints = 0))
        // Alone on the board and idle all week: still down a rung. A league is for people who played.
        assertEquals(Zone.DEMOTION, Promotion.zone(position = 1, size = 1, tier = Tier.SILVER, weekPoints = 0))
        assertEquals(Zone.SAFE, Promotion.zone(position = 1, size = 1, tier = Tier.WOOD, weekPoints = 0))
    }

    @Test
    fun `nobody drops out of wood and nobody rises above elite`() {
        assertEquals(Zone.SAFE, Promotion.zone(position = 20, size = 20, tier = Tier.WOOD, weekPoints = 0))
        assertEquals(Zone.SAFE, Promotion.zone(position = 1, size = 20, tier = Tier.ELITE, weekPoints = 900))
        assertEquals(Tier.WOOD, Promotion.outcome(1, Tier.WOOD, 20, 20, 0).to)
        assertEquals(Tier.ELITE, Promotion.outcome(1, Tier.ELITE, 1, 20, 900).to)
    }

    @Test
    fun `an outcome says where you went and why`() {
        val up = Promotion.outcome(week = 2957, tier = Tier.BRONZE, position = 3, size = 12, weekPoints = 640)
        assertEquals(Movement.PROMOTED, up.movement)
        assertEquals(Tier.SILVER, up.to)
        val down = Promotion.outcome(week = 2957, tier = Tier.GOLD, position = 11, size = 12, weekPoints = 40)
        assertEquals(Movement.DEMOTED, down.movement)
        assertEquals(Tier.SILVER, down.to)
        val stay = Promotion.outcome(week = 2957, tier = Tier.GOLD, position = 7, size = 12, weekPoints = 40)
        assertEquals(Movement.STAYED, stay.movement)
    }

    @Test
    fun `ordinals read like English`() {
        assertEquals(listOf("1st", "2nd", "3rd", "4th", "11th", "12th", "13th", "21st", "22nd", "23rd"),
            listOf(1, 2, 3, 4, 11, 12, 13, 21, 22, 23).map(Promotion::ordinal))
    }
}
