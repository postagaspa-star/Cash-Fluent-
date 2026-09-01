package com.cashfluent.app.domain.finance

/**
 * One progressive tax band. [upTo] is null for the top band, which has no ceiling.
 */
data class TaxBand(val upTo: Double?, val rate: Double)

data class PayslipBreakdown(
    val gross: Double,
    val contributions: Double,
    val tax: Double,
) {
    val net: Double get() = gross - contributions - tax
    val monthlyNet: Double get() = net / 12.0
    val taxableIncome: Double get() = gross - contributions

    /** What actually comes off, all in. Never the same as the marginal rate. */
    val averageRate: Double get() = if (gross <= 0.0) 0.0 else (contributions + tax) / gross
}

/**
 * Module 06 — Gross vs net.
 *
 *   net = gross - contributions - income tax
 *
 * The rates below are simplified and illustrative on purpose: they show how the
 * machine works without claiming to be any particular country's numbers. The app
 * says so on screen every time they appear.
 */
object Payslip {

    val ILLUSTRATIVE_BANDS: List<TaxBand> = listOf(
        TaxBand(upTo = 8_000.0, rate = 0.00),
        TaxBand(upTo = 20_000.0, rate = 0.15),
        TaxBand(upTo = 40_000.0, rate = 0.25),
        TaxBand(upTo = null, rate = 0.35),
    )

    const val ILLUSTRATIVE_CONTRIBUTION_RATE = 0.09

    fun compute(
        gross: Double,
        contributionRate: Double = ILLUSTRATIVE_CONTRIBUTION_RATE,
        bands: List<TaxBand> = ILLUSTRATIVE_BANDS,
    ): PayslipBreakdown {
        require(gross >= 0.0) { "gross must not be negative" }
        val contributions = gross * contributionRate
        val taxable = gross - contributions
        return PayslipBreakdown(gross, contributions, taxOn(taxable, bands))
    }

    /** Each band's rate applies only to the slice of income inside that band. */
    fun taxOn(taxable: Double, bands: List<TaxBand> = ILLUSTRATIVE_BANDS): Double {
        var tax = 0.0
        var floor = 0.0
        for (band in bands) {
            if (taxable <= floor) break
            val ceiling = band.upTo ?: Double.MAX_VALUE
            tax += (minOf(taxable, ceiling) - floor) * band.rate
            floor = ceiling
        }
        return tax
    }

    /** The rate on the next unit earned — the number people confuse with the average. */
    fun marginalRate(taxable: Double, bands: List<TaxBand> = ILLUSTRATIVE_BANDS): Double {
        var floor = 0.0
        for (band in bands) {
            val ceiling = band.upTo ?: return band.rate
            if (taxable < ceiling) return band.rate
            floor = ceiling
        }
        return bands.last().rate
    }

    /** Proves the myth wrong: what a raise actually adds to your pocket. */
    fun netGainFromRaise(
        gross: Double,
        raise: Double,
        contributionRate: Double = ILLUSTRATIVE_CONTRIBUTION_RATE,
        bands: List<TaxBand> = ILLUSTRATIVE_BANDS,
    ): Double =
        compute(gross + raise, contributionRate, bands).net - compute(gross, contributionRate, bands).net
}
