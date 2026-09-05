package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.CompoundInterest
import com.cashfluent.app.domain.finance.SavingPlan
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
import kotlin.random.Random

/** Module 02. The Alex-and-Sam comparison, dealt with fresh numbers every time. */
object CompoundDrill : Drill {

    override val moduleId = "compound-interest"

    private const val HORIZON = 58

    override fun round(random: Random, index: Int): Round {
        val monthly = random.pick(listOf(50.0, 100.0, 150.0, 200.0))
        val start = random.pick(listOf(18, 20, 22, 25))
        val years = random.pick(listOf(5, 10, 15))
        val rate = random.pick(listOf(0.05, 0.06, 0.07, 0.08))
        val plan = SavingPlan(monthly, start, start + years, rate)
        val stop = plan.stopAge

        return when (index) {
            0 -> {
                val atStop = plan.valueAt(stop)
                val total = plan.valueAt(HORIZON)
                numberRound(
                    prompt = "${money(monthly)} a month from $start to $stop at ${pct(rate)}, then nothing " +
                        "more, ever. What is it worth at $HORIZON?",
                    quantity = Quantity.AMOUNT,
                    truth = total,
                    random = random,
                    explanation = "By $stop the pile is ${money(atStop)}. Left alone for ${HORIZON - stop} " +
                        "more years: ${num(atStop)} × ${num(1 + rate, 2)}^${HORIZON - stop} = ${money(total)}.",
                )
            }

            1 -> {
                val early = SavingPlan(monthly, start, start + 10, rate)
                val lateStart = start + random.pick(listOf(10, 12, 15))
                val late = SavingPlan(monthly * 2, lateStart, lateStart + 15, rate)
                val a = early.valueAt(HORIZON)
                val b = late.valueAt(HORIZON)
                ChoiceRound(
                    prompt = "Same ${pct(rate)} return either way. Which is worth more at $HORIZON?",
                    options = listOf(
                        "A: ${money(monthly)} a month from $start to ${start + 10}, then nothing",
                        "B: ${money(monthly * 2)} a month from $lateStart to ${lateStart + 15}",
                    ),
                    correctIndex = if (a >= b) 0 else 1,
                    explanation = "A puts in ${money(early.totalContributed)} and reaches ${money(a)}. B puts " +
                        "in ${money(late.totalContributed)} and reaches ${money(b)}. " +
                        if (a >= b) {
                            "Ten early years beat fifteen later ones at twice the amount."
                        } else {
                            "Here twice the money for fifteen years wins — the head start was too short " +
                                "to make up for it."
                        },
                )
            }

            2 -> {
                val value = plan.valueAt(stop)
                val growth = value - plan.totalContributed
                numberRound(
                    prompt = "${money(monthly)} a month from $start to $stop at ${pct(rate)}. At $stop, how " +
                        "much of the pile is growth rather than your own deposits?",
                    quantity = Quantity.AMOUNT,
                    truth = growth,
                    random = random,
                    explanation = "You deposited ${money(plan.totalContributed)}. The pile is ${money(value)}. " +
                        "The difference, ${money(growth)}, was earned by the money itself.",
                )
            }

            3 -> {
                var n = 1
                while (CompoundInterest.lumpSum(1.0, rate, n) < 2.0) n++
                NumberRound(
                    prompt = "At ${pct(rate)} a year, how many whole years until a sum left alone has doubled?",
                    quantity = Quantity.YEARS,
                    truth = n.toDouble(),
                    min = 1.0, max = 40.0, step = 1.0,
                    explanation = "${num(1 + rate, 2)}^${n - 1} = ${num(CompoundInterest.lumpSum(1.0, rate, n - 1), 2)}, " +
                        "still short. ${num(1 + rate, 2)}^$n = ${num(CompoundInterest.lumpSum(1.0, rate, n), 2)}. " +
                        "The rule of 72 gets close: 72 ÷ ${num(rate * 100)} ≈ ${num(72 / (rate * 100), 1)}.",
                )
            }

            else -> {
                val moreMoney = SavingPlan(monthly * 2, start, start + years, rate).valueAt(HORIZON)
                val moreYears = SavingPlan(monthly, start, start + years * 2, rate).valueAt(HORIZON)
                ChoiceRound(
                    prompt = "${money(monthly)} a month from $start for $years years at ${pct(rate)}. " +
                        "Which adds more by $HORIZON?",
                    options = listOf(
                        "Doubling the amount: ${money(monthly * 2)} a month for $years years",
                        "Doubling the years: ${money(monthly)} a month for ${years * 2} years",
                    ),
                    correctIndex = if (moreMoney >= moreYears) 0 else 1,
                    explanation = "Twice the amount: ${money(moreMoney)}. Twice the years: ${money(moreYears)}. " +
                        if (moreMoney >= moreYears) {
                            "Doubling the deposit doubles the result, exactly — and here that was enough."
                        } else {
                            "Doubling the deposit only doubles the result. Doubling the years does more, " +
                                "because the exponent lives in the years."
                        },
                )
            }
        }
    }
}
