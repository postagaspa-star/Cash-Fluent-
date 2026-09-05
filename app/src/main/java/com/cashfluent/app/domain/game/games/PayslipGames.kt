package com.cashfluent.app.domain.game.games

import com.cashfluent.app.domain.finance.Payslip
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

/** Topic 06, gross versus net. On the same illustrative bands the lesson uses. */
object PayslipGames {

    private const val TOPIC = "payslip"
    private const val BANDS = "0–8,000 at 0%, 8,000–20,000 at 15%, 20,000–40,000 at 25%, above that 35%"

    private class Slip(random: Random) {
        val gross = random.pick(listOf(18_000.0, 24_000.0, 30_000.0, 36_000.0, 45_000.0))
        val c = random.pick(listOf(0.07, 0.09, 0.11))
        val slip = Payslip.compute(gross, c)
    }

    val all: List<MiniGame> = listOf(
        MiniGame(
            id = "payslip-contributions", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Contributions first",
            blurb = "The flat slice that comes off before any tax is worked out.",
            deal = ::contributions,
        ),
        MiniGame(
            id = "payslip-tax", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Tax by the band",
            blurb = "Run the bands on a taxable income. Each rate touches only its slice.",
            deal = ::tax,
        ),
        MiniGame(
            id = "payslip-monthly-net", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "What lands each month",
            blurb = "From the number in the job ad to the number in your account.",
            deal = ::monthlyNet,
        ),
        MiniGame(
            id = "payslip-marginal", topicId = TOPIC, mechanic = Mechanic.PICK,
            title = "The next unit",
            blurb = "What rate does the next unit you earn pay? Not the average.",
            deal = ::marginal,
        ),
        MiniGame(
            id = "payslip-raise", topicId = TOPIC, mechanic = Mechanic.SLIDER,
            title = "Keep the raise",
            blurb = "A raise arrives. How much of it reaches your account over the year?",
            deal = ::raise,
        ),
        MiniGame(
            id = "payslip-true-false", topicId = TOPIC, mechanic = Mechanic.TRUE_FALSE,
            title = "Payslip myths",
            blurb = "Claims about raises, bands and take-home. Kill them with arithmetic.",
            deal = ::claims,
        ),
    )

    private fun contributions(random: Random): Round {
        val s = Slip(random)
        return numberRound(
            prompt = "Gross pay ${money(s.gross)} a year, contributions at ${pct(s.c)}. How much goes to contributions?",
            quantity = Quantity.AMOUNT,
            truth = s.slip.contributions,
            random = random,
            explanation = "${num(s.gross)} × ${pct(s.c)} = ${money(s.slip.contributions)}. They come off first, before " +
                "any tax is worked out.",
        )
    }

    private fun tax(random: Random): Round {
        val s = Slip(random)
        return numberRound(
            prompt = "Taxable income ${money(s.slip.taxableIncome)}, on illustrative bands: $BANDS. How much income tax?",
            quantity = Quantity.AMOUNT,
            truth = s.slip.tax,
            random = random,
            explanation = bandByBand(s.slip.taxableIncome) + " = ${money(s.slip.tax)}. Each rate touches only the " +
                "slice inside its band.",
        )
    }

    private fun monthlyNet(random: Random): Round {
        val s = Slip(random)
        return numberRound(
            prompt = "Gross ${money(s.gross)}, contributions ${pct(s.c)}, tax on illustrative bands ($BANDS). What " +
                "lands in your account each month?",
            quantity = Quantity.AMOUNT_CENTS,
            truth = s.slip.monthlyNet,
            random = random,
            explanation = "${num(s.gross)} − ${num(s.slip.contributions)} − ${num(s.slip.tax)} = ${money(s.slip.net)} " +
                "a year, ÷ 12 = ${money(s.slip.monthlyNet, 2)} a month.",
        )
    }

    private fun marginal(random: Random): Round {
        val s = Slip(random)
        val marginal = Payslip.marginalRate(s.slip.taxableIncome)
        val rates = listOf(0.0, 0.15, 0.25, 0.35)
        return ChoiceRound(
            prompt = "Taxable income ${money(s.slip.taxableIncome)} on these bands: $BANDS. What rate does the next " +
                "unit you earn pay?",
            options = rates.map { pct(it) },
            correctIndex = rates.indexOf(marginal),
            explanation = "${num(s.slip.taxableIncome)} sits in the ${pct(marginal)} band, so the next unit pays " +
                "${pct(marginal)} — while your average deduction is only ${pct(s.slip.averageRate, 1)}.",
        )
    }

    private fun raise(random: Random): Round {
        val s = Slip(random)
        val raise = random.pick(listOf(1_000.0, 2_000.0, 3_000.0))
        val gain = Payslip.netGainFromRaise(s.gross, raise, s.c)
        val after = Payslip.compute(s.gross + raise, s.c)
        return numberRound(
            prompt = "On ${money(s.gross)} gross with ${pct(s.c)} contributions you get a ${money(raise)} raise. How " +
                "much more reaches your account over the year?",
            quantity = Quantity.AMOUNT,
            truth = gain,
            random = random,
            explanation = "Net before: ${money(s.slip.net)}. Net after: ${money(after.net)}. You keep ${money(gain)} of " +
                "the ${money(raise)} — ${pct(gain / raise, 0)} of it, and never less than nothing.",
        )
    }

    private fun claims(random: Random): Round {
        val s = Slip(random)
        return when (random.nextInt(3)) {
            0 -> trueFalse(
                prompt = "On ${money(s.gross)} gross, a ${money(2_000.0)} raise that moves you into a higher band can " +
                    "leave you with less money in your pocket.",
                isTrue = false,
                explanation = "The higher rate applies only to the slice inside that band; nothing already earned is " +
                    "taxed again. On ${money(s.gross)} a ${money(2_000.0)} raise keeps " +
                    "${money(Payslip.netGainFromRaise(s.gross, 2_000.0, s.c))}.",
            )
            1 -> {
                val claimedMonthly = random.pick(listOf(s.gross / 12, s.slip.monthlyNet))
                val isNet = claimedMonthly == s.slip.monthlyNet
                trueFalse(
                    prompt = "On ${money(s.gross)} gross with ${pct(s.c)} contributions, about ${money(claimedMonthly)} " +
                        "arrives each month.",
                    isTrue = isNet,
                    explanation = "Net is ${money(s.slip.net)} a year, ${money(s.slip.monthlyNet, 2)} a month. " +
                        if (isNet) "That is the number." else "${money(s.gross / 12)} is the gross divided by twelve — the mistake the topic exists to prevent.",
                )
            }
            else -> trueFalse(
                prompt = "On ${money(s.gross)} gross your average deduction is higher than your marginal rate.",
                isTrue = s.slip.averageRate > Payslip.marginalRate(s.slip.taxableIncome),
                explanation = "Average: ${pct(s.slip.averageRate, 1)} of everything. Marginal: " +
                    "${pct(Payslip.marginalRate(s.slip.taxableIncome))} on the next unit. With an untaxed first " +
                    "slice, the average sits below the marginal rate.",
            )
        }
    }

    /** "8,000 × 0% + 8,380 × 15%" — the slices, so the reader can see the bands working. */
    private fun bandByBand(taxable: Double): String {
        val parts = ArrayList<String>()
        var floor = 0.0
        for (band in Payslip.ILLUSTRATIVE_BANDS) {
            if (taxable <= floor) break
            val ceiling = band.upTo ?: Double.MAX_VALUE
            val slice = minOf(taxable, ceiling) - floor
            parts += "${num(slice)} × ${pct(band.rate)}"
            floor = ceiling
        }
        return parts.joinToString(" + ")
    }
}
