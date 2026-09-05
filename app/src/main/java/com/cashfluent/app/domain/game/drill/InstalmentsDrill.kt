package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Instalments
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
import kotlin.random.Random

/** Module 07. A flat fee on a small amount for a short time, priced honestly. */
object InstalmentsDrill : Drill {

    override val moduleId = "instalments"

    private data class Slip(val fee: Double, val instalment: Double, val days: Int)

    /** Five slips whose yearly rates are all different, so a comparison has one answer. */
    private val slips = listOf(
        Slip(6.0, 30.0, 14), Slip(10.0, 50.0, 30), Slip(5.0, 20.0, 7), Slip(8.0, 40.0, 21), Slip(5.0, 100.0, 14),
    )

    override fun round(random: Random, index: Int): Round {
        val price = random.pick(listOf(80.0, 120.0, 200.0, 300.0, 450.0))
        val count = random.pick(listOf(3, 4, 6))
        val fee = random.pick(listOf(5.0, 6.0, 8.0, 10.0))
        val days = random.pick(listOf(7, 14, 21, 30))
        val instalment = Instalments.instalment(price, count)

        return when (index) {
            0 -> {
                val financed = Instalments.amountFinanced(price, count)
                numberRound(
                    prompt = "${money(price)} split into $count payments, the first one at the till. How much " +
                        "do you actually borrow?",
                    quantity = Quantity.AMOUNT,
                    truth = financed,
                    random = random,
                    explanation = "${num(price)} ÷ $count = ${money(instalment)} each. The first is paid on the " +
                        "spot, so only ${count - 1} × ${num(instalment)} = ${money(financed)} is ever lent to you.",
                )
            }

            1 -> {
                val annual = Instalments.effectiveAnnualRate(fee, instalment, days)
                val top = niceCeil(annual * 2)
                NumberRound(
                    prompt = "A ${money(fee)} late fee on a ${money(instalment)} instalment, settled $days days " +
                        "late. As a yearly rate, what is that?",
                    quantity = Quantity.PERCENT,
                    truth = annual,
                    min = 0.0, max = top, step = top / 100,
                    explanation = "${num(fee)} ÷ ${num(instalment)} = ${pct(fee / instalment)} for $days days. " +
                        "A year holds ${num(365.0 / days, 1)} such stretches: ${pct(fee / instalment)} × " +
                        "${num(365.0 / days, 1)} = ${pct(annual)} a year.",
                )
            }

            2 -> {
                val card = Instalments.cardInterestFor(instalment, 0.20, days)
                numberRound(
                    prompt = "The same ${money(instalment)} for the same $days days on an ordinary card at 20% " +
                        "APR. How much interest would that cost?",
                    quantity = Quantity.AMOUNT_CENTS,
                    truth = card,
                    random = random,
                    explanation = "${num(instalment)} × 0.20 ÷ 365 × $days = ${money(card, 2)}. The card everyone " +
                        "warns you about costs cents; the flat fee costs ${money(fee)}.",
                )
            }

            3 -> {
                val chosen = slips.shuffled(random).take(3)
                val rates = chosen.map { Instalments.effectiveAnnualRate(it.fee, it.instalment, it.days) }
                ChoiceRound(
                    prompt = "Three slips. Which one is the dearest, priced as a yearly rate?",
                    options = chosen.map {
                        "${money(it.fee)} fee on a ${money(it.instalment)} instalment, ${it.days} days late"
                    },
                    correctIndex = rates.indexOf(rates.max()),
                    explanation = chosen.zip(rates).joinToString(" ") { (slip, rate) ->
                        "${num(slip.fee)} on ${num(slip.instalment)} for ${slip.days} days: ${pct(rate)} a year."
                    } + " The smallest fee on the smallest amount for the shortest time prices worst.",
                )
            }

            else -> {
                val missed = random.pick(listOf(1, 2, 3)).coerceAtMost(count)
                val plan = Instalments.plan(price, count, fee, missed)
                val share = plan.feeShareOfPrice
                val candidates = listOf(share, share / 2, share * 2).shuffled(random)
                ChoiceRound(
                    prompt = "${money(price)} in $count instalments with a ${money(fee)} late fee. You miss " +
                        "$missed of them. What did the slips add, as a share of the price?",
                    options = candidates.map { pct(it, 1) },
                    correctIndex = candidates.indexOf(share),
                    explanation = "$missed × ${num(fee)} = ${money(plan.feesCharged)} on a ${money(price)} order: " +
                        "${num(plan.feesCharged)} ÷ ${num(price)} = ${pct(share, 1)}. You paid " +
                        "${money(plan.totalPaid)} for the thing.",
                )
            }
        }
    }
}
