package com.cashfluent.app.ui.simulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Tone
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.finance.Mortgage
import com.cashfluent.app.ui.components.ComparisonBars
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.theme.CashfluentTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Module 10. The two bars compare what each choice actually costs over the years you
 * stay: rent paid, against everything owning took out minus the part of the place you
 * now own. Comparing the rent with the mortgage payment is the mistake both sides of
 * this argument make, so the panel refuses to draw that comparison at all.
 *
 * Which bar is green follows the answer rather than the position, because the answer
 * changes sides as the sliders move — that is the entire lesson.
 */
@Composable
fun MortgageSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var price by rememberSaveable { mutableStateOf(200_000f) }
    var depositPercent by rememberSaveable { mutableStateOf(10f) }
    var rate by rememberSaveable { mutableStateOf(4.5f) }
    var rent by rememberSaveable { mutableStateOf(950f) }
    var stayYears by rememberSaveable { mutableStateOf(5f) }

    val term = 30
    val depositFraction = depositPercent / 100.0
    val annualRate = rate / 100.0
    val years = stayYears.roundToInt()

    val cost = Mortgage.cost(price.toDouble(), depositFraction, annualRate, term)
    val comparison = Mortgage.compare(
        price = price.toDouble(),
        depositFraction = depositFraction,
        annualRate = annualRate,
        termYears = term,
        monthlyRent = rent.toDouble(),
        overYears = years,
    )
    val owningWins = comparison.net > 0.0

    SimulatorScaffold(
        onReset = {
            price = 200_000f; depositPercent = 10f; rate = 4.5f; rent = 950f; stayYears = 5f
        },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Price of the place",
            display = Money.amount(price.toDouble(), currency),
            value = price,
            onValueChange = { price = it },
            valueRange = 50_000f..600_000f,
            step = 10_000f,
            minLabel = Money.amount(50_000.0, currency),
            maxLabel = Money.amount(600_000.0, currency),
        )
        LabeledSlider(
            label = "Deposit",
            display = "${depositPercent.roundToInt()}% · ${Money.amount(cost.deposit, currency)}",
            value = depositPercent,
            onValueChange = { depositPercent = it },
            valueRange = 5f..40f,
            step = 1f,
            minLabel = "5%",
            maxLabel = "40%",
        )
        LabeledSlider(
            label = "Yearly rate, over 30 years",
            display = Money.percent(annualRate, decimals = 2),
            value = rate,
            onValueChange = { rate = it },
            valueRange = 0.5f..10f,
            step = 0.1f,
            minLabel = "0.5%",
            maxLabel = "10%",
        )
        LabeledSlider(
            label = "Rent for the same place",
            display = Money.amount(rent.toDouble(), currency),
            value = rent,
            onValueChange = { rent = it },
            valueRange = 300f..3_000f,
            step = 25f,
            minLabel = Money.amount(300.0, currency),
            maxLabel = Money.amount(3_000.0, currency),
        )
        LabeledSlider(
            label = "How long you stay",
            display = if (years == 1) "1 year" else "$years years",
            value = stayYears,
            onValueChange = { stayYears = it },
            valueRange = 1f..30f,
            step = 1f,
            minLabel = "1",
            maxLabel = "30",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Payment",
                value = Money.amount(cost.monthlyPayment, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Upkeep",
                value = Money.amount(cost.monthlyMaintenance, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Upfront",
                value = Money.amount(cost.upfront, currency),
                valueColor = colors.cost,
                modifier = Modifier.weight(1f),
            )
        }

        TotalStrip(
            label = "Owning costs, per month",
            value = Money.amount(cost.monthlyTotal, currency),
            tone = Tone.COST,
        )

        // Cash out minus what you get to keep. Comparing the payments alone is the
        // mistake both sides of this argument make.
        val rentingCost = comparison.rentCashOut
        val owningCost = comparison.ownCashOut - comparison.equity

        ComparisonBars(
            leftLabel = "Renting, $years ${if (years == 1) "year" else "years"}",
            leftValue = rentingCost.toFloat(),
            leftDisplay = Money.amount(rentingCost, currency),
            rightLabel = "Owning, same $years",
            rightValue = owningCost.toFloat(),
            rightDisplay = Money.amount(owningCost, currency),
            contentDescription = "Over $years years renting costs " +
                "${Money.amount(rentingCost, currency)}. Owning costs " +
                "${Money.amount(owningCost, currency)} once the " +
                "${Money.amount(comparison.equity, currency)} of the place you now own is " +
                "taken off the ${Money.amount(comparison.ownCashOut, currency)} you paid out.",
            highlightLeft = !owningWins,
        )

        // With a high enough rent against a cheap enough place, owning takes less cash
        // out than renting does, and the sentence has to say "less" — not "-X more".
        val extraCash = comparison.extraCashOut
        val draw = abs(comparison.net) < cost.monthlyTotal

        PlainEnglishResult(
            text = buildString {
                append("Over $years ${if (years == 1) "year" else "years"} owning costs ")
                append(Money.amount(abs(extraCash), currency))
                append(if (extraCash >= 0.0) " more in cash, and " else " less in cash, and ")
                append("leaves you owning ${Money.amount(comparison.equity, currency)} of the ")
                append("place. ")
                append(
                    when {
                        draw ->
                            "That's a draw, to within a month's payment — at this point it " +
                                "isn't a money question at all."
                        owningWins ->
                            "Buying is ahead by ${Money.amount(comparison.net, currency)}, " +
                                "before the price of the place moves either way."
                        else ->
                            "Renting is ahead by ${Money.amount(-comparison.net, currency)}: " +
                                "you haven't stayed long enough to earn back what it cost to " +
                                "get in."
                    },
                )
            },
            // A draw is not a win, so it does not get the green.
            tone = if (owningWins && !draw) Tone.GOOD else null,
        )
    }
}
