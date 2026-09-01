package com.cashfluent.app.domain.finance

import kotlin.math.ceil
import kotlin.math.ln

/**
 * Module 03 — What borrowing actually costs.
 *
 *   monthly interest = balance x (APR / 12)
 *   n = -ln(1 - (i x B) / P) / ln(1 + i)
 *
 * The interesting case is the one where the formula has no answer: when the payment
 * is not larger than the month's interest, the balance never goes down. That is not
 * an invalid input to reject — it is the lesson of the module.
 */
sealed interface Payoff {
    data class Clears(
        val months: Double,
        val totalPaid: Double,
        val totalInterest: Double,
    ) : Payoff {
        val wholeMonths: Int get() = ceil(months).toInt()
    }

    /** Payment <= this month's interest: the debt outlives every payment you make. */
    data class NeverClears(val monthlyInterest: Double) : Payoff
}

object Debt {

    fun monthlyRate(apr: Double): Double = apr / 12.0

    fun monthlyInterest(balance: Double, apr: Double): Double = balance * monthlyRate(apr)

    fun payoff(balance: Double, apr: Double, monthlyPayment: Double): Payoff {
        require(balance >= 0.0) { "balance must not be negative" }
        require(monthlyPayment > 0.0) { "monthlyPayment must be positive" }
        if (balance == 0.0) return Payoff.Clears(0.0, 0.0, 0.0)

        val i = monthlyRate(apr)
        val interest = balance * i
        if (monthlyPayment <= interest) return Payoff.NeverClears(interest)

        // An interest-free debt is just division; the log formula would divide by ln(1).
        val months = if (i == 0.0) balance / monthlyPayment
        else -ln(1.0 - (i * balance) / monthlyPayment) / ln(1.0 + i)

        val totalPaid = months * monthlyPayment
        return Payoff.Clears(months, totalPaid, totalPaid - balance)
    }

    /** The balance at the end of each month, for the chart. Stops once it clears. */
    fun balanceCurve(balance: Double, apr: Double, monthlyPayment: Double, maxMonths: Int = 360): List<Double> {
        val i = monthlyRate(apr)
        val points = ArrayList<Double>(maxMonths + 1)
        var current = balance
        points += current
        repeat(maxMonths) {
            if (current <= 0.0) return points
            current = (current * (1.0 + i) - monthlyPayment).coerceAtLeast(0.0)
            points += current
        }
        return points
    }

    /** "Pay 10 more a month and you finish X months sooner and save Y." */
    fun savingsFromPayingMore(
        balance: Double,
        apr: Double,
        monthlyPayment: Double,
        extra: Double,
    ): Pair<Double, Double>? {
        val now = payoff(balance, apr, monthlyPayment) as? Payoff.Clears ?: return null
        val better = payoff(balance, apr, monthlyPayment + extra) as? Payoff.Clears ?: return null
        return (now.months - better.months) to (now.totalInterest - better.totalInterest)
    }
}
