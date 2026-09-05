package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Debt
import com.cashfluent.app.domain.finance.Payoff
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
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
import com.cashfluent.app.domain.game.trueFalse
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

/** Topic 03, debt. The month's interest, and the payment that has to beat it. */
object DebtGames {

    private const val TOPIC = "debt"

    private class Loan(random: Random) {
        val balance = random.pick(listOf(500.0, 800.0, 1_200.0, 2_000.0))
        val apr = random.pick(listOf(0.15, 0.20, 0.25, 0.30))
        val interest = Debt.monthlyInterest(balance, apr)
        // Always above the month's interest, so the debt is one that clears.
        val payment = roundTo(interest * random.pick(listOf(1.5, 2.0, 3.0)), 5.0)
            .coerceAtLeast(roundTo(interest, 5.0) + 5.0)
        val payoff = Debt.payoff(balance, apr, payment) as Payoff.Clears
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "debt-month-interest", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "This month's interest",
            blurb = "A balance and an APR. What does the debt charge just for existing this month?",
            deal = ::monthInterest,
        ),
        MiniGame(
            id = "debt-smallest-payment", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "The payment that bites",
            blurb = "Three payments. Only some of them actually shrink the debt.",
            deal = ::smallestPayment,
        ),
        MiniGame(
            id = "debt-months-to-clear", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Months to zero",
            blurb = "Fixed payment, fixed rate. How long until it's gone?",
            deal = ::monthsToClear,
        ),
        MiniGame(
            id = "debt-total-interest", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The real price tag",
            blurb = "Everything the interest added by the time the debt cleared.",
            deal = ::totalInterest,
        ),
        MiniGame(
            id = "debt-ten-more", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Ten more a month",
            blurb = "A small extra payment. How many months does it buy back?",
            deal = ::tenMore,
        ),
        MiniGame(
            id = "debt-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Minimum-payment myths",
            blurb = "Claims about paying on time and paying enough. Not the same thing.",
            deal = ::claims,
        ),
    )

    private fun monthInterest(random: Random): Round {
        val l = Loan(random)
        return numberRound(
            prompt = "You owe ${money(l.balance)} on a card at ${pct(l.apr)} APR. How much interest does this month add?",
            quantity = Quantity.AMOUNT_CENTS,
            truth = l.interest,
            random = random,
            explanation = "${num(l.balance)} × ${num(l.apr, 2)} ÷ 12 = ${money(l.interest, 2)}. Any payment below " +
                "that and the balance grows while you pay.",
        )
    }

    private fun smallestPayment(random: Random): Round {
        val l = Loan(random)
        val base = floor(l.interest).toInt()
        val candidates = listOf(base - 3, base, base + 1)
        return ChoiceRound(
            prompt = "${money(l.balance)} at ${pct(l.apr)} APR: the month's interest is ${money(l.interest, 2)}. Which " +
                "is the smallest payment here that actually shrinks the debt?",
            options = candidates.map { money(it.toDouble()) },
            correctIndex = 2,
            explanation = "A payment has to beat ${money(l.interest, 2)}. ${money(base.toDouble())} does not, so the " +
                "balance climbs; ${money((base + 1).toDouble())} is the first one that does.",
        )
    }

    private fun monthsToClear(random: Random): Round {
        val l = Loan(random)
        val top = niceCeil(l.payoff.months * 2)
        return NumberRound(
            prompt = "Paying ${money(l.payment)} a month on ${money(l.balance)} at ${pct(l.apr)}. How many months " +
                "until it's gone?",
            quantity = Quantity.MONTHS,
            truth = l.payoff.months,
            min = 0.0, max = top, step = top / 100,
            explanation = "n = −ln(1 − (${num(Debt.monthlyRate(l.apr), 4)} × ${num(l.balance)}) ÷ ${num(l.payment)}) ÷ " +
                "ln(1 + ${num(Debt.monthlyRate(l.apr), 4)}) = ${num(l.payoff.months, 1)} months — " +
                "${l.payoff.wholeMonths} payments.",
        )
    }

    private fun totalInterest(random: Random): Round {
        val l = Loan(random)
        return numberRound(
            prompt = "Paying ${money(l.payment)} a month on ${money(l.balance)} at ${pct(l.apr)} clears it in " +
                "${l.payoff.wholeMonths} payments. How much interest did you pay in total?",
            quantity = Quantity.AMOUNT,
            truth = l.payoff.totalInterest,
            random = random,
            explanation = "${num(l.payoff.months, 1)} months × ${num(l.payment)} = ${money(l.payoff.totalPaid)} paid. " +
                "Minus the ${money(l.balance)} you borrowed, ${money(l.payoff.totalInterest)} was interest.",
        )
    }

    private fun tenMore(random: Random): Round {
        val l = Loan(random)
        val better = Debt.payoff(l.balance, l.apr, l.payment + 10.0) as Payoff.Clears
        val sooner = (l.payoff.months - better.months).roundToInt().coerceAtLeast(1)
        val candidates = listOf(sooner, if (sooner >= 2) sooner / 2 else sooner + 1, sooner * 2 + 1).shuffled(random)
        return ChoiceRound(
            prompt = "Same debt, ${money(l.payment)} a month. Pay ${money(10.0)} more each month: how many months " +
                "sooner is it gone?",
            options = candidates.map { if (it == 1) "1 month sooner" else "$it months sooner" },
            correctIndex = candidates.indexOf(sooner),
            explanation = "At ${money(l.payment)}: ${num(l.payoff.months, 1)} months. At ${money(l.payment + 10.0)}: " +
                "${num(better.months, 1)}. $sooner sooner, and ${money(l.payoff.totalInterest - better.totalInterest)} " +
                "less interest.",
        )
    }

    private fun claims(random: Random): Round {
        val l = Loan(random)
        return when (random.nextInt(3)) {
            0 -> {
                val payment = roundTo(l.interest, 1.0)
                val clears = Debt.payoff(l.balance, l.apr, payment) is Payoff.Clears
                trueFalse(
                    prompt = "Paying ${money(payment)} a month on ${money(l.balance)} at ${pct(l.apr)}, on time every " +
                        "month, the balance goes down.",
                    isTrue = clears,
                    explanation = "The month's interest is ${money(l.interest, 2)}. A payment of ${money(payment)} " +
                        "${if (clears) "beats it, so the balance falls" else "does not beat it, so the balance stands still or grows"} " +
                        "— paying on time protects your record, not your balance.",
                )
            }
            1 -> {
                val claimed = roundTo(l.payoff.totalInterest * random.pick(listOf(0.5, 1.5)), 10.0)
                trueFalse(
                    prompt = "Paying ${money(l.payment)} a month on ${money(l.balance)} at ${pct(l.apr)} costs more than " +
                        "${money(claimed)} in interest by the end.",
                    isTrue = l.payoff.totalInterest > claimed,
                    explanation = "Total paid ${money(l.payoff.totalPaid)} minus ${money(l.balance)} borrowed = " +
                        "${money(l.payoff.totalInterest)} of interest. ${if (l.payoff.totalInterest > claimed) "More" else "Less"} " +
                        "than ${money(claimed)}.",
                )
            }
            else -> trueFalse(
                prompt = "${money(5_000.0)} at 4% for a course that raises your income is a worse debt than " +
                    "${money(800.0)} at 20% for clothes, because it is the bigger number.",
                isTrue = false,
                explanation = "Size is not the measure. 20% is five times the rate, and it bought something losing value " +
                    "from day one; the 4% loan is cheaper per unit borrowed and buys earning power.",
            )
        }
    }
}
