package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Inflation
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.higherLower
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.trueFalse
import kotlin.math.pow
import kotlin.random.Random

/** Topic 04, saving versus investing. The number the bank shows, against what it buys. */
object InflationGames {

    private const val TOPIC = "inflation"

    /** Account rate against inflation; every pair loses a different amount. */
    private val pairs = listOf(0.0 to 0.02, 0.01 to 0.04, 0.02 to 0.03, 0.03 to 0.06, 0.005 to 0.03)

    private class Pot(random: Random) {
        val amount = random.pick(listOf(1_000.0, 3_000.0, 5_000.0, 10_000.0))
        val rate = random.pick(listOf(0.0, 0.005, 0.01, 0.02))
        val inflation = random.pick(listOf(0.03, 0.04, 0.05))
        val years = random.pick(listOf(3, 5, 10))
        val nominal = Inflation.nominalValue(amount, rate, years)
        val real = Inflation.realValueAfter(amount, rate, inflation, years)
        val lost = Inflation.purchasingPowerLost(amount, rate, inflation, years)
        val priceFactor = (1 + inflation).pow(years)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "inflation-real-value", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "In today's money",
            blurb = "The balance grew. What does it actually buy at the end?",
            deal = ::realValue,
        ),
        MiniGame(
            id = "inflation-real-return", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Real return",
            blurb = "Your rate against inflation. What the money really gains or loses a year.",
            deal = ::realReturn,
        ),
        MiniGame(
            id = "inflation-which-loses-less", topicId = TOPIC, mechanic = Mechanic.HIGHER_LOWER,
            title = "Which loses less",
            blurb = "Two accounts, two inflation rates. It's the gap that matters.",
            deal = ::whichLosesLess,
        ),
        MiniGame(
            id = "inflation-power-lost", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Buying power gone",
            blurb = "How much a pot quietly lost while the balance went up.",
            deal = ::powerLost,
        ),
        MiniGame(
            id = "inflation-years-to-750", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "A quarter gone",
            blurb = "How many years until 1,000 buys what 750 buys today?",
            deal = ::yearsToQuarterGone,
        ),
        MiniGame(
            id = "inflation-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Cash claims",
            blurb = "Statements about savings and prices. The exact formula decides.",
            deal = ::claims,
        ),
    )

    private fun realValue(random: Random): Round {
        val p = Pot(random)
        return numberRound(
            prompt = "${money(p.amount)} sits in an account paying ${pct(p.rate, 1)} for ${p.years} years, while " +
                "prices rise ${pct(p.inflation)} a year. What is it worth at the end, in today's money?",
            quantity = Quantity.AMOUNT,
            truth = p.real,
            random = random,
            explanation = "The balance says ${money(p.nominal)}: ${num(p.amount)} × ${num(1 + p.rate, 3)}^${p.years}. " +
                "Prices rose ${num(1 + p.inflation, 2)}^${p.years} = ${num(p.priceFactor, 3)} times. " +
                "${num(p.nominal)} ÷ ${num(p.priceFactor, 3)} = ${money(p.real)}.",
        )
    }

    private fun realReturn(random: Random): Round {
        val r = random.pick(listOf(0.005, 0.01, 0.02, 0.03))
        val p = random.pick(listOf(0.02, 0.03, 0.04, 0.05, 0.06))
        val realReturn = Inflation.realReturn(r, p)
        return NumberRound(
            prompt = "Your account pays ${pct(r, 1)} and inflation is ${pct(p)}. What is your real return — what the " +
                "money gains or loses in buying power each year?",
            quantity = Quantity.PERCENT_PRECISE,
            truth = realReturn,
            min = -0.08, max = 0.08, step = 0.001,
            explanation = "(1 + ${num(r, 3)}) ÷ (1 + ${num(p, 2)}) − 1 = ${pct(realReturn, 2)}. The shortcut r − π " +
                "gives ${pct(r - p, 1)}, close enough for a guess.",
        )
    }

    private fun whichLosesLess(random: Random): Round {
        val years = random.pick(listOf(3, 5, 10))
        val chosen = pairs.shuffled(random).take(2)
        val lostShare = chosen.map { (r, p) -> 1.0 - Inflation.realValueAfter(1.0, r, p, years) }
        return higherLower(
            prompt = "Which of these loses less buying power over $years years?",
            first = "An account paying ${pct(chosen[0].first, 1)} while prices rise ${pct(chosen[0].second)}",
            second = "An account paying ${pct(chosen[1].first, 1)} while prices rise ${pct(chosen[1].second)}",
            firstIsHigher = lostShare[0] <= lostShare[1],
            explanation = "The first loses ${pct(lostShare[0], 1)} of its buying power, the second " +
                "${pct(lostShare[1], 1)}. What matters is the gap between the two rates, not the account's rate " +
                "on its own.",
        )
    }

    private fun powerLost(random: Random): Round {
        val p = Pot(random)
        return numberRound(
            prompt = "${money(p.amount)} at ${pct(p.rate, 1)} for ${p.years} years, with inflation at " +
                "${pct(p.inflation)}. How much buying power does it lose?",
            quantity = Quantity.AMOUNT,
            truth = p.lost,
            random = random,
            explanation = "Worth ${money(p.real)} in today's money against the ${money(p.amount)} put in: " +
                "${money(p.lost)} of buying power gone, while the balance read ${money(p.nominal)}.",
        )
    }

    private fun yearsToQuarterGone(random: Random): Round {
        val p = random.pick(listOf(0.02, 0.03, 0.04, 0.05))
        var n = 1
        while (Inflation.realValue(1_000.0, p, n) > 750.0) n++
        val candidates = listOf(n, n / 2, n * 2).shuffled(random)
        return ChoiceRound(
            prompt = "Prices rise ${pct(p)} a year. After how many years does ${money(1_000.0)} in a 0% account buy " +
                "only what ${money(750.0)} buys today?",
            options = candidates.map { "$it years" },
            correctIndex = candidates.indexOf(n),
            explanation = "1,000 ÷ ${num(1 + p, 2)}^$n = ${money(Inflation.realValue(1_000.0, p, n))}, the first year " +
                "at or below 750. A quarter of the buying power goes in about $n years at ${pct(p)} — with no " +
                "alert, and no line on any statement.",
        )
    }

    private fun claims(random: Random): Round {
        val p = Pot(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "${money(p.amount)} in an account at ${pct(p.rate, 1)} with inflation at ${pct(p.inflation)}: " +
                    "after ${p.years} years the balance is higher, so you are better off.",
                isTrue = false,
                explanation = "The balance reads ${money(p.nominal)}, but it buys what ${money(p.real)} buys today. " +
                    "A rising balance and rising value are not the same thing.",
            )
            1 -> {
                val r = random.pick(listOf(0.01, 0.02, 0.03, 0.04))
                val i = random.pick(listOf(0.02, 0.03, 0.04))
                val ahead = r > i
                trueFalse(
                    prompt = "An account paying ${pct(r)} while inflation runs at ${pct(i)} is gaining buying power.",
                    isTrue = ahead,
                    explanation = "Real return: (1 + ${num(r, 2)}) ÷ (1 + ${num(i, 2)}) − 1 = " +
                        "${pct(Inflation.realReturn(r, i), 2)} a year. ${if (ahead) "Gaining, just." else if (r == i) "Standing still." else "Losing."}",
                )
            }
            else -> trueFalse(
                prompt = "An emergency fund of ${money(p.amount)} should be invested, so that it at least keeps up " +
                    "with ${pct(p.inflation)} inflation.",
                isTrue = false,
                explanation = "That money has one job: being there in full on a bad day. Investments can be down " +
                    "exactly when you need them. Losing about ${money(p.lost)} of buying power over ${p.years} years " +
                    "is the price of that, and it is worth paying.",
            )
        }
    }
}
