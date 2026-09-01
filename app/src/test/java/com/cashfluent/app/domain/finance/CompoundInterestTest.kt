package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Module 02's worked example is the strongest number in the app, so it is pinned here.
 * If one of these fails, the copy on screen has started lying.
 */
class CompoundInterestTest {

    private val alex = SavingPlan(monthlyAmount = 100.0, startAge = 18, stopAge = 28, annualRate = 0.07)
    private val sam = SavingPlan(monthlyAmount = 100.0, startAge = 28, stopAge = 58, annualRate = 0.07)

    @Test
    fun `ten years of 100 a month at 7 percent reaches 17308`() {
        assertEquals(17_308.48, CompoundInterest.monthlyPayments(100.0, 0.07, 120), 0.01)
    }

    @Test
    fun `alex stops at 28 and still has 131757 at 58`() {
        assertEquals(17_308.48, alex.valueAt(28), 0.01)
        assertEquals(131_756.57, alex.valueAt(58), 0.01)
    }

    @Test
    fun `sam pays three times as much and ends with less`() {
        assertEquals(121_997.10, sam.valueAt(58), 0.01)
        assertEquals(12_000.0, alex.totalContributed, 0.001)
        assertEquals(36_000.0, sam.totalContributed, 0.001)
        assertTrue("Alex must finish ahead — the whole module rests on it", alex.valueAt(58) > sam.valueAt(58))
        assertEquals(9_759.47, alex.valueAt(58) - sam.valueAt(58), 0.01)
    }

    @Test
    fun `contributions stop when the payments stop`() {
        assertEquals(0.0, alex.contributedBy(18), 0.001)
        assertEquals(6_000.0, alex.contributedBy(23), 0.001)
        assertEquals(12_000.0, alex.contributedBy(28), 0.001)
        assertEquals("flat forever after the last payment", 12_000.0, alex.contributedBy(58), 0.001)
    }

    @Test
    fun `nothing exists before the plan starts`() {
        assertEquals(0.0, alex.valueAt(17), 0.001)
        assertEquals(0.0, alex.valueAt(18), 0.001)
    }

    @Test
    fun `a zero rate is a slider position, not a crash`() {
        assertEquals(1_200.0, CompoundInterest.monthlyPayments(100.0, 0.0, 12), 0.001)
        assertEquals(1_000.0, CompoundInterest.lumpSum(1_000.0, 0.0, 30), 0.001)
    }

    @Test
    fun `the chart covers every year from start to the requested age`() {
        val curve = alex.curve(toAge = 58)
        assertEquals(41, curve.size)
        assertEquals(18, curve.first().age)
        assertEquals(58, curve.last().age)
        assertEquals(131_756.57, curve.last().value, 0.01)
    }
}
