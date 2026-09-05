package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Instalments
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
import com.cashfluent.app.domain.game.trueFalse
import kotlin.random.Random

/** Topic 07, buy now pay later. A flat fee on a small amount for a short time, priced honestly. */
object InstalmentsGames {

    private const val TOPIC = "instalments"

    private data class Slip(val fee: Double, val instalment: Double, val days: Int)

    /** Five slips whose yearly rates are all different, so a comparison has one answer. */
    private val slips = listOf(
        Slip(6.0, 30.0, 14), Slip(10.0, 50.0, 30), Slip(5.0, 20.0, 7), Slip(8.0, 40.0, 21), Slip(5.0, 100.0, 14),
    )

    private class Plan(random: Random) {
        val price = random.pick(listOf(80.0, 120.0, 200.0, 300.0, 450.0))
        val count = random.pick(listOf(3, 4, 6))
        val fee = random.pick(listOf(5.0, 6.0, 8.0, 10.0))
        val days = random.pick(listOf(7, 14, 21, 30))
        val instalment = Instalments.instalment(price, count)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "bnpl-borrowed", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What you actually borrow",
            blurb = "The first instalment is paid at the till. So how much is ever lent to you?",
            deal = ::borrowed,
        ),
        MiniGame(
            id = "bnpl-annual-rate", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The fee as a yearly rate",
            blurb = "A small fee, a small amount, a short time. Price it the way a lender must.",
            deal = ::annualRate,
        ),
        MiniGame(
            id = "bnpl-card-cost", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "The card nobody warns you about",
            blurb = "The same money for the same days at 20% APR. Cents, not pounds.",
            deal = ::cardCost,
        ),
        MiniGame(
            id = "bnpl-dearest-slip", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Dearest slip",
            blurb = "Three late fees. Which one is the worst deal, annualised?",
            deal = ::dearestSlip,
        ),
        MiniGame(
            id = "bnpl-fee-share", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "Added to the price",
            blurb = "Miss a few payments. What did the slips add, as a share of the thing?",
            deal = ::feeShare,
        ),
        MiniGame(
            id = "bnpl-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "0%, true or false",
            blurb = "Claims about pay-in-four plans. Some of them are genuinely true.",
            deal = ::claims,
        ),
    )

    private fun borrowed(random: Random): Round {
        val p = Plan(random)
        val financed = Instalments.amountFinanced(p.price, p.count)
        return numberRound(
            prompt = "${money(p.price)} split into ${p.count} payments, the first one at the till. How much do you " +
                "actually borrow?",
            quantity = Quantity.AMOUNT,
            truth = financed,
            random = random,
            explanation = "${num(p.price)} ÷ ${p.count} = ${money(p.instalment)} each. The first is paid on the spot, so " +
                "only ${p.count - 1} × ${num(p.instalment)} = ${money(financed)} is ever lent to you.",
        )
    }

    private fun annualRate(random: Random): Round {
        val p = Plan(random)
        val annual = Instalments.effectiveAnnualRate(p.fee, p.instalment, p.days)
        val top = niceCeil(annual * 2)
        return NumberRound(
            prompt = "A ${money(p.fee)} late fee on a ${money(p.instalment)} instalment, settled ${p.days} days late. " +
                "As a yearly rate, what is that?",
            quantity = Quantity.PERCENT,
            truth = annual,
            min = 0.0, max = top, step = top / 100,
            explanation = "${num(p.fee)} ÷ ${num(p.instalment)} = ${pct(p.fee / p.instalment)} for ${p.days} days. A " +
                "year holds ${num(365.0 / p.days, 1)} such stretches: ${pct(p.fee / p.instalment)} × " +
                "${num(365.0 / p.days, 1)} = ${pct(annual)} a year.",
        )
    }

    private fun cardCost(random: Random): Round {
        val p = Plan(random)
        val card = Instalments.cardInterestFor(p.instalment, 0.20, p.days)
        return numberRound(
            prompt = "The same ${money(p.instalment)} for the same ${p.days} days on an ordinary card at 20% APR. How " +
                "much interest would that cost?",
            quantity = Quantity.AMOUNT_CENTS,
            truth = card,
            random = random,
            explanation = "${num(p.instalment)} × 0.20 ÷ 365 × ${p.days} = ${money(card, 2)}. The card everyone warns " +
                "you about costs cents; the flat fee costs ${money(p.fee)}.",
        )
    }

    private fun dearestSlip(random: Random): Round {
        val chosen = slips.shuffled(random).take(3)
        val rates = chosen.map { Instalments.effectiveAnnualRate(it.fee, it.instalment, it.days) }
        return ChoiceRound(
            prompt = "Three slips. Which one is the dearest, priced as a yearly rate?",
            options = chosen.map { "${money(it.fee)} fee on a ${money(it.instalment)} instalment, ${it.days} days late" },
            correctIndex = rates.indexOf(rates.max()),
            explanation = chosen.zip(rates).joinToString(" ") { (slip, rate) ->
                "${num(slip.fee)} on ${num(slip.instalment)} for ${slip.days} days: ${pct(rate)} a year."
            } + " The smallest fee on the smallest amount for the shortest time prices worst.",
        )
    }

    private fun feeShare(random: Random): Round {
        val p = Plan(random)
        val missed = random.pick(listOf(1, 2, 3)).coerceAtMost(p.count)
        val plan = Instalments.plan(p.price, p.count, p.fee, missed)
        val share = plan.feeShareOfPrice
        val candidates = listOf(share, share / 2, share * 2).shuffled(random)
        return ChoiceRound(
            prompt = "${money(p.price)} in ${p.count} instalments with a ${money(p.fee)} late fee. You miss $missed of " +
                "them. What did the slips add, as a share of the price?",
            options = candidates.map { pct(it, 1) },
            correctIndex = candidates.indexOf(share),
            explanation = "$missed × ${num(p.fee)} = ${money(plan.feesCharged)} on a ${money(p.price)} order: " +
                "${num(plan.feesCharged)} ÷ ${num(p.price)} = ${pct(share, 1)}. You paid ${money(plan.totalPaid)} " +
                "for the thing.",
        )
    }

    private fun claims(random: Random): Round {
        val p = Plan(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "Paid on time, a pay-in-${p.count} plan for ${money(p.price)} costs you nothing at all.",
                isTrue = true,
                explanation = "The 0% is real. The provider is paid by the shop, and by the people who miss a payment. " +
                    "${p.count} × ${money(p.instalment)} = ${money(p.price)}, fees ${money(0.0)}.",
            )
            1 -> {
                val annual = Instalments.effectiveAnnualRate(p.fee, p.instalment, p.days)
                val claimed = random.pick(listOf(0.20, 1.0, 5.0))
                trueFalse(
                    prompt = "A ${money(p.fee)} fee on a ${money(p.instalment)} instalment settled ${p.days} days late " +
                        "works out below ${pct(claimed)} a year.",
                    isTrue = annual < claimed,
                    explanation = "${num(p.fee)} ÷ ${num(p.instalment)} × 365 ÷ ${p.days} = ${pct(annual)} a year. " +
                        "${if (annual < claimed) "Below" else "Not below"} ${pct(claimed)}.",
                )
            }
            else -> trueFalse(
                prompt = "Four plans of ${money(100.0)} on different dates carry the same risk as one plan of ${money(400.0)}.",
                isTrue = false,
                explanation = "The total is ${money(400.0)} either way; the risk is not. 4 dates to track means 4 " +
                    "chances of a ${money(p.fee)} fee, and each ${money(100.0)} plan was easy to say yes to precisely " +
                    "because it was small.",
            )
        }
    }
}
