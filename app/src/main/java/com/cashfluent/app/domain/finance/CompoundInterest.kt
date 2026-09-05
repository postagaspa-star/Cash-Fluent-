package com.cashfluent.app.domain.finance

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Module 02 — Why time beats the amount.
 *
 *   FV = PV x (1 + r)^n            a sum left alone
 *   FV = PMT x [((1 + i)^n - 1)/i] a repeated monthly deposit
 */
object CompoundInterest {

    /** What a lump sum becomes after [years] at [annualRate] (0.07 for 7%). */
    fun lumpSum(present: Double, annualRate: Double, years: Int): Double {
        require(years >= 0) { "years must not be negative" }
        return present * (1.0 + annualRate).pow(years)
    }

    /** What [monthlyAmount] deposited every month for [months] becomes. */
    fun monthlyPayments(monthlyAmount: Double, annualRate: Double, months: Int): Double {
        if (months <= 0) return 0.0
        val i = annualRate / 12.0
        // A zero rate is a legitimate slider position, not an error: it degenerates to
        // plain addition, and the formula would divide by zero.
        if (i == 0.0) return monthlyAmount * months
        return monthlyAmount * (((1.0 + i).pow(months) - 1.0) / i)
    }
}

/** One point on the simulator's chart. */
data class AgePoint(val age: Int, val contributed: Double, val value: Double)

/**
 * Pay [monthlyAmount] every month from [startAge] until [stopAge], then stop and leave
 * it alone. This is exactly the Alex-and-Sam comparison from module 02.
 */
data class SavingPlan(
    val monthlyAmount: Double,
    val startAge: Int,
    val stopAge: Int,
    val annualRate: Double,
) {
    init {
        require(stopAge >= startAge) { "stopAge must not be before startAge" }
        require(monthlyAmount >= 0.0) { "monthlyAmount must not be negative" }
    }

    val monthsPaying: Int get() = (stopAge - startAge) * 12

    val totalContributed: Double get() = monthlyAmount * monthsPaying

    /** What you have put in by [age] — flat once you stop paying. */
    fun contributedBy(age: Int): Double =
        monthlyAmount * max(0, min(age, stopAge) - startAge) * 12

    /** What it is worth at [age], including the years after you stopped paying. */
    fun valueAt(age: Int): Double {
        if (age <= startAge) return 0.0
        val payingMonths = (min(age, stopAge) - startAge) * 12
        val atStop = CompoundInterest.monthlyPayments(monthlyAmount, annualRate, payingMonths)
        return CompoundInterest.lumpSum(atStop, annualRate, max(0, age - stopAge))
    }

    /** Everything the chart needs, one point per year. */
    fun curve(toAge: Int): List<AgePoint> =
        (startAge..max(startAge, toAge)).map { AgePoint(it, contributedBy(it), valueAt(it)) }
}
