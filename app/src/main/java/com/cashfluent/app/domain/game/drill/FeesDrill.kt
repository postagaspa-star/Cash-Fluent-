package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Fees
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import kotlin.random.Random

/** Module 05. What a yearly fee costs over the years it is taken. */
object FeesDrill : Drill {

    override val moduleId = "investing"

    override fun round(random: Random, index: Int): Round {
        val monthly = random.pick(listOf(100.0, 150.0, 200.0, 300.0))
        val years = random.pick(listOf(20, 25, 30, 40))
        val gross = random.pick(listOf(0.06, 0.07, 0.08))
        val ter = random.pick(listOf(0.008, 0.01, 0.012, 0.015, 0.02))
        val yours = Fees.outcome(monthly, gross, ter, years)
        val cheap = Fees.outcome(monthly, gross, Fees.BENCHMARK_TER, years)
        val free = Fees.outcome(monthly, gross, 0.0, years)
        val difference = cheap.finalValue - yours.finalValue

        return when (index) {
            0 -> numberRound(
                prompt = "${money(monthly)} a month for $years years, ${pct(gross)} before fees. Your fund " +
                    "charges ${pct(ter, 1)} a year; a cheap one tracking the same index charges " +
                    "${pct(Fees.BENCHMARK_TER, 1)}. How much does your fee cost you by the end?",
                quantity = Quantity.AMOUNT,
                truth = difference,
                random = random,
                explanation = "At ${pct(gross - Fees.BENCHMARK_TER, 1)} the cheap fund ends at " +
                    "${money(cheap.finalValue)}. At ${pct(gross - ter, 1)} yours ends at " +
                    "${money(yours.finalValue)}. The gap, ${money(difference)}, is what " +
                    "${pct(ter - Fees.BENCHMARK_TER, 1)} a year adds up to.",
            )

            1 -> NumberRound(
                prompt = "The fund earns ${pct(gross)} before costs and charges a ${pct(ter, 1)} yearly fee. " +
                    "What return actually reaches you?",
                quantity = Quantity.PERCENT_PRECISE,
                truth = gross - ter,
                min = 0.0, max = 0.10, step = 0.001,
                explanation = "${pct(gross)} − ${pct(ter, 1)} = ${pct(gross - ter, 1)}. The fee comes off " +
                    "inside the fund, every year, before anything reaches you.",
            )

            2 -> {
                val share = yours.feesPaid / free.finalValue
                val nearest = (Math.round(share * 10) * 10).toInt()
                val candidates = (if (nearest <= 10) listOf(nearest, nearest + 10, nearest + 20)
                    else listOf(nearest - 10, nearest, nearest + 10)).shuffled(random)
                ChoiceRound(
                    prompt = "${money(monthly)} a month for $years years at ${pct(gross)}, in a fund charging " +
                        "${pct(ter, 1)}. With no fee at all the pile would end at ${money(free.finalValue)}. " +
                        "Roughly what share of that did the fee eat?",
                    options = candidates.map { "$it%" },
                    correctIndex = candidates.indexOf(nearest),
                    explanation = "With the fee: ${money(yours.finalValue)}. Without: ${money(free.finalValue)}. " +
                        "The fee took ${money(yours.feesPaid)}, which is ${pct(share, 1)} of the no-fee pile — " +
                        "from a fee that looked like ${pct(ter, 1)}.",
                )
            }

            3 -> numberRound(
                prompt = "${money(monthly)} a month for $years years at ${pct(gross)} gross, fee ${pct(ter, 1)}. " +
                    "Compared with paying no fee at all, how much did the fee take?",
                quantity = Quantity.AMOUNT,
                truth = yours.feesPaid,
                random = random,
                explanation = "No fee: ${money(free.finalValue)}. With ${pct(ter, 1)}: ${money(yours.finalValue)}. " +
                    "Difference: ${money(yours.feesPaid)} — not just the fee, but everything the fee would " +
                    "have earned.",
            )

            else -> {
                val longer = Fees.outcome(monthly, gross, ter, years + 5).finalValue - yours.finalValue
                val feeWins = difference >= longer
                ChoiceRound(
                    prompt = "${money(monthly)} a month at ${pct(gross)} gross for $years years in a " +
                        "${pct(ter, 1)} fund. Which adds more to the final pile?",
                    options = listOf(
                        "Cutting the fee to ${pct(Fees.BENCHMARK_TER, 1)}",
                        "Staying five more years, fee unchanged",
                    ),
                    correctIndex = if (feeWins) 0 else 1,
                    explanation = "The cheaper fee adds ${money(difference)}. Five more years add ${money(longer)}. " +
                        if (feeWins) {
                            "The fee wins here — and it costs nothing to change."
                        } else {
                            "Time wins here, but the fee is the one you can change today."
                        },
                )
            }
        }
    }
}
