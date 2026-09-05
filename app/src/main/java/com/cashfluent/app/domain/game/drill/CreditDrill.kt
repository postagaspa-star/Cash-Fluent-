package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Credit
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.NumberRound
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

/** Module 08. The ratio, the payment that moves it, and the day the photograph is taken. */
object CreditDrill : Drill {

    override val moduleId = "credit-record"

    override fun round(random: Random, index: Int): Round {
        val limit = random.pick(listOf(500.0, 1_000.0, 1_500.0, 2_000.0, 3_000.0))
        val balance = roundTo(limit * random.pick(listOf(0.45, 0.6, 0.75)), 10.0)
        val utilisation = Credit.utilisation(balance, limit)
        val ceiling = Credit.balanceCeiling(limit)

        return when (index) {
            0 -> NumberRound(
                prompt = "${money(balance)} is on the card when the statement is cut, on a ${money(limit)} " +
                    "limit. What utilisation gets recorded?",
                quantity = Quantity.PERCENT,
                truth = utilisation,
                min = 0.0, max = 1.0, step = 0.01,
                explanation = "${num(balance)} ÷ ${num(limit)} = ${pct(utilisation)}. Above 30% reads as heavy " +
                    "use, however promptly you pay afterwards.",
            )

            1 -> {
                val pay = Credit.paymentToReach(balance, limit)
                numberRound(
                    prompt = "${money(balance)} on a ${money(limit)} limit. How much do you pay before the " +
                        "statement date to be recorded at 30%?",
                    quantity = Quantity.AMOUNT,
                    truth = pay,
                    random = random,
                    explanation = "30% of ${num(limit)} is ${money(ceiling)}. ${num(balance)} − ${num(ceiling)} = " +
                        "${money(pay)}, paid before the statement is cut — not before the bill is due.",
                )
            }

            2 -> {
                val unused = random.pick(listOf(300.0, 500.0, 1_000.0))
                var pay = random.pick(listOf(100.0, 150.0, 200.0))
                val extra = random.pick(listOf(500.0, 1_000.0))
                val now = Credit.utilisation(balance, limit + unused)
                var paying = Credit.utilisation(balance - pay, limit + unused)
                val bigger = Credit.utilisationWithExtraLimit(balance, limit + unused, extra)
                if (abs(paying - bigger) < 1e-9) {
                    pay += 10.0
                    paying = Credit.utilisation(balance - pay, limit + unused)
                }
                val closing = Credit.utilisation(balance, limit)
                val results = listOf(paying, bigger, closing)
                ChoiceRound(
                    prompt = "Two cards: a ${money(limit)} limit with ${money(balance)} on it, and a " +
                        "${money(unused)} card you never use. Utilisation now: ${pct(now)}. Which move gets " +
                        "it lowest?",
                    options = listOf(
                        "Pay ${money(pay)} before the statement",
                        "Open a third card with a ${money(extra)} limit",
                        "Close the unused ${money(unused)} card",
                    ),
                    correctIndex = results.indexOf(results.min()),
                    explanation = "Paying: ${num(balance - pay)} ÷ ${num(limit + unused)} = ${pct(paying, 1)}. A " +
                        "bigger total limit: ${num(balance)} ÷ ${num(limit + unused + extra)} = " +
                        "${pct(bigger, 1)}. Closing the unused card: ${num(balance)} ÷ ${num(limit)} = " +
                        "${pct(closing, 1)} — the limit that vanished was doing you a favour.",
                )
            }

            3 -> NumberRound(
                prompt = "A ${money(limit)} limit. What is the biggest balance that still reads as 30% when " +
                    "the statement is cut?",
                quantity = Quantity.AMOUNT,
                truth = ceiling,
                min = 0.0, max = limit, step = limit / 100,
                explanation = "${num(limit)} × 0.30 = ${money(ceiling)}. Anything above it on statement day, and " +
                    "the recorded ratio is past the line lenders look for.",
            )

            else -> ChoiceRound(
                prompt = "The statement is cut on the 5th with ${money(balance)} on a ${money(limit)} limit. " +
                    "You pay all of it on the 20th, days before it's due. What ratio does the file record?",
                options = listOf(
                    "0% — it was paid in full",
                    pct(utilisation),
                    "Somewhere between the two, averaged over the month",
                ),
                correctIndex = 1,
                explanation = "The file records a snapshot from the 5th: ${num(balance)} ÷ ${num(limit)} = " +
                    "${pct(utilisation)}. Paying on the 20th protects you from interest and from a late " +
                    "mark; it does not change a photograph already taken.",
            )
        }
    }
}
