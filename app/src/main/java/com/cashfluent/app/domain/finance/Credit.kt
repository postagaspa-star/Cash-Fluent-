package com.cashfluent.app.domain.finance

/**
 * Module 08 — The file you never applied for.
 *
 *   utilisation = balance / limit
 *   payment to reach a target = balance - (limit x target)
 *
 * The second formula is the first one rearranged, and that rearrangement is the whole
 * practical value of the module: it turns "my ratio is bad" into a number you can pay.
 */
enum class UtilisationBand { COMFORTABLE, WATCH, HIGH }

object Credit {

    /** The line most lenders are said to look for. A convention, not a law of nature. */
    const val COMFORTABLE_TARGET = 0.30

    fun utilisation(balance: Double, limit: Double): Double {
        require(limit > 0.0) { "a credit limit must be positive" }
        require(balance >= 0.0) { "balance must not be negative" }
        return balance / limit
    }

    fun band(utilisation: Double): UtilisationBand = when {
        utilisation <= COMFORTABLE_TARGET -> UtilisationBand.COMFORTABLE
        utilisation <= 0.50 -> UtilisationBand.WATCH
        else -> UtilisationBand.HIGH
    }

    /**
     * What to pay before the statement date to land on [target].
     *
     * Zero when you are already there — the honest answer, and the one the simulator
     * shows most often once someone has understood the module.
     */
    fun paymentToReach(balance: Double, limit: Double, target: Double = COMFORTABLE_TARGET): Double {
        require(target > 0.0) { "target must be positive" }
        return (balance - limit * target).coerceAtLeast(0.0)
    }

    /** The other lever: the same balance against a bigger total limit. */
    fun utilisationWithExtraLimit(balance: Double, limit: Double, extraLimit: Double): Double =
        utilisation(balance, limit + extraLimit)

    /** The biggest balance that still sits inside [target] on this limit. */
    fun balanceCeiling(limit: Double, target: Double = COMFORTABLE_TARGET): Double = limit * target
}
