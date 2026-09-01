package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtTest {

    private val balance = 800.0
    private val apr = 0.20

    @Test
    fun `the month's interest on 800 at 20 percent is 13 33`() {
        assertEquals(13.333, Debt.monthlyInterest(balance, apr), 0.001)
    }

    @Test
    fun `paying 200 clears it in four months and costs 35`() {
        val result = Debt.payoff(balance, apr, 200.0) as Payoff.Clears
        assertEquals(4.174, result.months, 0.001)
        assertEquals(5, result.wholeMonths)
        assertEquals(834.79, result.totalPaid, 0.01)
        assertEquals(34.79, result.totalInterest, 0.01)
    }

    @Test
    fun `paying 25 takes nearly four years and the jacket costs 1153`() {
        val result = Debt.payoff(balance, apr, 25.0) as Payoff.Clears
        assertEquals(46.108, result.months, 0.001)
        assertEquals(1_152.71, result.totalPaid, 0.01)
        assertEquals(352.71, result.totalInterest, 0.01)
    }

    @Test
    fun `paying 13 never clears — this is the lesson, not an error`() {
        val result = Debt.payoff(balance, apr, 13.0)
        assertTrue(result is Payoff.NeverClears)
        assertEquals(13.333, (result as Payoff.NeverClears).monthlyInterest, 0.001)
    }

    @Test
    fun `a payment exactly equal to the interest never clears either`() {
        val result = Debt.payoff(balance, apr, Debt.monthlyInterest(balance, apr))
        assertTrue("balance stands still forever", result is Payoff.NeverClears)
    }

    @Test
    fun `an interest free debt is plain division`() {
        val result = Debt.payoff(1_200.0, 0.0, 100.0) as Payoff.Clears
        assertEquals(12.0, result.months, 0.001)
        assertEquals(0.0, result.totalInterest, 0.001)
    }

    @Test
    fun `ten more a month is worth saying out loud`() {
        val (monthsSaved, interestSaved) = Debt.savingsFromPayingMore(balance, apr, 25.0, extra = 10.0)!!
        assertTrue(monthsSaved > 13.0)
        assertTrue(interestSaved > 100.0)
    }

    @Test
    fun `the balance curve falls to zero and stops`() {
        val curve = Debt.balanceCurve(balance, apr, 200.0)
        assertEquals(800.0, curve.first(), 0.001)
        assertEquals(0.0, curve.last(), 0.001)
        assertTrue(curve.size < 8)
    }

    @Test
    fun `a payment below the interest makes the curve climb`() {
        val curve = Debt.balanceCurve(balance, apr, 5.0, maxMonths = 12)
        assertTrue("the debt grows while you pay", curve.last() > curve.first())
    }
}
