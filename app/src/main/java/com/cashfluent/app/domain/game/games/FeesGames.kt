package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Fees
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.higherLower
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.trueFalse
import kotlin.random.Random

/** Topic 05, shares and funds. What a yearly fee costs over the years it is taken. */
object FeesGames {

    private const val TOPIC = "investing"

    private class Fund(random: Random) {
        val monthly = random.pick(listOf(100.0, 150.0, 200.0, 300.0))
        val years = random.pick(listOf(20, 25, 30, 40))
        val gross = random.pick(listOf(0.06, 0.07, 0.08))
        val ter = random.pick(listOf(0.008, 0.01, 0.012, 0.015, 0.02))
        val yours = Fees.outcome(monthly, gross, ter, years)
        val cheap = Fees.outcome(monthly, gross, Fees.BENCHMARK_TER, years)
        val free = Fees.outcome(monthly, gross, 0.0, years)
        val difference = cheap.finalValue - yours.finalValue
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "fees-cost-of-fee", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What the fee costs",
            blurb = "Your fund against a cheap one on the same index. The gap at the end.",
            deal = ::costOfFee,
        ),
        MiniGame(
            id = "fees-effective-return", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What reaches you",
            blurb = "Gross return minus the fee. The only return you ever see.",
            deal = ::effectiveReturn,
        ),
        MiniGame(
            id = "fees-share-eaten", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "The slice the fee ate",
            blurb = "A fee that looked like 1% took what share of the pile?",
            deal = ::shareEaten,
        ),
        MiniGame(
            id = "fees-total-paid", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Fees, all in",
            blurb = "Against paying nothing at all: the fee, plus everything it would have earned.",
            deal = ::totalPaid,
        ),
        MiniGame(
            id = "fees-fee-vs-years", topicId = TOPIC, mechanic = Mechanic.HIGHER_LOWER,
            title = "Cheaper fee or more years",
            blurb = "Which adds more to the final pile? Only one of them is free.",
            deal = ::feeVersusYears,
        ),
        MiniGame(
            id = "fees-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Fund claims",
            blurb = "Statements about fees, funds and risk. Judge each one.",
            deal = ::claims,
        ),
    )

    private fun costOfFee(random: Random): Round {
        val f = Fund(random)
        return numberRound(
            prompt = "${money(f.monthly)} a month for ${f.years} years, ${pct(f.gross)} before fees. Your fund charges " +
                "${pct(f.ter, 1)} a year; a cheap one tracking the same index charges ${pct(Fees.BENCHMARK_TER, 1)}. " +
                "How much does your fee cost you by the end?",
            quantity = Quantity.AMOUNT,
            truth = f.difference,
            random = random,
            explanation = "At ${pct(f.gross - Fees.BENCHMARK_TER, 1)} the cheap fund ends at ${money(f.cheap.finalValue)}. " +
                "At ${pct(f.gross - f.ter, 1)} yours ends at ${money(f.yours.finalValue)}. The gap, " +
                "${money(f.difference)}, is what ${pct(f.ter - Fees.BENCHMARK_TER, 1)} a year adds up to.",
        )
    }

    private fun effectiveReturn(random: Random): Round {
        val f = Fund(random)
        return NumberRound(
            prompt = "The fund earns ${pct(f.gross)} before costs and charges a ${pct(f.ter, 1)} yearly fee. What " +
                "return actually reaches you?",
            quantity = Quantity.PERCENT_PRECISE,
            truth = f.gross - f.ter,
            min = 0.0, max = 0.10, step = 0.001,
            explanation = "${pct(f.gross)} − ${pct(f.ter, 1)} = ${pct(f.gross - f.ter, 1)}. The fee comes off inside " +
                "the fund, every year, before anything reaches you.",
        )
    }

    private fun shareEaten(random: Random): Round {
        val f = Fund(random)
        val share = f.yours.feesPaid / f.free.finalValue
        val nearest = (Math.round(share * 10) * 10).toInt()
        val candidates = (if (nearest <= 10) listOf(nearest, nearest + 10, nearest + 20)
            else listOf(nearest - 10, nearest, nearest + 10)).shuffled(random)
        return ChoiceRound(
            prompt = "${money(f.monthly)} a month for ${f.years} years at ${pct(f.gross)}, in a fund charging " +
                "${pct(f.ter, 1)}. With no fee at all the pile would end at ${money(f.free.finalValue)}. Roughly " +
                "what share of that did the fee eat?",
            options = candidates.map { "$it%" },
            correctIndex = candidates.indexOf(nearest),
            explanation = "With the fee: ${money(f.yours.finalValue)}. Without: ${money(f.free.finalValue)}. The fee " +
                "took ${money(f.yours.feesPaid)}, which is ${pct(share, 1)} of the no-fee pile — from a fee that " +
                "looked like ${pct(f.ter, 1)}.",
        )
    }

    private fun totalPaid(random: Random): Round {
        val f = Fund(random)
        return numberRound(
            prompt = "${money(f.monthly)} a month for ${f.years} years at ${pct(f.gross)} gross, fee ${pct(f.ter, 1)}. " +
                "Compared with paying no fee at all, how much did the fee take?",
            quantity = Quantity.AMOUNT,
            truth = f.yours.feesPaid,
            random = random,
            explanation = "No fee: ${money(f.free.finalValue)}. With ${pct(f.ter, 1)}: ${money(f.yours.finalValue)}. " +
                "Difference: ${money(f.yours.feesPaid)} — not just the fee, but everything the fee would have earned.",
        )
    }

    private fun feeVersusYears(random: Random): Round {
        val f = Fund(random)
        val longer = Fees.outcome(f.monthly, f.gross, f.ter, f.years + 5).finalValue - f.yours.finalValue
        val feeWins = f.difference >= longer
        return higherLower(
            prompt = "${money(f.monthly)} a month at ${pct(f.gross)} gross for ${f.years} years in a ${pct(f.ter, 1)} " +
                "fund. Which adds more to the final pile?",
            first = "Cutting the fee to ${pct(Fees.BENCHMARK_TER, 1)}",
            second = "Staying five more years, fee unchanged",
            firstIsHigher = feeWins,
            explanation = "The cheaper fee adds ${money(f.difference)}. Five more years add ${money(longer)}. " +
                if (feeWins) "The fee wins here — and it costs nothing to change." else "Time wins here, but the fee is the one you can change today.",
        )
    }

    private fun claims(random: Random): Round {
        val f = Fund(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "Two funds track the same index. The one charging ${pct(f.ter, 1)} returns more than the one " +
                    "charging ${pct(Fees.BENCHMARK_TER, 1)}, because you get what you pay for.",
                isTrue = false,
                explanation = "Same companies, same return before costs. The fee comes off afterwards, every year: " +
                    "over ${f.years} years that is ${money(f.difference)} less for the dearer fund.",
            )
            1 -> {
                val claimed = f.yours.feesPaid * random.pick(listOf(0.5, 1.5))
                trueFalse(
                    prompt = "${money(f.monthly)} a month for ${f.years} years at ${pct(f.gross)}: a ${pct(f.ter, 1)} fee " +
                        "takes more than ${money(claimed)} compared with paying no fee.",
                    isTrue = f.yours.feesPaid > claimed,
                    explanation = "No fee: ${money(f.free.finalValue)}. With the fee: ${money(f.yours.finalValue)}. Gap: " +
                        "${money(f.yours.feesPaid)} — ${if (f.yours.feesPaid > claimed) "more" else "less"} than ${money(claimed)}.",
                )
            }
            else -> trueFalse(
                prompt = "Owning a fund of 500 companies protects you from a fall in the whole market.",
                isTrue = false,
                explanation = "Spreading across 500 companies removes the risk of any 1 of them failing. When the " +
                    "whole market falls, it falls across all 500 at once. That risk is handled by time, not by count.",
            )
        }
    }
}
