package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Mortgage
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import kotlin.math.abs
import kotlin.random.Random

/** Module 10. The payment, what owning really costs, and how long it takes to win. */
object MortgageDrill : Drill {

    override val moduleId = "rent-vs-buy"

    override fun round(random: Random, index: Int): Round {
        val price = random.pick(listOf(150_000.0, 200_000.0, 250_000.0, 300_000.0))
        val deposit = random.pick(listOf(0.10, 0.20))
        val rate = random.pick(listOf(0.03, 0.04, 0.045, 0.05, 0.06))
        val term = random.pick(listOf(25, 30))
        val cost = Mortgage.cost(price, deposit, rate, term)
        val principal = price - cost.deposit

        return when (index) {
            0 -> numberRound(
                prompt = "A ${money(price)} place with ${pct(deposit)} down: you borrow ${money(principal)} at " +
                    "${pct(rate, 1)} over $term years. What is the monthly payment?",
                quantity = Quantity.AMOUNT,
                truth = cost.monthlyPayment,
                random = random,
                explanation = "i = ${num(rate, 3)} ÷ 12 = ${num(Mortgage.monthlyRate(rate), 5)}, n = ${term * 12}. " +
                    "M = ${num(principal)} × [i(1+i)ⁿ] ÷ [(1+i)ⁿ − 1] = ${money(cost.monthlyPayment, 2)}.",
            )

            1 -> numberRound(
                prompt = "The payment on a ${money(price)} place is ${money(cost.monthlyPayment)} a month. Add " +
                    "upkeep at 1% of the price a year. What does owning cost per month?",
                quantity = Quantity.AMOUNT,
                truth = cost.monthlyTotal,
                random = random,
                explanation = "1% of ${num(price)} is ${money(price * Mortgage.MAINTENANCE_RATE)} a year, " +
                    "${money(cost.monthlyMaintenance)} a month. ${num(cost.monthlyPayment)} + " +
                    "${num(cost.monthlyMaintenance)} = ${money(cost.monthlyTotal)}.",
            )

            2 -> numberRound(
                prompt = "${money(price)}, ${pct(deposit)} deposit, and buying costs about 4% of the price in " +
                    "fees. How much cash has to exist on day one?",
                quantity = Quantity.AMOUNT,
                truth = cost.upfront,
                random = random,
                explanation = "Deposit ${money(cost.deposit)} plus fees ${money(cost.purchaseFees)} = " +
                    "${money(cost.upfront)}. The deposit is still yours; the fees are gone the day you sign.",
            )

            3 -> {
                val owed = Mortgage.balanceAfter(principal, rate, term, months = 60)
                val repaid = principal - owed
                val paid = cost.monthlyPayment * 60
                val naive = principal * 5 / term
                val candidates = listOf(repaid, paid, naive).shuffled(random)
                ChoiceRound(
                    prompt = "Five years of paying ${money(cost.monthlyPayment)} a month on ${money(principal)} at " +
                        "${pct(rate, 1)} over $term years. How much of the loan is repaid?",
                    options = candidates.map { money(it) },
                    correctIndex = candidates.indexOf(repaid),
                    explanation = "You handed over ${money(paid)}, but ${money(paid - repaid)} of it was interest. " +
                        "Only ${money(repaid)} came off the ${money(principal)} — early payments are mostly " +
                        "interest.",
                )
            }

            else -> {
                val years = random.pick(listOf(2, 5, 15))
                val rent = roundTo(price * random.pick(listOf(0.0045, 0.005, 0.0055)), 25.0)
                val comparison = Mortgage.compare(price, deposit, rate, term, rent, years)
                val draw = abs(comparison.net) < cost.monthlyTotal
                ChoiceRound(
                    prompt = "${money(price)} to buy with ${pct(deposit)} down at ${pct(rate, 1)}, or ${money(rent)} " +
                        "a month to rent the same place. You stay $years years. Which comes out ahead?",
                    options = listOf("Renting", "Buying", "A draw, within a month's payment"),
                    correctIndex = when {
                        draw -> 2
                        comparison.net > 0.0 -> 1
                        else -> 0
                    },
                    explanation = "Owning: ${money(comparison.ownCashOut)} out, minus the ${money(comparison.equity)} " +
                        "you now own = ${money(comparison.ownCashOut - comparison.equity)}. Renting: " +
                        "${money(comparison.rentCashOut)}. " +
                        when {
                            draw -> "Within a month's payment of each other — not a money question."
                            comparison.net > 0.0 -> "Buying is ahead by ${money(comparison.net)}."
                            else -> "Renting is ahead by ${money(-comparison.net)} — not long enough to earn " +
                                "back the cost of getting in."
                        },
                )
            }
        }
    }
}
