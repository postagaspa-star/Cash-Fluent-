package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Debt
import com.cashfluent.app.domain.finance.Payoff
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.niceCeil
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

/** Module 03. The month's interest, and the payment that has to beat it. */
object DebtDrill : Drill {

    override val moduleId = "debt"

    override fun round(random: Random, index: Int): Round {
        val balance = random.pick(listOf(500.0, 800.0, 1_200.0, 2_000.0))
        val apr = random.pick(listOf(0.15, 0.20, 0.25, 0.30))
        val interest = Debt.monthlyInterest(balance, apr)
        // Always above the month's interest, so the debt is one that clears.
        val payment = roundTo(interest * random.pick(listOf(1.5, 2.0, 3.0)), 5.0)
            .coerceAtLeast(roundTo(interest, 5.0) + 5.0)
        val payoff = Debt.payoff(balance, apr, payment) as Payoff.Clears

        return when (index) {
            0 -> numberRound(
                prompt = "You owe ${money(balance)} on a card at ${pct(apr)} APR. How much interest does " +
                    "this month add?",
                quantity = Quantity.AMOUNT_CENTS,
                truth = interest,
                random = random,
                explanation = "${num(balance)} × ${num(apr, 2)} ÷ 12 = ${money(interest, 2)}. Any payment " +
                    "below that and the balance grows while you pay.",
            )

            1 -> {
                val base = floor(interest).toInt()
                val candidates = listOf(base - 3, base, base + 1)
                ChoiceRound(
                    prompt = "${money(balance)} at ${pct(apr)} APR: the month's interest is ${money(interest, 2)}. " +
                        "Which is the smallest payment here that actually shrinks the debt?",
                    options = candidates.map { money(it.toDouble()) },
                    correctIndex = 2,
                    explanation = "A payment has to beat ${money(interest, 2)}. ${money(base.toDouble())} does " +
                        "not, so the balance climbs; ${money((base + 1).toDouble())} is the first one that does.",
                )
            }

            2 -> {
                val top = niceCeil(payoff.months * 2)
                NumberRound(
                    prompt = "Paying ${money(payment)} a month on ${money(balance)} at ${pct(apr)}. How many " +
                        "months until it's gone?",
                    quantity = Quantity.MONTHS,
                    truth = payoff.months,
                    min = 0.0, max = top, step = top / 100,
                    explanation = "n = −ln(1 − (${num(Debt.monthlyRate(apr), 4)} × ${num(balance)}) ÷ " +
                        "${num(payment)}) ÷ ln(1 + ${num(Debt.monthlyRate(apr), 4)}) = ${num(payoff.months, 1)} " +
                        "months — ${payoff.wholeMonths} payments.",
                )
            }

            3 -> numberRound(
                prompt = "Paying ${money(payment)} a month on ${money(balance)} at ${pct(apr)} clears it in " +
                    "${payoff.wholeMonths} payments. How much interest did you pay in total?",
                quantity = Quantity.AMOUNT,
                truth = payoff.totalInterest,
                random = random,
                explanation = "${num(payoff.months, 1)} months × ${num(payment)} = ${money(payoff.totalPaid)} " +
                    "paid. Minus the ${money(balance)} you borrowed, ${money(payoff.totalInterest)} was interest.",
            )

            else -> {
                val better = Debt.payoff(balance, apr, payment + 10.0) as Payoff.Clears
                val sooner = (payoff.months - better.months).roundToInt().coerceAtLeast(1)
                val candidates = listOf(sooner, if (sooner >= 2) sooner / 2 else sooner + 1, sooner * 2 + 1)
                    .shuffled(random)
                ChoiceRound(
                    prompt = "Same debt, ${money(payment)} a month. Pay ${money(10.0)} more each month: how " +
                        "many months sooner is it gone?",
                    options = candidates.map { if (it == 1) "1 month sooner" else "$it months sooner" },
                    correctIndex = candidates.indexOf(sooner),
                    explanation = "At ${money(payment)}: ${num(payoff.months, 1)} months. At " +
                        "${money(payment + 10.0)}: ${num(better.months, 1)}. $sooner sooner, and " +
                        "${money(payoff.totalInterest - better.totalInterest)} less interest.",
                )
            }
        }
    }
}
