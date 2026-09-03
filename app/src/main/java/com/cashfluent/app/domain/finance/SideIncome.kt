package com.cashfluent.app.domain.finance

/**
 * Module 09 — The bill nobody deducts for you.
 *
 *   profit    = income - allowable expenses
 *   tax due   = profit x combined rate
 *   set aside = each payment x combined rate
 *
 * An employer takes tax out before the money reaches you. Nobody does that for a gig,
 * a commission or a weekend job invoiced by you — so the whole amount lands in your
 * account and looks like yours, and the bill turns up a year later.
 *
 * One combined rate stands in for income tax plus whatever social contributions apply.
 * Splitting it into real bands would make the module about one country's tax code
 * instead of about the habit, which is the part that transfers.
 */
data class SideIncomeYear(
    val gross: Double,
    val expenses: Double,
    val profit: Double,
    val taxDue: Double,
    val keep: Double,
) {
    /** Tax as a share of the money that actually arrived — always below the headline rate. */
    val effectiveRateOnGross: Double get() = if (gross == 0.0) 0.0 else taxDue / gross
}

object SideIncome {

    fun profit(gross: Double, expenses: Double): Double = (gross - expenses).coerceAtLeast(0.0)

    fun taxDue(gross: Double, expenses: Double, rate: Double): Double = profit(gross, expenses) * rate

    fun year(monthlyIncome: Double, yearlyExpenses: Double, rate: Double): SideIncomeYear {
        require(monthlyIncome >= 0.0) { "income must not be negative" }
        require(yearlyExpenses >= 0.0) { "expenses must not be negative" }
        require(rate in 0.0..1.0) { "a tax rate is a fraction between 0 and 1" }
        val gross = monthlyIncome * 12
        val due = taxDue(gross, yearlyExpenses, rate)
        return SideIncomeYear(
            gross = gross,
            expenses = yearlyExpenses,
            profit = profit(gross, yearlyExpenses),
            taxDue = due,
            keep = gross - yearlyExpenses - due,
        )
    }

    /** The safe habit: hold back the flat rate on everything that arrives, expenses ignored. */
    fun flatSetAside(payment: Double, rate: Double): Double = payment * rate

    /**
     * What holding back the flat rate all year leaves over once the real bill arrives.
     *
     * Positive is the point: expenses make the true bill smaller than the flat rate, so
     * the simple habit over-collects and the surplus is yours.
     */
    fun cushion(monthlyIncome: Double, yearlyExpenses: Double, rate: Double): Double =
        flatSetAside(monthlyIncome, rate) * 12 - taxDue(monthlyIncome * 12, yearlyExpenses, rate)
}
