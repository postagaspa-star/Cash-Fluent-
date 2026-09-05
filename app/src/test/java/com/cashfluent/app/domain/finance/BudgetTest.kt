package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetTest {

    private val net = 1_200.0

    @Test
    fun `the target on 1200 is 600 360 240`() {
        val target = Budget.target(net)
        assertEquals(600.0, target.needs, 0.001)
        assertEquals(360.0, target.wants, 0.001)
        assertEquals(240.0, target.future, 0.001)
    }

    @Test
    fun `Maya's real month leaves 140 for the future`() {
        val actual = Budget.actual(net, needsSpend = 670.0, wantsSpend = 390.0)
        assertEquals(140.0, actual.future, 0.001)
        assertEquals(net, actual.total, 0.001)
    }

    @Test
    fun `her split is 56 32 12`() {
        val shares = Budget.shares(Budget.actual(net, 670.0, 390.0), net)
        assertEquals(0.5583, shares.needs, 0.0001)
        assertEquals(0.3250, shares.wants, 0.0001)
        assertEquals(0.1167, shares.future, 0.0001)
    }

    @Test
    fun `the gap is 100 a month, which is 1200 a year`() {
        assertEquals(100.0, Budget.futureGapPerMonth(net, 140.0), 0.001)
        assertEquals(1_200.0, Budget.futureGapPerMonth(net, 140.0) * 12, 0.001)
    }

    @Test
    fun `hitting the target leaves no gap`() {
        assertEquals(0.0, Budget.futureGapPerMonth(net, 240.0), 0.001)
    }

    @Test
    fun `spending more than you earn is a state the app shows`() {
        assertTrue(Budget.isOverspending(net, 900.0, 400.0))
        assertEquals(100.0, Budget.overspend(net, 900.0, 400.0), 0.001)
        assertEquals(-100.0, Budget.actual(net, 900.0, 400.0).future, 0.001)
        assertFalse(Budget.isOverspending(net, 600.0, 360.0))
    }

    @Test
    fun `no income means no shares rather than a divide by zero`() {
        val shares = Budget.shares(Budget.actual(0.0, 0.0, 0.0), 0.0)
        assertEquals(0.0, shares.needs, 1e-9)
    }
}
