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
import com.cashfluent.app.domain.finance.Instalments
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.components.ComparisonBars
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.theme.CashfluentTheme
import kotlin.math.roundToInt

/**
 * Module 07. The two bars are the point: one flat fee against what the same borrowing
 * would have cost on the card everybody is warned about instead.
 */
@Composable
fun InstalmentsSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var price by rememberSaveable { mutableStateOf(120f) }
    var count by rememberSaveable { mutableStateOf(4f) }
    var fee by rememberSaveable { mutableStateOf(6f) }
    var daysLate by rememberSaveable { mutableStateOf(14f) }
    var missed by rememberSaveable { mutableStateOf(1f) }

    val instalmentCount = count.roundToInt()
    val missedCount = missed.roundToInt()
    val plan = Instalments.plan(price.toDouble(), instalmentCount, fee.toDouble(), missedCount)
    val annualRate = Instalments.effectiveAnnualRate(fee.toDouble(), plan.instalment, daysLate.roundToInt())
    val cardCost = Instalments.cardInterestFor(plan.instalment, apr = 0.20, days = daysLate.roundToInt())
    val onTime = missedCount == 0

    SimulatorScaffold(
        onReset = { price = 120f; count = 4f; fee = 6f; daysLate = 14f; missed = 1f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "What it costs",
            display = Money.amount(price.toDouble(), currency),
            value = price,
            onValueChange = { price = it },
            valueRange = 20f..1_000f,
            step = 10f,
            minLabel = Money.amount(20.0, currency),
            maxLabel = Money.amount(1_000.0, currency),
        )
        LabeledSlider(
            label = "Split into",
            display = "$instalmentCount payments",
            value = count,
            onValueChange = { count = it },
            valueRange = 2f..12f,
            step = 1f,
            minLabel = "2",
            maxLabel = "12",
        )
        LabeledSlider(
            label = "Payments you miss",
            display = if (onTime) "none" else missedCount.toString(),
            value = missed,
            onValueChange = { missed = it },
            valueRange = 0f..4f,
            step = 1f,
            minLabel = "0",
            maxLabel = "4",
        )
        LabeledSlider(
            label = "Late fee, each time",
            display = Money.amount(fee.toDouble(), currency),
            value = fee,
            onValueChange = { fee = it },
            valueRange = 0f..25f,
            step = 1f,
            minLabel = Money.amount(0.0, currency),
            maxLabel = Money.amount(25.0, currency),
        )
        LabeledSlider(
            label = "Days before you settle it",
            display = "${daysLate.roundToInt()} days",
            value = daysLate,
            onValueChange = { daysLate = it },
            valueRange = 1f..60f,
            step = 1f,
            minLabel = "1",
            maxLabel = "60",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Each payment",
                value = Money.amount(plan.instalment, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Fees",
                value = Money.amount(plan.feesCharged, currency),
                valueColor = if (onTime) colors.grow else colors.cost,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Added",
                value = Money.percent(plan.feeShareOfPrice, decimals = 1),
                valueColor = if (onTime) colors.grow else colors.cost,
                modifier = Modifier.weight(1f),
            )
        }

        TotalStrip(
            label = "You end up paying",
            value = Money.amount(plan.totalPaid, currency),
            tone = if (onTime) Tone.GOOD else Tone.COST,
        )

        // The comparison the plan never offers you: its fee, priced the way a lender
        // would have to price it.
        if (!onTime && fee > 0f) {
            ComparisonBars(
                leftLabel = "An ordinary credit card",
                leftValue = 0.20f,
                leftDisplay = "20% a year",
                rightLabel = "This plan, one fee",
                rightValue = annualRate.toFloat(),
                rightDisplay = "${Money.percent(annualRate, decimals = 0)} a year",
                contentDescription = "As a yearly rate the late fee works out at " +
                    "${Money.percent(annualRate, decimals = 0)}, against 20% on an ordinary " +
                    "credit card — the card is barely visible next to it.",
            )
        }

        PlainEnglishResult(
            text = if (onTime) {
                "Every payment on time: ${Money.amount(plan.price, currency)} split into " +
                    "$instalmentCount, and the plan costs you nothing. This is the honest case."
            } else {
                buildString {
                    append("Missing $missedCount ")
                    append(if (missedCount == 1) "payment adds " else "payments add ")
                    append("${Money.amount(plan.feesCharged, currency)} — ")
                    append("${Money.percent(plan.feeShareOfPrice, decimals = 1)} on top of the price. ")
                    append("Priced as a loan, one fee is ${Money.percent(annualRate, decimals = 0)} a year. ")
                    append("The same ${Money.amount(plan.instalment, currency)} for ")
                    append("${daysLate.roundToInt()} days on a 20% card: ")
                    append("${Money.preciseAmount(cardCost, currency)}.")
                }
            },
            tone = if (onTime) Tone.GOOD else Tone.COST,
        )
    }
}
