package com.cashfluent.app.domain.finance

/**
 * Module 05 — What you're actually buying.
 *
 *   annual cost      = value x TER
 *   effective return = r - TER
 *
 * The fee is taken from inside the fund every year, so it costs you the fee plus
 * everything the fee would have earned for the rest of the time.
 */
data class FundOutcome(
    val finalValue: Double,
    val contributed: Double,
    val feesPaid: Double,
) {
    val growth: Double get() = finalValue - contributed
}

object Fees {

    /** A cheap broad index fund, used as the yardstick in the comparison. */
    const val BENCHMARK_TER = 0.002

    fun effectiveReturn(grossReturn: Double, ter: Double): Double = grossReturn - ter

    fun contributed(monthlyAmount: Double, years: Int): Double = monthlyAmount * years * 12

    fun outcome(monthlyAmount: Double, grossReturn: Double, ter: Double, years: Int): FundOutcome {
        val withFee = CompoundInterest.monthlyPayments(
            monthlyAmount, effectiveReturn(grossReturn, ter), years * 12,
        )
        val withoutFee = CompoundInterest.monthlyPayments(monthlyAmount, grossReturn, years * 12)
        return FundOutcome(
            finalValue = withFee,
            contributed = contributed(monthlyAmount, years),
            feesPaid = withoutFee - withFee,
        )
    }

    /** What the same money would have become in a [benchmarkTer] fund instead. */
    fun differenceVsBenchmark(
        monthlyAmount: Double,
        grossReturn: Double,
        ter: Double,
        years: Int,
        benchmarkTer: Double = BENCHMARK_TER,
    ): Double =
        outcome(monthlyAmount, grossReturn, benchmarkTer, years).finalValue -
            outcome(monthlyAmount, grossReturn, ter, years).finalValue
}
