package com.cashfluent.app.domain.finance

/**
 * Module 07 — The cost of "0%".
 *
 *   instalment    = price / n
 *   fee share     = fees / price
 *   annual rate   = (fee / instalment) x (365 / days late)
 *
 * A pay-in-four plan really is interest-free while you pay it. The price lives
 * entirely in the late fee, and a late fee is a flat charge on a small amount for a
 * short time — which is exactly the shape that annualises into an enormous number.
 *
 * The annualised rate is the honest way to compare it with anything else that lends
 * money, and it is the number the plan never shows you.
 */
data class InstalmentPlan(
    val price: Double,
    val instalment: Double,
    val financed: Double,
    val missed: Int,
    val feesCharged: Double,
    val totalPaid: Double,
) {
    /** What the slip cost as a share of the thing you actually bought. */
    val feeShareOfPrice: Double get() = if (price == 0.0) 0.0 else feesCharged / price
}

object Instalments {

    const val DAYS_IN_YEAR = 365.0

    fun instalment(price: Double, count: Int): Double {
        require(count > 0) { "a plan needs at least one instalment" }
        return price / count
    }

    /** Paying the first instalment at the till means you only ever borrow the rest. */
    fun amountFinanced(price: Double, count: Int): Double = price - instalment(price, count)

    fun plan(price: Double, count: Int, lateFee: Double, missedPayments: Int): InstalmentPlan {
        require(price >= 0.0) { "price must not be negative" }
        require(lateFee >= 0.0) { "a late fee must not be negative" }
        require(missedPayments >= 0) { "missedPayments must not be negative" }
        val missed = missedPayments.coerceAtMost(count)
        val fees = lateFee * missed
        return InstalmentPlan(
            price = price,
            instalment = instalment(price, count),
            financed = amountFinanced(price, count),
            missed = missed,
            feesCharged = fees,
            totalPaid = price + fees,
        )
    }

    /**
     * What one late fee works out to as a yearly rate on the money you actually kept.
     *
     * You held one instalment for [daysLate] extra days and it cost [lateFee]. Scale
     * that to a year the way a lender has to when it quotes an APR.
     */
    fun effectiveAnnualRate(lateFee: Double, instalment: Double, daysLate: Int): Double {
        require(daysLate > 0) { "daysLate must be positive" }
        if (instalment <= 0.0) return 0.0
        return (lateFee / instalment) * (DAYS_IN_YEAR / daysLate)
    }

    /** The same borrowing on an ordinary card, for comparison. Simple interest, no fee. */
    fun cardInterestFor(amount: Double, apr: Double, days: Int): Double =
        amount * apr / DAYS_IN_YEAR * days
}
