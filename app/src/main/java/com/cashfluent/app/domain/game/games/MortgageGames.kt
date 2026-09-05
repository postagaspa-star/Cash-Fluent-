package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Mortgage
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import com.cashfluent.app.domain.game.trueFalse
import kotlin.math.abs
import kotlin.random.Random

/** Topic 10, rent or buy. The payment, what owning really costs, and how long it takes to win. */
object MortgageGames {

    private const val TOPIC = "rent-vs-buy"

    private class Home(random: Random) {
        val price = random.pick(listOf(150_000.0, 200_000.0, 250_000.0, 300_000.0))
        val deposit = random.pick(listOf(0.10, 0.20))
        val rate = random.pick(listOf(0.03, 0.04, 0.045, 0.05, 0.06))
        val term = random.pick(listOf(25, 30))
        val cost = Mortgage.cost(price, deposit, rate, term)
        val principal = price - cost.deposit
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "mortgage-payment", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The monthly payment",
            blurb = "Price, deposit, rate, years. Run the annuity formula backwards.",
            deal = ::payment,
        ),
        MiniGame(
            id = "mortgage-true-cost", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What owning costs",
            blurb = "The payment is not the cost. Add what breaks.",
            deal = ::trueCost,
        ),
        MiniGame(
            id = "mortgage-day-one", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Cash on day one",
            blurb = "Deposit plus fees. The money that has to exist before the first payment.",
            deal = ::dayOne,
        ),
        MiniGame(
            id = "mortgage-repaid", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Five years in",
            blurb = "Sixty payments made. How much of the loan is actually gone?",
            deal = ::repaid,
        ),
        MiniGame(
            id = "mortgage-rent-or-buy", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Rent or buy",
            blurb = "A place, a rent, a number of years. Which comes out ahead?",
            deal = ::rentOrBuy,
        ),
        MiniGame(
            id = "mortgage-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Slogans, true or false",
            blurb = "The things people say about renting and buying, checked against the arithmetic.",
            deal = ::claims,
        ),
    )

    private fun payment(random: Random): Round {
        val h = Home(random)
        return numberRound(
            prompt = "A ${money(h.price)} place with ${pct(h.deposit)} down: you borrow ${money(h.principal)} at " +
                "${pct(h.rate, 1)} over ${h.term} years. What is the monthly payment?",
            quantity = Quantity.AMOUNT,
            truth = h.cost.monthlyPayment,
            random = random,
            explanation = "i = ${num(h.rate, 3)} ÷ 12 = ${num(Mortgage.monthlyRate(h.rate), 5)}, n = ${h.term * 12}. " +
                "M = ${num(h.principal)} × [i(1+i)ⁿ] ÷ [(1+i)ⁿ − 1] = ${money(h.cost.monthlyPayment, 2)}.",
        )
    }

    private fun trueCost(random: Random): Round {
        val h = Home(random)
        return numberRound(
            prompt = "The payment on a ${money(h.price)} place is ${money(h.cost.monthlyPayment)} a month. Add upkeep at " +
                "1% of the price a year. What does owning cost per month?",
            quantity = Quantity.AMOUNT,
            truth = h.cost.monthlyTotal,
            random = random,
            explanation = "1% of ${num(h.price)} is ${money(h.price * Mortgage.MAINTENANCE_RATE)} a year, " +
                "${money(h.cost.monthlyMaintenance)} a month. ${num(h.cost.monthlyPayment)} + " +
                "${num(h.cost.monthlyMaintenance)} = ${money(h.cost.monthlyTotal)}.",
        )
    }

    private fun dayOne(random: Random): Round {
        val h = Home(random)
        return numberRound(
            prompt = "${money(h.price)}, ${pct(h.deposit)} deposit, and buying costs about 4% of the price in fees. How " +
                "much cash has to exist on day one?",
            quantity = Quantity.AMOUNT,
            truth = h.cost.upfront,
            random = random,
            explanation = "Deposit ${money(h.cost.deposit)} plus fees ${money(h.cost.purchaseFees)} = ${money(h.cost.upfront)}. " +
                "The deposit is still yours; the fees are gone the day you sign.",
        )
    }

    private fun repaid(random: Random): Round {
        val h = Home(random)
        val owed = Mortgage.balanceAfter(h.principal, h.rate, h.term, months = 60)
        val repaid = h.principal - owed
        val paid = h.cost.monthlyPayment * 60
        val naive = h.principal * 5 / h.term
        val candidates = listOf(repaid, paid, naive).shuffled(random)
        return ChoiceRound(
            prompt = "Five years of paying ${money(h.cost.monthlyPayment)} a month on ${money(h.principal)} at " +
                "${pct(h.rate, 1)} over ${h.term} years. How much of the loan is repaid?",
            options = candidates.map { money(it) },
            correctIndex = candidates.indexOf(repaid),
            explanation = "You handed over ${money(paid)}, but ${money(paid - repaid)} of it was interest. Only " +
                "${money(repaid)} came off the ${money(h.principal)} — early payments are mostly interest.",
        )
    }

    private fun rentOrBuy(random: Random): Round {
        val h = Home(random)
        val years = random.pick(listOf(2, 5, 15))
        val rent = roundTo(h.price * random.pick(listOf(0.0045, 0.005, 0.0055)), 25.0)
        val comparison = Mortgage.compare(h.price, h.deposit, h.rate, h.term, rent, years)
        val draw = abs(comparison.net) < h.cost.monthlyTotal
        return ChoiceRound(
            prompt = "${money(h.price)} to buy with ${pct(h.deposit)} down at ${pct(h.rate, 1)}, or ${money(rent)} a month " +
                "to rent the same place. You stay $years years. Which comes out ahead?",
            options = listOf("Renting", "Buying", "A draw, within a month's payment"),
            correctIndex = when {
                draw -> 2
                comparison.net > 0.0 -> 1
                else -> 0
            },
            explanation = "Owning: ${money(comparison.ownCashOut)} out, minus the ${money(comparison.equity)} you now own = " +
                "${money(comparison.ownCashOut - comparison.equity)}. Renting: ${money(comparison.rentCashOut)}. " +
                when {
                    draw -> "Within a month's payment of each other — not a money question."
                    comparison.net > 0.0 -> "Buying is ahead by ${money(comparison.net)}."
                    else -> "Renting is ahead by ${money(-comparison.net)} — not long enough to earn back the cost of getting in."
                },
        )
    }

    private fun claims(random: Random): Round {
        val h = Home(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "The monthly payment of ${money(h.cost.monthlyPayment)} is what owning a ${money(h.price)} place costs " +
                    "each month.",
                isTrue = false,
                explanation = "The lender's payment is only part of it. Upkeep at 1% a year adds " +
                    "${money(h.cost.monthlyMaintenance)} a month: owning costs about ${money(h.cost.monthlyTotal)}, and " +
                    "${money(h.cost.purchaseFees)} of fees were gone on day one.",
            )
            1 -> {
                val owed = Mortgage.balanceAfter(h.principal, h.rate, h.term, months = 60)
                val repaid = h.principal - owed
                val claimed = roundTo(repaid * random.pick(listOf(0.5, 2.0)), 1_000.0)
                trueFalse(
                    prompt = "After five years of paying ${money(h.cost.monthlyPayment)} a month on ${money(h.principal)}, " +
                        "more than ${money(claimed)} of the loan has been repaid.",
                    isTrue = repaid > claimed,
                    explanation = "Sixty payments come to ${money(h.cost.monthlyPayment * 60)}, but only ${money(repaid)} " +
                        "came off the loan; the rest was interest. ${if (repaid > claimed) "More" else "Less"} than ${money(claimed)}.",
                )
            }
            else -> trueFalse(
                prompt = "Putting ${money(h.cost.upfront)} into a deposit and fees costs you only the fees, because the " +
                    "deposit is still yours.",
                isTrue = false,
                explanation = "The fees are gone; the deposit is committed, and money committed to one thing cannot " +
                    "earn in another. At 5% the whole ${money(h.cost.upfront)} would have earned about " +
                    "${money(h.cost.upfront * (Math.pow(1.05, 5.0) - 1))} over five years.",
            )
        }
    }
}
