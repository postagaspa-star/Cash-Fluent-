package com.cashfluent.app.ui.simulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Tone
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Fees
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.components.ComparisonBars
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.theme.CashfluentTheme

@Composable
fun FeesSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var monthly by rememberSaveable { mutableFloatStateOf(150f) }
    var years by rememberSaveable { mutableFloatStateOf(30f) }
    var gross by rememberSaveable { mutableFloatStateOf(7f) }
    var ter by rememberSaveable { mutableFloatStateOf(1.2f) }

    val n = years.toInt()
    val yours = Fees.outcome(monthly.toDouble(), gross / 100.0, ter / 100.0, n)
    val cheap = Fees.outcome(monthly.toDouble(), gross / 100.0, Fees.BENCHMARK_TER, n)
    val difference = cheap.finalValue - yours.finalValue

    SimulatorScaffold(
        onReset = { monthly = 150f; years = 30f; gross = 7f; ter = 1.2f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Monthly amount",
            display = Money.amount(monthly.toDouble(), currency),
            value = monthly,
            onValueChange = { monthly = it },
            valueRange = 10f..1_000f,
            step = 10f,
            minLabel = Money.amount(10.0, currency),
            maxLabel = Money.amount(1_000.0, currency),
        )
        LabeledSlider(
            label = "Years",
            display = n.toString(),
            value = years,
            onValueChange = { years = it },
            valueRange = 5f..40f,
            step = 1f,
            minLabel = "5",
            maxLabel = "40",
        )
        LabeledSlider(
            label = "Gross yearly return",
            display = Money.percent(gross / 100.0),
            value = gross,
            onValueChange = { gross = it },
            valueRange = 2f..10f,
            step = 0.5f,
            minLabel = "2%",
            maxLabel = "10%",
        )
        LabeledSlider(
            label = "Fund fee (TER)",
            display = Money.percent(ter / 100.0, decimals = 2),
            value = ter,
            onValueChange = { ter = it },
            valueRange = 0f..2.5f,
            step = 0.05f,
            minLabel = "0%",
            maxLabel = "2.5%",
        )

        ComparisonBars(
            leftLabel = "A 0.20% fund",
            leftValue = cheap.finalValue.toFloat(),
            leftDisplay = Money.amount(cheap.finalValue, currency),
            rightLabel = "Your ${Money.percent(ter / 100.0, decimals = 2)} fund",
            rightValue = yours.finalValue.toFloat(),
            rightDisplay = Money.amount(yours.finalValue, currency),
            contentDescription = "A cheap fund ends at ${Money.amount(cheap.finalValue, currency)} " +
                "and yours at ${Money.amount(yours.finalValue, currency)}.",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "You paid in",
                value = Money.amount(yours.contributed, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Paid in fees",
                value = Money.amount(yours.feesPaid, currency),
                valueColor = colors.cost,
                modifier = Modifier.weight(1f),
            )
        }

        PlainEnglishResult(
            text = if (difference < 1.0) {
                "You're already in a cheap fund. Over $n years the fee costs you almost nothing."
            } else {
                "That ${Money.percent(ter / 100.0, decimals = 2)} fee costs you " +
                    "${Money.amount(difference, currency)} over $n years, compared with a " +
                    "0.20% fund holding the same companies."
            },
            tone = if (difference < 1.0) Tone.GOOD else Tone.COST,
        )
    }
}
