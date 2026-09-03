package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MortgageTest {

    private val price = 200_000.0
    private val deposit = 0.10
    private val rate = 0.045
    private val term = 30

    @Test
    fun `180000 at 4 point 5 percent over 30 years is 912 a month`() {
        assertEquals(912.03, Mortgage.monthlyPayment(180_000.0, rate, term), 0.01)
    }

    @Test
    fun `the interest over thirty years is most of another home`() {
        assertEquals(328_332.08, Mortgage.totalRepaid(180_000.0, rate, term), 0.5)
        assertEquals(148_332.08, Mortgage.totalInterest(180_000.0, rate, term), 0.5)
    }

    @Test
    fun `an interest-free loan is just the price split up`() {
        assertEquals(500.0, Mortgage.monthlyPayment(180_000.0, 0.0, 30), 0.001)
    }

    @Test
    fun `the payment is not the cost of owning`() {
        val cost = Mortgage.cost(price, deposit, rate, term)
        assertEquals(912.03, cost.monthlyPayment, 0.01)
        assertEquals(166.67, cost.monthlyMaintenance, 0.01)
        assertEquals(1_078.70, cost.monthlyTotal, 0.01)
    }

    @Test
    fun `28000 has to exist before the first payment`() {
        val cost = Mortgage.cost(price, deposit, rate, term)
        assertEquals(20_000.0, cost.deposit, 0.001)
        assertEquals(8_000.0, cost.purchaseFees, 0.001)
        assertEquals(28_000.0, cost.upfront, 0.001)
    }

    @Test
    fun `five years of paying 912 leaves 164084 still owed`() {
        assertEquals(164_084.25, Mortgage.balanceAfter(180_000.0, rate, term, months = 60), 0.5)
    }

    @Test
    fun `against 950 rent, five years of owning comes out almost exactly level`() {
        val result = Mortgage.compare(price, deposit, rate, term, monthlyRent = 950.0, overYears = 5)
        assertEquals(92_722.01, result.ownCashOut, 0.5)
        assertEquals(57_000.0, result.rentCashOut, 0.001)
        assertEquals(35_722.01, result.extraCashOut, 0.5)
        assertEquals(35_915.75, result.equity, 0.5)
        assertEquals(193.74, result.net, 1.0)
    }

    @Test
    fun `cheap enough rent makes renting the better arithmetic`() {
        val result = Mortgage.compare(price, deposit, rate, term, monthlyRent = 700.0, overYears = 5)
        assertTrue("renting for 700 wins clearly over five years", result.net < -10_000.0)
    }

    @Test
    fun `owning pulls ahead the longer you stay, because more of the payment is yours`() {
        val five = Mortgage.compare(price, deposit, rate, term, 950.0, overYears = 5)
        val fifteen = Mortgage.compare(price, deposit, rate, term, 950.0, overYears = 15)
        assertTrue("early payments are nearly all interest", fifteen.net > five.net)
    }
}
