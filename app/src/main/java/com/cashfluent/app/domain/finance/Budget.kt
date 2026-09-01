package com.cashfluent.app.domain.finance

/** How a month splits three ways. */
data class BudgetSplit(val needs: Double, val wants: Double, val future: Double) {
    val total: Double get() = needs + wants + future
}

/** Each bucket as a share of net income, for the stacked bar. */
data class BudgetShares(val needs: Double, val wants: Double, val future: Double)

/**
 * Module 01 — Where your money actually goes.
 *
 *   Net - Needs - Wants = what is left for your future
 *   Needs = 0.50 x Net   Wants = 0.30 x Net   Future = 0.20 x Net
 */
object Budget {

    const val TARGET_NEEDS = 0.50
    const val TARGET_WANTS = 0.30
    const val TARGET_FUTURE = 0.20

    /** What 50/30/20 would say, given what actually arrives. */
    fun target(netIncome: Double) = BudgetSplit(
        needs = netIncome * TARGET_NEEDS,
        wants = netIncome * TARGET_WANTS,
        future = netIncome * TARGET_FUTURE,
    )

    /**
     * What the month really looks like. Whatever the two spending buckets leave behind
     * is the future bucket — which goes negative when someone is spending more than
     * they earn, and that is a state the app shows rather than hides.
     */
    fun actual(netIncome: Double, needsSpend: Double, wantsSpend: Double) = BudgetSplit(
        needs = needsSpend,
        wants = wantsSpend,
        future = netIncome - needsSpend - wantsSpend,
    )

    fun shares(split: BudgetSplit, netIncome: Double): BudgetShares =
        if (netIncome <= 0.0) BudgetShares(0.0, 0.0, 0.0)
        else BudgetShares(split.needs / netIncome, split.wants / netIncome, split.future / netIncome)

    /** How far the future bucket is from the 20% target. Positive means short. */
    fun futureGapPerMonth(netIncome: Double, actualFuture: Double): Double =
        netIncome * TARGET_FUTURE - actualFuture

    fun overspend(netIncome: Double, needsSpend: Double, wantsSpend: Double): Double =
        (needsSpend + wantsSpend) - netIncome

    fun isOverspending(netIncome: Double, needsSpend: Double, wantsSpend: Double): Boolean =
        overspend(netIncome, needsSpend, wantsSpend) > 0.0
}
