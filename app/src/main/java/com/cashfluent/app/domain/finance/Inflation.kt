package com.cashfluent.app.domain.finance

import kotlin.math.pow

/**
 * Module 04 — Why cash quietly shrinks.
 *
 *   real value  = nominal / (1 + pi)^n
 *   real return = (1 + r) / (1 + pi) - 1
 */
object Inflation {

    /** What the balance says after [years] at [rate]. */
    fun nominalValue(amount: Double, rate: Double, years: Int): Double =
        amount * (1.0 + rate).pow(years)

    /** What that balance is worth in today's money. */
    fun realValue(nominal: Double, inflation: Double, years: Int): Double =
        nominal / (1.0 + inflation).pow(years)

    /** Both steps at once: grow it, then discount it back to today. */
    fun realValueAfter(amount: Double, rate: Double, inflation: Double, years: Int): Double =
        realValue(nominalValue(amount, rate, years), inflation, years)

    /** The exact version, not the r - pi shortcut. */
    fun realReturn(rate: Double, inflation: Double): Double =
        (1.0 + rate) / (1.0 + inflation) - 1.0

    /** Positive means buying power was lost, which is the usual case for cash. */
    fun purchasingPowerLost(amount: Double, rate: Double, inflation: Double, years: Int): Double =
        amount - realValueAfter(amount, rate, inflation, years)

    /** Real value year by year, for the two lines on the chart. */
    fun realValueCurve(amount: Double, rate: Double, inflation: Double, years: Int): List<Double> =
        (0..years).map { realValueAfter(amount, rate, inflation, it) }
}
