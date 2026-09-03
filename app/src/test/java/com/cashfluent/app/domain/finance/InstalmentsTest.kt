package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalmentsTest {

    private val price = 120.0
    private val count = 4
    private val lateFee = 6.0

    @Test
    fun `120 in four instalments is 30 each, and only 90 is ever borrowed`() {
        assertEquals(30.0, Instalments.instalment(price, count), 0.001)
        assertEquals(90.0, Instalments.amountFinanced(price, count), 0.001)
    }

    @Test
    fun `paying every instalment on time costs the price and nothing else`() {
        val plan = Instalments.plan(price, count, lateFee, missedPayments = 0)
        assertEquals(0.0, plan.feesCharged, 0.001)
        assertEquals(120.0, plan.totalPaid, 0.001)
        assertEquals(0.0, plan.feeShareOfPrice, 0.001)
    }

    @Test
    fun `one late fee is five percent of the whole order`() {
        val plan = Instalments.plan(price, count, lateFee, missedPayments = 1)
        assertEquals(6.0, plan.feesCharged, 0.001)
        assertEquals(126.0, plan.totalPaid, 0.001)
        assertEquals(0.05, plan.feeShareOfPrice, 0.0001)
    }

    @Test
    fun `two slips take a tenth of the price on top`() {
        val plan = Instalments.plan(price, count, lateFee, missedPayments = 2)
        assertEquals(12.0, plan.feesCharged, 0.001)
        assertEquals(132.0, plan.totalPaid, 0.001)
        assertEquals(0.10, plan.feeShareOfPrice, 0.0001)
    }

    @Test
    fun `you cannot miss more instalments than the plan has`() {
        val plan = Instalments.plan(price, count, lateFee, missedPayments = 9)
        assertEquals(4, plan.missed)
        assertEquals(24.0, plan.feesCharged, 0.001)
    }

    @Test
    fun `six on thirty for a fortnight annualises to 521 percent`() {
        val rate = Instalments.effectiveAnnualRate(lateFee, instalment = 30.0, daysLate = 14)
        assertEquals(5.2143, rate, 0.0001)
    }

    @Test
    fun `the same fortnight on a 20 percent card costs 23 cents`() {
        assertEquals(0.2301, Instalments.cardInterestFor(30.0, apr = 0.20, days = 14), 0.0001)
    }

    @Test
    fun `the late fee is the entire price of an interest-free plan`() {
        val fee = Instalments.effectiveAnnualRate(lateFee, 30.0, 14)
        val card = Instalments.cardInterestFor(30.0, 0.20, 14) / 30.0 * (365.0 / 14.0)
        assertTrue("a flat fee on a small sum dwarfs any ordinary APR", fee > card * 20)
    }

    @Test
    fun `paying on time for longer makes the same fee cheaper per day`() {
        val fortnight = Instalments.effectiveAnnualRate(lateFee, 30.0, 14)
        val quarter = Instalments.effectiveAnnualRate(lateFee, 30.0, 90)
        assertTrue("the shorter the loan, the worse a flat fee looks", fortnight > quarter)
        assertEquals(0.8111, quarter, 0.0001)
    }
}
