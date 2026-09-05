package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.SideIncome
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Mechanic
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.money
import com.cashfluent.app.domain.game.num
import com.cashfluent.app.domain.game.numberRound
import com.cashfluent.app.domain.game.pct
import com.cashfluent.app.domain.game.pick
import com.cashfluent.app.domain.game.trueFalse
import kotlin.random.Random

/** Topic 09, tax nobody deducts for you. What arrived, what was taxable, what to move out. */
object SideIncomeGames {

    private const val TOPIC = "side-income"

    private class Gig(random: Random) {
        val monthly = random.pick(listOf(300.0, 500.0, 800.0, 1_200.0))
        val expenses = random.pick(listOf(600.0, 900.0, 1_500.0, 2_400.0))
        val rate = random.pick(listOf(0.20, 0.25, 0.30))
        val year = SideIncome.year(monthly, expenses, rate)
        val setAside = SideIncome.flatSetAside(monthly, rate)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "side-taxable", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What is taxable",
            blurb = "A year of gigs, minus what the work cost you. Profit, not turnover.",
            deal = ::taxable,
        ),
        MiniGame(
            id = "side-bill", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The bill",
            blurb = "The combined rate on the profit. Smaller than a quarter of what arrived.",
            deal = ::bill,
        ),
        MiniGame(
            id = "side-set-aside", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Move it the same day",
            blurb = "A payment lands. How much goes out of reach before you spend a cent?",
            deal = ::setAside,
        ),
        MiniGame(
            id = "side-real-rate", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "The real rate",
            blurb = "Headline rate against what you actually paid on the money that arrived.",
            deal = ::realRate,
        ),
        MiniGame(
            id = "side-cushion", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The cushion",
            blurb = "Hold back the flat rate all year. What is left when the bill comes?",
            deal = ::cushion,
        ),
        MiniGame(
            id = "side-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Gig claims",
            blurb = "Statements about invoices, wages and the bill that arrives a year later.",
            deal = ::claims,
        ),
    )

    private fun taxable(random: Random): Round {
        val g = Gig(random)
        return numberRound(
            prompt = "You're paid ${money(g.monthly)} a month for a year of weekend work, and ${money(g.expenses)} of it " +
                "went on gear and travel for that work. What is taxable?",
            quantity = Quantity.AMOUNT,
            truth = g.year.profit,
            random = random,
            explanation = "${num(g.monthly)} × 12 = ${money(g.year.gross)} arrived. ${num(g.year.gross)} − ${num(g.expenses)} = " +
                "${money(g.year.profit)} is profit, and profit is what gets taxed.",
        )
    }

    private fun bill(random: Random): Round {
        val g = Gig(random)
        return numberRound(
            prompt = "${money(g.year.gross)} arrived over the year, ${money(g.expenses)} of it spent on the work, combined " +
                "rate ${pct(g.rate)}. How big is the bill?",
            quantity = Quantity.AMOUNT,
            truth = g.year.taxDue,
            random = random,
            explanation = "Profit ${num(g.year.profit)} × ${pct(g.rate)} = ${money(g.year.taxDue)}. Not ${pct(g.rate)} of " +
                "what arrived — of what was left after costs.",
        )
    }

    private fun setAside(random: Random): Round {
        val g = Gig(random)
        return numberRound(
            prompt = "A ${money(g.monthly)} payment lands and your combined rate is ${pct(g.rate)}. How much do you move " +
                "out the same day?",
            quantity = Quantity.AMOUNT,
            truth = g.setAside,
            random = random,
            explanation = "${num(g.monthly)} × ${pct(g.rate)} = ${money(g.setAside)}, out of reach before you do anything " +
                "else. Twelve of those make ${money(g.setAside * 12)} — more than the ${money(g.year.taxDue)} bill.",
        )
    }

    private fun realRate(random: Random): Round {
        val g = Gig(random)
        val effective = g.year.effectiveRateOnGross
        val candidates = listOf(g.rate, effective, effective / 2).shuffled(random)
        return ChoiceRound(
            prompt = "Headline rate ${pct(g.rate)}, ${money(g.year.gross)} arrived, ${money(g.expenses)} of costs. What " +
                "rate did you actually pay on the money that arrived?",
            options = candidates.map { pct(it, 1) },
            correctIndex = candidates.indexOf(effective),
            explanation = "The bill is ${money(g.year.taxDue)} on ${money(g.year.gross)}: ${num(g.year.taxDue)} ÷ " +
                "${num(g.year.gross)} = ${pct(effective, 1)}. Costs pull the real rate under the headline one, every time.",
        )
    }

    private fun cushion(random: Random): Round {
        val g = Gig(random)
        val cushion = SideIncome.cushion(g.monthly, g.expenses, g.rate)
        return numberRound(
            prompt = "You hold back ${pct(g.rate)} of every ${money(g.monthly)} payment all year. When the " +
                "${money(g.year.taxDue)} bill arrives, how much is left over?",
            quantity = Quantity.AMOUNT,
            truth = cushion,
            random = random,
            explanation = "Held back: ${num(g.setAside)} × 12 = ${money(g.setAside * 12)}. Bill: ${money(g.year.taxDue)}. " +
                "The difference, ${money(cushion)}, is yours — the habit over-collects because costs make the true " +
                "bill smaller.",
        )
    }

    private fun claims(random: Random): Round {
        val g = Gig(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "${money(g.year.gross)} arrived from gigs and ${money(g.expenses)} went on gear for them. The " +
                    "whole ${money(g.year.gross)} is what gets taxed.",
                isTrue = false,
                explanation = "Tax applies to profit: ${num(g.year.gross)} − ${num(g.expenses)} = ${money(g.year.profit)}. " +
                    "Being taxed on the full amount would mean paying tax on money that was never yours to keep.",
            )
            1 -> trueFalse(
                prompt = "Holding back ${pct(g.rate)} of every payment, with ${money(g.expenses)} of real costs, leaves " +
                    "you short when the bill arrives.",
                isTrue = false,
                explanation = "Held back: ${money(g.setAside * 12)}. Bill: ${money(g.year.taxDue)}. Costs make the true " +
                    "bill smaller than the flat rate, so the habit over-collects by ${money(SideIncome.cushion(g.monthly, g.expenses, g.rate))}.",
            )
            else -> trueFalse(
                prompt = "A ${money(g.monthly)} invoice lands in full while a ${money(g.monthly)} wage arrives with less, " +
                    "because invoiced money is taxed at a lower rate.",
                isTrue = false,
                explanation = "More arrives because nobody deducted anything yet, not because the rate is lower. The " +
                    "employer collects tax before a wage reaches you; for the invoice, ${money(g.setAside)} of the " +
                    "${money(g.monthly)} is a bill that turns up later.",
            )
        }
    }
}
