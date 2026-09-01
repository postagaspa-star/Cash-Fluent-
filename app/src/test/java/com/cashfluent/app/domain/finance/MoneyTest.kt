package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `big results are grouped and rounded`() {
        assertEquals("\$131,757", Money.amount(131_756.57, Currency.USD))
        assertEquals("€30,895", Money.amount(30_895.43, Currency.EUR))
    }

    @Test
    fun `monthly pay keeps its cents`() {
        assertEquals("£1,631.67", Money.preciseAmount(1_631.6666, Currency.GBP))
    }

    @Test
    fun `rates read as percentages`() {
        assertEquals("18.4%", Money.percent(0.184166))
        assertEquals("25.0%", Money.percent(0.25))
        assertEquals("25%", Money.percent(0.25, decimals = 0))
    }

    @Test
    fun `content carries a placeholder, not a hard coded symbol`() {
        assertEquals(
            "You put in £12,000",
            Money.applyCurrency("You put in {c}12,000", Currency.GBP),
        )
    }

    @Test
    fun `an unknown currency code falls back rather than throwing`() {
        assertEquals(Currency.USD, Currency.fromCode(null))
        assertEquals(Currency.USD, Currency.fromCode("XYZ"))
        assertEquals(Currency.EUR, Currency.fromCode("EUR"))
    }
}
