package com.cashfluent.app.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PayslipTest {

    @Test
    fun `24000 gross arrives as 1631 67 a month`() {
        val slip = Payslip.compute(24_000.0)
        assertEquals(2_160.0, slip.contributions, 0.001)
        assertEquals(21_840.0, slip.taxableIncome, 0.001)
        assertEquals(2_260.0, slip.tax, 0.001)
        assertEquals(19_580.0, slip.net, 0.001)
        assertEquals(1_631.67, slip.monthlyNet, 0.01)
    }

    @Test
    fun `the average rate is 18 4 percent and the marginal one is 25`() {
        val slip = Payslip.compute(24_000.0)
        assertEquals(0.1842, slip.averageRate, 0.0001)
        assertEquals(0.25, Payslip.marginalRate(slip.taxableIncome), 1e-9)
        assertTrue("confusing these two is where the myth starts", slip.averageRate < Payslip.marginalRate(slip.taxableIncome))
    }

    @Test
    fun `a raise never takes money away`() {
        assertEquals(20_945.0, Payslip.compute(26_000.0).net, 0.001)
        assertEquals(1_365.0, Payslip.netGainFromRaise(24_000.0, 2_000.0), 0.001)
    }

    @Test
    fun `no raise anywhere on the scale can ever be negative`() {
        var gross = 8_000.0
        while (gross <= 120_000.0) {
            assertTrue(
                "a raise at $gross must not reduce net pay",
                Payslip.netGainFromRaise(gross, 1_000.0) > 0.0,
            )
            gross += 500.0
        }
    }

    @Test
    fun `the allowance really is untaxed`() {
        assertEquals(0.0, Payslip.taxOn(8_000.0), 0.001)
        assertEquals(0.0, Payslip.taxOn(0.0), 0.001)
        assertEquals(0.0, Payslip.marginalRate(5_000.0), 1e-9)
    }

    @Test
    fun `each band taxes only the slice inside it`() {
        assertEquals(1_800.0, Payslip.taxOn(20_000.0), 0.001)
        assertEquals(1_800.0 + 5_000.0 * 0.25, Payslip.taxOn(25_000.0), 0.001)
        assertEquals(0.35, Payslip.marginalRate(45_000.0), 1e-9)
    }

    @Test
    fun `zero gross is a valid slider position`() {
        val slip = Payslip.compute(0.0)
        assertEquals(0.0, slip.net, 0.001)
        assertEquals(0.0, slip.averageRate, 1e-9)
    }
}
