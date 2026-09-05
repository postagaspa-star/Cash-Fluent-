package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.SideIncome
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Drill
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import kotlin.random.Random

/** Module 09. What arrived, what was taxable, what to move out the same day. */
object SideIncomeDrill : Drill {

    override val moduleId = "side-income"

    override fun round(random: Random, index: Int): Round {
        val monthly = random.pick(listOf(300.0, 500.0, 800.0, 1_200.0))
        val expenses = random.pick(listOf(600.0, 900.0, 1_500.0, 2_400.0))
        val rate = random.pick(listOf(0.20, 0.25, 0.30))
        val year = SideIncome.year(monthly, expenses, rate)
        val setAside = SideIncome.flatSetAside(monthly, rate)

        return when (index) {
            0 -> numberRound(
                prompt = "You're paid ${money(monthly)} a month for a year of weekend work, and ${money(expenses)} " +
                    "of it went on gear and travel for that work. What is taxable?",
                quantity = Quantity.AMOUNT,
                truth = year.profit,
                random = random,
                explanation = "${num(monthly)} × 12 = ${money(year.gross)} arrived. ${num(year.gross)} − " +
                    "${num(expenses)} = ${money(year.profit)} is profit, and profit is what gets taxed.",
            )

            1 -> numberRound(
                prompt = "${money(year.gross)} arrived over the year, ${money(expenses)} of it spent on the " +
                    "work, combined rate ${pct(rate)}. How big is the bill?",
                quantity = Quantity.AMOUNT,
                truth = year.taxDue,
                random = random,
                explanation = "Profit ${num(year.profit)} × ${pct(rate)} = ${money(year.taxDue)}. Not ${pct(rate)} " +
                    "of what arrived — of what was left after costs.",
            )

            2 -> numberRound(
                prompt = "A ${money(monthly)} payment lands and your combined rate is ${pct(rate)}. How much do " +
                    "you move out the same day?",
                quantity = Quantity.AMOUNT,
                truth = setAside,
                random = random,
                explanation = "${num(monthly)} × ${pct(rate)} = ${money(setAside)}, out of reach before you do " +
                    "anything else. Twelve of those make ${money(setAside * 12)} — more than the " +
                    "${money(year.taxDue)} bill.",
            )

            3 -> {
                val effective = year.effectiveRateOnGross
                val candidates = listOf(rate, effective, effective / 2).shuffled(random)
                ChoiceRound(
                    prompt = "Headline rate ${pct(rate)}, ${money(year.gross)} arrived, ${money(expenses)} of " +
                        "costs. What rate did you actually pay on the money that arrived?",
                    options = candidates.map { pct(it, 1) },
                    correctIndex = candidates.indexOf(effective),
                    explanation = "The bill is ${money(year.taxDue)} on ${money(year.gross)}: ${num(year.taxDue)} ÷ " +
                        "${num(year.gross)} = ${pct(effective, 1)}. Costs pull the real rate under the " +
                        "headline one, every time.",
                )
            }

            else -> {
                val cushion = SideIncome.cushion(monthly, expenses, rate)
                numberRound(
                    prompt = "You hold back ${pct(rate)} of every ${money(monthly)} payment all year. When the " +
                        "${money(year.taxDue)} bill arrives, how much is left over?",
                    quantity = Quantity.AMOUNT,
                    truth = cushion,
                    random = random,
                    explanation = "Held back: ${num(setAside)} × 12 = ${money(setAside * 12)}. Bill: " +
                        "${money(year.taxDue)}. The difference, ${money(cushion)}, is yours — the habit " +
                        "over-collects because costs make the true bill smaller.",
                )
            }
        }
    }
}
