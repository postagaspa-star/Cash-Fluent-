package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeesTest {

    private val monthly = 150.0
    private val gross = 0.07
    private val years = 30

    @Test
    fun `a 0 2 percent fund ends at 175935`() {
        assertEquals(175_935.49, Fees.outcome(monthly, gross, 0.002, years).finalValue, 0.01)
    }

    @Test
    fun `a 1 2 percent fund ends at 145040`() {
        assertEquals(145_040.06, Fees.outcome(monthly, gross, 0.012, years).finalValue, 0.01)
    }

    @Test
    fun `one percent a year costs 30895 — the number the module is built on`() {
        assertEquals(30_895.43, Fees.differenceVsBenchmark(monthly, gross, 0.012, years), 0.01)
    }

    @Test
    fun `you paid in the same 54000 either way`() {
        assertEquals(54_000.0, Fees.contributed(monthly, years), 0.001)
        assertEquals(54_000.0, Fees.outcome(monthly, gross, 0.012, years).contributed, 0.001)
    }

    @Test
    fun `fees paid is measured against paying none at all`() {
        val expensive = Fees.outcome(monthly, gross, 0.012, years)
        val free = Fees.outcome(monthly, gross, 0.0, years)
        assertEquals(free.finalValue - expensive.finalValue, expensive.feesPaid, 0.01)
        assertTrue("more than half of everything deposited", expensive.feesPaid > 54_000.0 * 0.5)
    }

    @Test
    fun `a free fund pays no fees`() {
        assertEquals(0.0, Fees.outcome(monthly, gross, 0.0, years).feesPaid, 1e-9)
    }

    @Test
    fun `the benchmark compared with itself costs nothing`() {
        assertEquals(0.0, Fees.differenceVsBenchmark(monthly, gross, Fees.BENCHMARK_TER, years), 1e-9)
    }
}
