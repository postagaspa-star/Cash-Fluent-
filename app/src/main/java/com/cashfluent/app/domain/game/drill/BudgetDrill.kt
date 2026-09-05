package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Budget
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.niceCeil
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.roundTo
import kotlin.math.abs
import kotlin.random.Random

/** Module 01. Every round is the split of one month, with real figures. */
object BudgetDrill : Drill {

    override val moduleId = "budgeting"

    private val nets = listOf(900.0, 1_200.0, 1_500.0, 1_800.0, 2_200.0)
    private val needShares = listOf(0.45, 0.5, 0.55, 0.6)
    private val wantShares = listOf(0.2, 0.25, 0.3)

    override fun round(random: Random, index: Int): Round {
        val net = random.pick(nets)
        val needs = roundTo(net * random.pick(needShares), 10.0)
        val wants = roundTo(net * random.pick(wantShares), 10.0)
        val actual = Budget.actual(net, needs, wants)
        val shares = Budget.shares(actual, net)
        val target = Budget.target(net)

        return when (index) {
            0 -> NumberRound(
                prompt = "${money(net)} lands in your account this month. Needs took ${money(needs)} " +
                    "and wants ${money(wants)}. What share of the month is left for your future?",
                quantity = Quantity.PERCENT,
                truth = shares.future,
                min = 0.0, max = 0.5, step = 0.01,
                explanation = "${num(net)} − ${num(needs)} − ${num(wants)} = ${num(actual.future)}, and " +
                    "${num(actual.future)} ÷ ${num(net)} = ${pct(shares.future, 1)}.",
            )

            1 -> {
                val gap = Budget.futureGapPerMonth(net, actual.future)
                val short = gap >= 0.0
                val top = niceCeil(target.future)
                NumberRound(
                    prompt = "Same month: ${money(net)} in, ${money(needs)} on needs, ${money(wants)} on " +
                        "wants. The 20% target for your future is ${money(target.future)}. How far " +
                        "${if (short) "short of" else "past"} it are you, per month?",
                    quantity = Quantity.AMOUNT,
                    truth = abs(gap),
                    min = 0.0, max = top, step = top / 100,
                    explanation = "20% of ${num(net)} is ${num(target.future)}. What's left is " +
                        "${num(actual.future)}, so the gap is ${money(abs(gap))} a month" +
                        if (short) " — ${money(abs(gap) * 12)} a year." else ", in your favour.",
                )
            }

            2 -> {
                val months = listOf(0.45 to 0.25, 0.5 to 0.3, 0.6 to 0.3).shuffled(random)
                    .map { (n, w) -> roundTo(net * n, 10.0) to roundTo(net * w, 10.0) }
                val futures = months.map { (n, w) -> Budget.actual(net, n, w).future }
                ChoiceRound(
                    prompt = "On ${money(net)} a month, which of these leaves the most for your future?",
                    options = months.map { (n, w) -> "Needs ${money(n)}, wants ${money(w)}" },
                    correctIndex = futures.indexOf(futures.max()),
                    explanation = months.zip(futures).joinToString(" ") { (m, f) ->
                        "${num(m.first)} and ${num(m.second)} leave ${money(f)}."
                    } + " The pile that's left is the only number the future ever sees.",
                )
            }

            3 -> {
                val truth = net - needs - target.future
                NumberRound(
                    prompt = "Needs are ${money(needs)} on ${money(net)}. Set wants so that exactly 20% " +
                        "is left for your future.",
                    quantity = Quantity.AMOUNT,
                    truth = truth,
                    min = 0.0, max = net, step = net / 100,
                    explanation = "20% of ${num(net)} is ${num(target.future)}. ${num(net)} − ${num(needs)} − " +
                        "${num(target.future)} = ${money(truth)} for wants — ${pct(truth / net, 0)} of the month.",
                )
            }

            else -> {
                val heavyNeeds = roundTo(net * 0.6, 10.0)
                val heavyWants = roundTo(net * random.pick(listOf(0.45, 0.5)), 10.0)
                val future = Budget.actual(net, heavyNeeds, heavyWants).future
                ChoiceRound(
                    prompt = "${money(net)} in. Needs ${money(heavyNeeds)}, wants ${money(heavyWants)}. " +
                        "What is the future pile this month?",
                    options = listOf(
                        "${money(future)} — more went out than came in",
                        "${money(0.0)} — nothing left, nothing owed",
                        money(-future),
                    ),
                    correctIndex = 0,
                    explanation = "${num(net)} − ${num(heavyNeeds)} − ${num(heavyWants)} = ${money(future)}. " +
                        "A negative future pile is the month borrowing from the next one — the app " +
                        "shows it rather than hiding it, because this is the number to fix first.",
                )
            }
        }
    }
}
