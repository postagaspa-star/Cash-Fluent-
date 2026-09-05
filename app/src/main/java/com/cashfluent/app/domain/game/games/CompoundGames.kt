package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.CompoundInterest
import com.cashfluent.app.domain.finance.SavingPlan
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
import kotlin.random.Random

/** Topic 02, compound interest. The Alex-and-Sam comparison, dealt with fresh numbers. */
object CompoundGames {

    private const val TOPIC = "compound-interest"
    private const val HORIZON = 58

    private class Plan(random: Random) {
        val monthly = random.pick(listOf(50.0, 100.0, 150.0, 200.0))
        val start = random.pick(listOf(18, 20, 22, 25))
        val years = random.pick(listOf(5, 10, 15))
        val rate = random.pick(listOf(0.05, 0.06, 0.07, 0.08))
        val plan = SavingPlan(monthly, start, start + years, rate)
        val stop get() = plan.stopAge
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "compound-worth-at-58", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Worth at 58",
            blurb = "A few years of paying in, then nothing. What is it worth decades later?",
            deal = ::worthAtHorizon,
        ),
        MiniGame(
            id = "compound-early-vs-late", topicId = TOPIC, mechanic = Mechanic.HIGHER_LOWER,
            title = "Early or late",
            blurb = "Less money earlier, or twice the money later. Which pile is bigger?",
            deal = ::earlyVersusLate,
        ),
        MiniGame(
            id = "compound-growth-share", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Earned, not deposited",
            blurb = "How much of the pile did the money itself put there?",
            deal = ::growthShare,
        ),
        MiniGame(
            id = "compound-doubling", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Doubling time",
            blurb = "At this rate, how many years until a sum has doubled?",
            deal = ::doublingTime,
        ),
        MiniGame(
            id = "compound-amount-vs-years", topicId = TOPIC, mechanic = Mechanic.HIGHER_LOWER,
            title = "Double the money or the years",
            blurb = "Two ways to double your effort. Only one of them is exponential.",
            deal = ::amountVersusYears,
        ),
        MiniGame(
            id = "compound-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Compound claims",
            blurb = "Statements about time and money. The formula decides.",
            deal = ::claims,
        ),
    )

    private fun worthAtHorizon(random: Random): Round {
        val p = Plan(random)
        val atStop = p.plan.valueAt(p.stop)
        val total = p.plan.valueAt(HORIZON)
        return numberRound(
            prompt = "${money(p.monthly)} a month from ${p.start} to ${p.stop} at ${pct(p.rate)}, then nothing " +
                "more, ever. What is it worth at $HORIZON?",
            quantity = Quantity.AMOUNT,
            truth = total,
            random = random,
            explanation = "By ${p.stop} the pile is ${money(atStop)}. Left alone for ${HORIZON - p.stop} more " +
                "years: ${num(atStop)} × ${num(1 + p.rate, 2)}^${HORIZON - p.stop} = ${money(total)}.",
        )
    }

    private fun earlyVersusLate(random: Random): Round {
        val p = Plan(random)
        val early = SavingPlan(p.monthly, p.start, p.start + 10, p.rate)
        val lateStart = p.start + random.pick(listOf(10, 12, 15))
        val late = SavingPlan(p.monthly * 2, lateStart, lateStart + 15, p.rate)
        val a = early.valueAt(HORIZON)
        val b = late.valueAt(HORIZON)
        return higherLower(
            prompt = "Same ${pct(p.rate)} return either way. Which is worth more at $HORIZON?",
            first = "${money(p.monthly)} a month from ${p.start} to ${p.start + 10}, then nothing",
            second = "${money(p.monthly * 2)} a month from $lateStart to ${lateStart + 15}",
            firstIsHigher = a >= b,
            explanation = "The first puts in ${money(early.totalContributed)} and reaches ${money(a)}. The second " +
                "puts in ${money(late.totalContributed)} and reaches ${money(b)}. " +
                if (a >= b) {
                    "Ten early years beat fifteen later ones at twice the amount."
                } else {
                    "Here twice the money for fifteen years wins — the head start was too short to make up for it."
                },
        )
    }

    private fun growthShare(random: Random): Round {
        val p = Plan(random)
        val value = p.plan.valueAt(p.stop)
        val growth = value - p.plan.totalContributed
        return numberRound(
            prompt = "${money(p.monthly)} a month from ${p.start} to ${p.stop} at ${pct(p.rate)}. At ${p.stop}, how " +
                "much of the pile is growth rather than your own deposits?",
            quantity = Quantity.AMOUNT,
            truth = growth,
            random = random,
            explanation = "You deposited ${money(p.plan.totalContributed)}. The pile is ${money(value)}. The " +
                "difference, ${money(growth)}, was earned by the money itself.",
        )
    }

    private fun doublingTime(random: Random): Round {
        val rate = random.pick(listOf(0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.10))
        var n = 1
        while (CompoundInterest.lumpSum(1.0, rate, n) < 2.0) n++
        return NumberRound(
            prompt = "At ${pct(rate)} a year, how many whole years until a sum left alone has doubled?",
            quantity = Quantity.YEARS,
            truth = n.toDouble(),
            min = 1.0, max = 40.0, step = 1.0,
            explanation = "${num(1 + rate, 2)}^${n - 1} = ${num(CompoundInterest.lumpSum(1.0, rate, n - 1), 2)}, still " +
                "short. ${num(1 + rate, 2)}^$n = ${num(CompoundInterest.lumpSum(1.0, rate, n), 2)}. The rule of 72 " +
                "gets close: 72 ÷ ${num(rate * 100)} ≈ ${num(72 / (rate * 100), 1)}.",
        )
    }

    private fun amountVersusYears(random: Random): Round {
        val p = Plan(random)
        val moreMoney = SavingPlan(p.monthly * 2, p.start, p.start + p.years, p.rate).valueAt(HORIZON)
        val moreYears = SavingPlan(p.monthly, p.start, p.start + p.years * 2, p.rate).valueAt(HORIZON)
        return higherLower(
            prompt = "${money(p.monthly)} a month from ${p.start} for ${p.years} years at ${pct(p.rate)}. Which adds " +
                "more by $HORIZON?",
            first = "Doubling the amount: ${money(p.monthly * 2)} a month for ${p.years} years",
            second = "Doubling the years: ${money(p.monthly)} a month for ${p.years * 2} years",
            firstIsHigher = moreMoney >= moreYears,
            explanation = "Twice the amount: ${money(moreMoney)}. Twice the years: ${money(moreYears)}. " +
                if (moreMoney >= moreYears) {
                    "Doubling the deposit doubles the result, exactly — and here that was enough."
                } else {
                    "Doubling the deposit only doubles the result. Doubling the years does more, because the " +
                        "exponent lives in the years."
                },
        )
    }

    private fun claims(random: Random): Round {
        val p = Plan(random)
        return when (random.nextInt(3)) {
            0 -> {
                val value = p.plan.valueAt(p.stop)
                val claimed = p.plan.totalContributed * random.pick(listOf(1.0, 1.5, 2.0))
                trueFalse(
                    prompt = "${money(p.monthly)} a month for ${p.years} years at ${pct(p.rate)} ends above " +
                        "${money(claimed)}.",
                    isTrue = value > claimed,
                    explanation = "The pile at ${p.stop} is ${money(value)}, on ${money(p.plan.totalContributed)} " +
                        "deposited. ${if (value > claimed) "Above" else "Not above"} ${money(claimed)}.",
                )
            }
            1 -> {
                val once = p.plan.valueAt(p.stop)
                trueFalse(
                    prompt = "${money(p.monthly)} a month for ${p.years} years at ${pct(p.rate)}: doubling the " +
                        "monthly amount to ${money(p.monthly * 2)} exactly doubles the final pile.",
                    isTrue = true,
                    explanation = "${money(p.monthly)} a month reaches ${money(once)}; ${money(p.monthly * 2)} reaches " +
                        "${money(once * 2)} — exactly twice, because the formula multiplies by the payment. " +
                        "Doubling the years does far more than doubling: that is the asymmetry the topic is about.",
                )
            }
            else -> {
                val stayed = p.plan.valueAt(HORIZON)
                val claimed = stayed * random.pick(listOf(0.5, 0.8, 1.2))
                trueFalse(
                    prompt = "${money(p.monthly)} a month from ${p.start} to ${p.stop} at ${pct(p.rate)}, then left " +
                        "alone, is worth more than ${money(claimed)} at $HORIZON.",
                    isTrue = stayed > claimed,
                    explanation = "At ${p.stop}: ${money(p.plan.valueAt(p.stop))}. Left alone until $HORIZON: " +
                        "${money(stayed)}. ${if (stayed > claimed) "More" else "Less"} than ${money(claimed)}.",
                )
            }
        }
    }
}
