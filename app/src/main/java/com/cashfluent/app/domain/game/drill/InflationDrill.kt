package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Inflation
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
import kotlin.math.pow
import kotlin.random.Random

/** Module 04. The number the bank shows, against what it buys. */
object InflationDrill : Drill {

    override val moduleId = "inflation"

    /** Account rate against inflation; every pair loses a different amount. */
    private val pairs = listOf(0.0 to 0.02, 0.01 to 0.04, 0.02 to 0.03, 0.03 to 0.06, 0.005 to 0.03)

    override fun round(random: Random, index: Int): Round {
        val amount = random.pick(listOf(1_000.0, 3_000.0, 5_000.0, 10_000.0))
        val rate = random.pick(listOf(0.0, 0.005, 0.01, 0.02))
        val inflation = random.pick(listOf(0.03, 0.04, 0.05))
        val years = random.pick(listOf(3, 5, 10))
        val nominal = Inflation.nominalValue(amount, rate, years)
        val real = Inflation.realValueAfter(amount, rate, inflation, years)
        val lost = Inflation.purchasingPowerLost(amount, rate, inflation, years)
        val priceFactor = (1 + inflation).pow(years)

        return when (index) {
            0 -> numberRound(
                prompt = "${money(amount)} sits in an account paying ${pct(rate, 1)} for $years years, while " +
                    "prices rise ${pct(inflation)} a year. What is it worth at the end, in today's money?",
                quantity = Quantity.AMOUNT,
                truth = real,
                random = random,
                explanation = "The balance says ${money(nominal)}: ${num(amount)} × ${num(1 + rate, 3)}^$years. " +
                    "Prices rose ${num(1 + inflation, 2)}^$years = ${num(priceFactor, 3)} times. " +
                    "${num(nominal)} ÷ ${num(priceFactor, 3)} = ${money(real)}.",
            )

            1 -> {
                val r = random.pick(listOf(0.005, 0.01, 0.02, 0.03))
                val p = random.pick(listOf(0.02, 0.03, 0.04, 0.05, 0.06))
                val realReturn = Inflation.realReturn(r, p)
                NumberRound(
                    prompt = "Your account pays ${pct(r, 1)} and inflation is ${pct(p)}. What is your real " +
                        "return — what the money gains or loses in buying power each year?",
                    quantity = Quantity.PERCENT_PRECISE,
                    truth = realReturn,
                    min = -0.08, max = 0.08, step = 0.001,
                    explanation = "(1 + ${num(r, 3)}) ÷ (1 + ${num(p, 2)}) − 1 = ${pct(realReturn, 2)}. The " +
                        "shortcut r − π gives ${pct(r - p, 1)}, close enough for a guess.",
                )
            }

            2 -> {
                val chosen = pairs.shuffled(random).take(2)
                val lostShare = chosen.map { (r, p) -> 1.0 - Inflation.realValueAfter(1.0, r, p, years) }
                ChoiceRound(
                    prompt = "Which of these loses less buying power over $years years?",
                    options = chosen.map { (r, p) ->
                        "An account paying ${pct(r, 1)} while prices rise ${pct(p)}"
                    },
                    correctIndex = lostShare.indexOf(lostShare.min()),
                    explanation = "The first loses ${pct(lostShare[0], 1)} of its buying power, the second " +
                        "${pct(lostShare[1], 1)}. What matters is the gap between the two rates, not the " +
                        "account's rate on its own.",
                )
            }

            3 -> numberRound(
                prompt = "${money(amount)} at ${pct(rate, 1)} for $years years, with inflation at " +
                    "${pct(inflation)}. How much buying power does it lose?",
                quantity = Quantity.AMOUNT,
                truth = lost,
                random = random,
                explanation = "Worth ${money(real)} in today's money against the ${money(amount)} put in: " +
                    "${money(lost)} of buying power gone, while the balance read ${money(nominal)}.",
            )

            else -> {
                val p = random.pick(listOf(0.02, 0.03, 0.04, 0.05))
                var n = 1
                while (Inflation.realValue(1_000.0, p, n) > 750.0) n++
                val candidates = listOf(n, n / 2, n * 2).shuffled(random)
                ChoiceRound(
                    prompt = "Prices rise ${pct(p)} a year. After how many years does ${money(1_000.0)} in a " +
                        "0% account buy only what ${money(750.0)} buys today?",
                    options = candidates.map { "$it years" },
                    correctIndex = candidates.indexOf(n),
                    explanation = "1,000 ÷ ${num(1 + p, 2)}^$n = ${money(Inflation.realValue(1_000.0, p, n))}, " +
                        "the first year at or below 750. A quarter of the buying power goes in about $n " +
                        "years at ${pct(p)} — with no alert, and no line on any statement.",
                )
            }
        }
    }
}
