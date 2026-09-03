package com.cashfluent.app.domain.finance

import kotlin.math.pow

/**
 * Module 10 — Rent or buy.
 *
 *   M = P x [ i(1+i)^n ] / [ (1+i)^n - 1 ]
 *
 * The same annuity formula as module 02, run the other way round: there it was what a
 * repeated payment grows into, here it is the repeated payment a lump sum requires.
 *
 * The comparison the module is really about is not M against rent. It is everything
 * owning costs — the payment, the upkeep, the money handed over at the start — against
 * everything renting costs, with the part of the payment you get back counted as yours.
 */
data class OwnershipCost(
    val monthlyPayment: Double,
    val monthlyMaintenance: Double,
    val deposit: Double,
    val purchaseFees: Double,
) {
    val monthlyTotal: Double get() = monthlyPayment + monthlyMaintenance
    val upfront: Double get() = deposit + purchaseFees
}

/** Owning and renting compared over a stretch of years, in cash and in what you own. */
data class RentVsBuy(
    val years: Int,
    val ownCashOut: Double,
    val rentCashOut: Double,
    val equity: Double,
) {
    val extraCashOut: Double get() = ownCashOut - rentCashOut

    /** Positive means owning came out ahead — before the price of the home moves at all. */
    val net: Double get() = equity - extraCashOut
}

object Mortgage {

    /** The rule of thumb for upkeep: about 1% of what the place is worth, every year. */
    const val MAINTENANCE_RATE = 0.01

    fun monthlyRate(annualRate: Double): Double = annualRate / 12.0

    fun monthlyPayment(principal: Double, annualRate: Double, years: Int): Double {
        require(principal >= 0.0) { "principal must not be negative" }
        require(years > 0) { "a term needs at least one year" }
        val i = monthlyRate(annualRate)
        val n = years * 12
        // A 0% loan is the price divided by the number of payments; the formula would
        // divide by zero.
        if (i == 0.0) return principal / n
        val growth = (1.0 + i).pow(n)
        return principal * (i * growth) / (growth - 1.0)
    }

    fun totalRepaid(principal: Double, annualRate: Double, years: Int): Double =
        monthlyPayment(principal, annualRate, years) * years * 12

    fun totalInterest(principal: Double, annualRate: Double, years: Int): Double =
        totalRepaid(principal, annualRate, years) - principal

    fun cost(
        price: Double,
        depositFraction: Double,
        annualRate: Double,
        years: Int,
        feeFraction: Double = 0.04,
        maintenanceRate: Double = MAINTENANCE_RATE,
    ): OwnershipCost {
        val deposit = price * depositFraction
        return OwnershipCost(
            monthlyPayment = monthlyPayment(price - deposit, annualRate, years),
            monthlyMaintenance = price * maintenanceRate / 12.0,
            deposit = deposit,
            purchaseFees = price * feeFraction,
        )
    }

    /** What is still owed after [months] of paying, month by month rather than by formula. */
    fun balanceAfter(principal: Double, annualRate: Double, years: Int, months: Int): Double {
        val i = monthlyRate(annualRate)
        val payment = monthlyPayment(principal, annualRate, years)
        var balance = principal
        repeat(months) {
            balance = (balance * (1.0 + i) - payment).coerceAtLeast(0.0)
        }
        return balance
    }

    fun compare(
        price: Double,
        depositFraction: Double,
        annualRate: Double,
        termYears: Int,
        monthlyRent: Double,
        overYears: Int,
        feeFraction: Double = 0.04,
        maintenanceRate: Double = MAINTENANCE_RATE,
    ): RentVsBuy {
        val cost = cost(price, depositFraction, annualRate, termYears, feeFraction, maintenanceRate)
        val months = overYears * 12
        val principal = price - cost.deposit
        val owed = balanceAfter(principal, annualRate, termYears, months)
        return RentVsBuy(
            years = overYears,
            ownCashOut = cost.monthlyTotal * months + cost.upfront,
            rentCashOut = monthlyRent * months,
            equity = cost.deposit + (principal - owed),
        )
    }
}
