package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Budget
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.niceCeil
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import com.cashfluent.app.domain.game.trueFalse
import kotlin.math.abs
import kotlin.random.Random

/** Topic 01, budgeting. Every round is the split of one month, with real figures. */
object BudgetGames {

    private const val TOPIC = "budgeting"

    private val nets = listOf(900.0, 1_200.0, 1_500.0, 1_800.0, 2_200.0)
    private val needShares = listOf(0.45, 0.5, 0.55, 0.6)
    private val wantShares = listOf(0.2, 0.25, 0.3)

    private class Month(random: Random) {
        val net = random.pick(nets)
        val needs = roundTo(net * random.pick(needShares), 10.0)
        val wants = roundTo(net * random.pick(wantShares), 10.0)
        val actual = Budget.actual(net, needs, wants)
        val shares = Budget.shares(actual, net)
        val target = Budget.target(net)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "budget-future-share", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What's left",
            blurb = "Net in, needs and wants out. What share of the month reaches your future?",
            deal = ::futureShare,
        ),
        MiniGame(
            id = "budget-gap", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The gap to 20%",
            blurb = "How far a month lands from the 20% target, in money.",
            deal = ::gapToTarget,
        ),
        MiniGame(
            id = "budget-best-month", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Best month",
            blurb = "Three months on the same pay. Which one leaves the most behind?",
            deal = ::bestMonth,
        ),
        MiniGame(
            id = "budget-set-wants", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Dial in the wants",
            blurb = "Needs are fixed. Set wants so exactly 20% is left.",
            deal = ::setWants,
        ),
        MiniGame(
            id = "budget-overspend", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Red month",
            blurb = "More went out than came in. Read the number for what it is.",
            deal = ::overspend,
        ),
        MiniGame(
            id = "budget-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "50/30/20, true or false",
            blurb = "Claims about a month. Judge each one against the arithmetic.",
            deal = ::claims,
        ),
    )

    private fun futureShare(random: Random): Round {
        val m = Month(random)
        return NumberRound(
            prompt = "${money(m.net)} lands in your account this month. Needs took ${money(m.needs)} and " +
                "wants ${money(m.wants)}. What share of the month is left for your future?",
            quantity = Quantity.PERCENT,
            truth = m.shares.future,
            min = 0.0, max = 0.5, step = 0.01,
            explanation = "${num(m.net)} − ${num(m.needs)} − ${num(m.wants)} = ${num(m.actual.future)}, and " +
                "${num(m.actual.future)} ÷ ${num(m.net)} = ${pct(m.shares.future, 1)}.",
        )
    }

    private fun gapToTarget(random: Random): Round {
        val m = Month(random)
        val gap = Budget.futureGapPerMonth(m.net, m.actual.future)
        val short = gap >= 0.0
        val top = niceCeil(m.target.future)
        return NumberRound(
            prompt = "${money(m.net)} in, ${money(m.needs)} on needs, ${money(m.wants)} on wants. The 20% " +
                "target for your future is ${money(m.target.future)}. How far ${if (short) "short of" else "past"} " +
                "it are you, per month?",
            quantity = Quantity.AMOUNT,
            truth = abs(gap),
            min = 0.0, max = top, step = top / 100,
            explanation = "20% of ${num(m.net)} is ${num(m.target.future)}. What's left is ${num(m.actual.future)}, " +
                "so the gap is ${money(abs(gap))} a month" +
                if (short) " — ${money(abs(gap) * 12)} a year." else ", in your favour.",
        )
    }

    private fun bestMonth(random: Random): Round {
        val net = random.pick(nets)
        val months = listOf(0.45 to 0.25, 0.5 to 0.3, 0.6 to 0.3).shuffled(random)
            .map { (n, w) -> roundTo(net * n, 10.0) to roundTo(net * w, 10.0) }
        val futures = months.map { (n, w) -> Budget.actual(net, n, w).future }
        return ChoiceRound(
            prompt = "On ${money(net)} a month, which of these leaves the most for your future?",
            options = months.map { (n, w) -> "Needs ${money(n)}, wants ${money(w)}" },
            correctIndex = futures.indexOf(futures.max()),
            explanation = months.zip(futures).joinToString(" ") { (month, future) ->
                "${num(month.first)} and ${num(month.second)} leave ${money(future)}."
            } + " The pile that's left is the only number the future ever sees.",
        )
    }

    private fun setWants(random: Random): Round {
        val m = Month(random)
        val truth = m.net - m.needs - m.target.future
        return NumberRound(
            prompt = "Needs are ${money(m.needs)} on ${money(m.net)}. Set wants so that exactly 20% is left " +
                "for your future.",
            quantity = Quantity.AMOUNT,
            truth = truth,
            min = 0.0, max = m.net, step = m.net / 100,
            explanation = "20% of ${num(m.net)} is ${num(m.target.future)}. ${num(m.net)} − ${num(m.needs)} − " +
                "${num(m.target.future)} = ${money(truth)} for wants — ${pct(truth / m.net, 0)} of the month.",
        )
    }

    private fun overspend(random: Random): Round {
        val net = random.pick(nets)
        val needs = roundTo(net * 0.6, 10.0)
        val wants = roundTo(net * random.pick(listOf(0.45, 0.5)), 10.0)
        val future = Budget.actual(net, needs, wants).future
        return ChoiceRound(
            prompt = "${money(net)} in. Needs ${money(needs)}, wants ${money(wants)}. What is the future pile " +
                "this month?",
            options = listOf(
                "${money(future)} — more went out than came in",
                "${money(0.0)} — nothing left, nothing owed",
                money(-future),
            ),
            correctIndex = 0,
            explanation = "${num(net)} − ${num(needs)} − ${num(wants)} = ${money(future)}. A negative future pile " +
                "is this month borrowing from the next one — the number to fix first.",
        )
    }

    private fun claims(random: Random): Round {
        val m = Month(random)
        return when (random.nextInt(3)) {
            0 -> {
                val onTarget = m.shares.future >= Budget.TARGET_FUTURE
                trueFalse(
                    prompt = "${money(m.net)} in, ${money(m.needs)} on needs, ${money(m.wants)} on wants. This month " +
                        "hits the 20% target for the future.",
                    isTrue = onTarget,
                    explanation = "What's left is ${money(m.actual.future)}, which is ${pct(m.shares.future, 1)} of " +
                        "${num(m.net)}. The target is ${pct(Budget.TARGET_FUTURE)}, so the claim is " +
                        "${if (onTarget) "true" else "false"}.",
                )
            }
            1 -> {
                val insideNeeds = m.shares.needs <= Budget.TARGET_NEEDS
                trueFalse(
                    prompt = "Needs of ${money(m.needs)} on a ${money(m.net)} month are inside the 50% target.",
                    isTrue = insideNeeds,
                    explanation = "${num(m.needs)} ÷ ${num(m.net)} = ${pct(m.shares.needs, 1)}, against a target " +
                        "of 50%. ${if (insideNeeds) "Inside it." else "Over it — which is common, and not a failure."}",
                )
            }
            else -> {
                val raise = random.pick(listOf(100.0, 150.0, 200.0))
                val claimed = random.pick(listOf(raise * 0.2, raise * 0.5, raise))
                trueFalse(
                    prompt = "You get a ${money(raise)} raise. Under 50/30/20, ${money(claimed)} of it goes to " +
                        "your future.",
                    isTrue = claimed == raise * Budget.TARGET_FUTURE,
                    explanation = "The split applies to every unit that arrives: 20% of ${num(raise)} is " +
                        "${money(raise * Budget.TARGET_FUTURE)}. 50% would be the needs share; all of it is a rule " +
                        "nobody keeps.",
                )
            }
        }
    }
}
