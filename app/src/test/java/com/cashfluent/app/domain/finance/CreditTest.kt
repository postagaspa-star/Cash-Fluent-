package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Test

class CreditTest {

    private val limit = 1_000.0
    private val balance = 450.0

    @Test
    fun `450 against a 1000 limit is 45 percent`() {
        assertEquals(0.45, Credit.utilisation(balance, limit), 0.0001)
    }

    @Test
    fun `45 percent is the band worth watching, not the alarming one`() {
        assertEquals(UtilisationBand.WATCH, Credit.band(0.45))
        assertEquals(UtilisationBand.COMFORTABLE, Credit.band(0.30))
        assertEquals(UtilisationBand.COMFORTABLE, Credit.band(0.09))
        assertEquals(UtilisationBand.HIGH, Credit.band(0.80))
    }

    @Test
    fun `paying 150 before the statement lands exactly on 30 percent`() {
        val payment = Credit.paymentToReach(balance, limit)
        assertEquals(150.0, payment, 0.001)
        assertEquals(0.30, Credit.utilisation(balance - payment, limit), 0.0001)
    }

    @Test
    fun `already inside the target asks for nothing`() {
        assertEquals(0.0, Credit.paymentToReach(200.0, limit), 0.001)
    }

    @Test
    fun `a second card with a 500 limit does the same job without paying anything`() {
        assertEquals(0.30, Credit.utilisationWithExtraLimit(balance, limit, 500.0), 0.0001)
    }

    @Test
    fun `clearing the phone before the statement date shows 9 percent instead of 45`() {
        assertEquals(0.09, Credit.utilisation(90.0, limit), 0.0001)
    }

    @Test
    fun `the ceiling is the largest balance still inside the target`() {
        assertEquals(300.0, Credit.balanceCeiling(limit), 0.001)
        assertEquals(0.30, Credit.utilisation(Credit.balanceCeiling(limit), limit), 0.0001)
    }

    @Test
    fun `a stricter target asks for a bigger payment`() {
        assertEquals(350.0, Credit.paymentToReach(balance, limit, target = 0.10), 0.001)
    }
}
