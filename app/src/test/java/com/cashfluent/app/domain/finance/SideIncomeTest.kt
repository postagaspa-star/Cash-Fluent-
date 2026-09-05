package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SideIncomeTest {

    private val monthly = 500.0
    private val expenses = 900.0
    private val rate = 0.25

    @Test
    fun `500 a month is 6000 a year, and 5100 of it is taxable`() {
        val year = SideIncome.year(monthly, expenses, rate)
        assertEquals(6_000.0, year.gross, 0.001)
        assertEquals(5_100.0, year.profit, 0.001)
    }

    @Test
    fun `a quarter of the profit is 1275`() {
        assertEquals(1_275.0, SideIncome.year(monthly, expenses, rate).taxDue, 0.001)
    }

    @Test
    fun `after the bill and the gear, 3825 was actually yours`() {
        assertEquals(3_825.0, SideIncome.year(monthly, expenses, rate).keep, 0.001)
    }

    @Test
    fun `expenses pull the real rate below the headline one`() {
        val year = SideIncome.year(monthly, expenses, rate)
        assertEquals(0.2125, year.effectiveRateOnGross, 0.0001)
        assertTrue("expenses always make the true rate lower", year.effectiveRateOnGross < rate)
    }

    @Test
    fun `holding back 125 from every payment covers it with 225 to spare`() {
        assertEquals(125.0, SideIncome.flatSetAside(monthly, rate), 0.001)
        assertEquals(225.0, SideIncome.cushion(monthly, expenses, rate), 0.001)
    }

    @Test
    fun `with no expenses the flat habit collects exactly the bill and no more`() {
        assertEquals(0.0, SideIncome.cushion(monthly, 0.0, rate), 0.001)
        assertEquals(1_500.0, SideIncome.year(monthly, 0.0, rate).taxDue, 0.001)
    }

    @Test
    fun `a loss-making year owes nothing rather than a negative bill`() {
        val year = SideIncome.year(100.0, 5_000.0, rate)
        assertEquals(0.0, year.profit, 0.001)
        assertEquals(0.0, year.taxDue, 0.001)
    }
}
