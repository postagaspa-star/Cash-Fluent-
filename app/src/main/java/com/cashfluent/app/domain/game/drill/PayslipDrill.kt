package com.cashfluent.app.domain.game.drill

import com.cashfluent.app.domain.finance.Payslip
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

/** Module 06. Gross to net, on the same illustrative bands the lesson uses. */
object PayslipDrill : Drill {

    override val moduleId = "payslip"

    private const val BANDS = "0–8,000 at 0%, 8,000–20,000 at 15%, 20,000–40,000 at 25%, above that 35%"

    override fun round(random: Random, index: Int): Round {
        val gross = random.pick(listOf(18_000.0, 24_000.0, 30_000.0, 36_000.0, 45_000.0))
        val c = random.pick(listOf(0.07, 0.09, 0.11))
        val slip = Payslip.compute(gross, c)

        return when (index) {
            0 -> numberRound(
                prompt = "Gross pay ${money(gross)} a year, contributions at ${pct(c)}. How much goes to " +
                    "contributions?",
                quantity = Quantity.AMOUNT,
                truth = slip.contributions,
                random = random,
                explanation = "${num(gross)} × ${pct(c)} = ${money(slip.contributions)}. They come off first, " +
                    "before any tax is worked out.",
            )

            1 -> numberRound(
                prompt = "Taxable income ${money(slip.taxableIncome)}, on illustrative bands: $BANDS. How " +
                    "much income tax?",
                quantity = Quantity.AMOUNT,
                truth = slip.tax,
                random = random,
                explanation = bandByBand(slip.taxableIncome) + " = ${money(slip.tax)}. Each rate touches only " +
                    "the slice inside its band.",
            )

            2 -> numberRound(
                prompt = "Gross ${money(gross)}, contributions ${pct(c)}, tax on illustrative bands ($BANDS). " +
                    "What lands in your account each month?",
                quantity = Quantity.AMOUNT_CENTS,
                truth = slip.monthlyNet,
                random = random,
                explanation = "${num(gross)} − ${num(slip.contributions)} − ${num(slip.tax)} = ${money(slip.net)} " +
                    "a year, ÷ 12 = ${money(slip.monthlyNet, 2)} a month.",
            )

            3 -> {
                val marginal = Payslip.marginalRate(slip.taxableIncome)
                val rates = listOf(0.0, 0.15, 0.25, 0.35)
                ChoiceRound(
                    prompt = "Taxable income ${money(slip.taxableIncome)} on these bands: $BANDS. What rate " +
                        "does the next unit you earn pay?",
                    options = rates.map { pct(it) },
                    correctIndex = rates.indexOf(marginal),
                    explanation = "${num(slip.taxableIncome)} sits in the ${pct(marginal)} band, so the next unit " +
                        "pays ${pct(marginal)} — while your average deduction is only ${pct(slip.averageRate, 1)}.",
                )
            }

            else -> {
                val raise = random.pick(listOf(1_000.0, 2_000.0, 3_000.0))
                val gain = Payslip.netGainFromRaise(gross, raise, c)
                val after = Payslip.compute(gross + raise, c)
                numberRound(
                    prompt = "On ${money(gross)} gross with ${pct(c)} contributions you get a ${money(raise)} " +
                        "raise. How much more reaches your account over the year?",
                    quantity = Quantity.AMOUNT,
                    truth = gain,
                    random = random,
                    explanation = "Net before: ${money(slip.net)}. Net after: ${money(after.net)}. You keep " +
                        "${money(gain)} of the ${money(raise)} — ${pct(gain / raise, 0)} of it, and never " +
                        "less than nothing.",
                )
            }
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
