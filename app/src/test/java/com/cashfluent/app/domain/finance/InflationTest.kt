package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InflationTest {

    @Test
    fun `3000 in a 0 5 percent account looks like 3075 after five years`() {
        assertEquals(3_075.75, Inflation.nominalValue(3_000.0, 0.005, 5), 0.01)
    }

    @Test
    fun `but it is worth 2653 in today's money`() {
        assertEquals(2_653.17, Inflation.realValueAfter(3_000.0, 0.005, 0.03, 5), 0.01)
        assertEquals(346.83, Inflation.purchasingPowerLost(3_000.0, 0.005, 0.03, 5), 0.01)
    }

    @Test
    fun `invested at 6 percent it keeps ahead of prices`() {
        assertEquals(4_014.68, Inflation.nominalValue(3_000.0, 0.06, 5), 0.01)
        assertEquals(3_463.10, Inflation.realValueAfter(3_000.0, 0.06, 0.03, 5), 0.01)
    }

    @Test
    fun `the real return of a losing account is negative`() {
        assertEquals(-0.0243, Inflation.realReturn(0.005, 0.03), 0.0001)
    }

    @Test
    fun `matching inflation exactly means standing still`() {
        assertEquals(0.0, Inflation.realReturn(0.03, 0.03), 1e-9)
        assertEquals(3_000.0, Inflation.realValueAfter(3_000.0, 0.03, 0.03, 10), 0.001)
    }

    @Test
    fun `the curve starts at the amount and falls year by year`() {
        val curve = Inflation.realValueCurve(3_000.0, 0.005, 0.03, 5)
        assertEquals(6, curve.size)
        assertEquals(3_000.0, curve.first(), 0.001)
        assertTrue(curve.zipWithNext().all { (a, b) -> b < a })
    }
}
