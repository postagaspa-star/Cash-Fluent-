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
import com.cashfluent.app.domain.finance.Inflation
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.components.ChartLegend
import com.cashfluent.app.ui.components.ChartSeries
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.LineChart
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.theme.CashfluentTheme

@Composable
fun InflationSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var amount by rememberSaveable { mutableFloatStateOf(3_000f) }
    var years by rememberSaveable { mutableFloatStateOf(5f) }
    var inflation by rememberSaveable { mutableFloatStateOf(3f) }
    var accountRate by rememberSaveable { mutableFloatStateOf(0.5f) }
    var investRate by rememberSaveable { mutableFloatStateOf(6f) }

    val n = years.toInt()
    val principal = amount.toDouble()
    val cashReal = Inflation.realValueAfter(principal, accountRate / 100.0, inflation / 100.0, n)
    val investedReal = Inflation.realValueAfter(principal, investRate / 100.0, inflation / 100.0, n)
    val beatingInflation = accountRate >= inflation

    SimulatorScaffold(
        onReset = {
            amount = 3_000f; years = 5f; inflation = 3f; accountRate = 0.5f; investRate = 6f
        },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Amount",
            display = Money.amount(principal, currency),
            value = amount,
            onValueChange = { amount = it },
            valueRange = 100f..20_000f,
            step = 100f,
            minLabel = Money.amount(100.0, currency),
            maxLabel = Money.amount(20_000.0, currency),
        )
        LabeledSlider(
            label = "Years",
            display = n.toString(),
            value = years,
            onValueChange = { years = it },
            valueRange = 1f..30f,
            step = 1f,
            minLabel = "1",
            maxLabel = "30",
        )
        LabeledSlider(
            label = "Inflation",
            display = Money.percent(inflation / 100.0),
            value = inflation,
            onValueChange = { inflation = it },
            valueRange = 0f..8f,
            step = 0.5f,
            minLabel = "0%",
            maxLabel = "8%",
        )
        LabeledSlider(
            label = "Account rate",
            display = Money.percent(accountRate / 100.0, decimals = 2),
            value = accountRate,
            onValueChange = { accountRate = it },
            valueRange = 0f..4f,
            step = 0.25f,
            minLabel = "0%",
            maxLabel = "4%",
        )
        LabeledSlider(
            label = "Investment rate",
            display = Money.percent(investRate / 100.0),
            value = investRate,
            onValueChange = { investRate = it },
            valueRange = 0f..10f,
            step = 0.5f,
            minLabel = "0%",
            maxLabel = "10%",
        )

        ChartLegend(
            items = listOf(
                "invested" to colors.grow,
                "in the account" to colors.cost,
            ),
        )

        // Both lines are REAL value, not the number the bank shows. That is the module.
        LineChart(
            series = listOf(
                ChartSeries(
                    values = Inflation.realValueCurve(principal, accountRate / 100.0, inflation / 100.0, n)
                        .map { it.toFloat() },
                    color = colors.cost,
                ),
                ChartSeries(
                    values = Inflation.realValueCurve(principal, investRate / 100.0, inflation / 100.0, n)
                        .map { it.toFloat() },
                    color = colors.grow,
                ),
            ),
            contentDescription = "In today's money over $n years, the account falls to " +
                "${Money.amount(cashReal, currency)} while the investment reaches " +
                "${Money.amount(investedReal, currency)}.",
            startLabel = "today",
            endLabel = "$n years",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "In the account",
                value = Money.amount(cashReal, currency),
                valueColor = if (cashReal < principal) colors.cost else colors.ink,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Invested",
                value = Money.amount(investedReal, currency),
                valueColor = colors.grow,
                modifier = Modifier.weight(1f),
            )
        }

        PlainEnglishResult(
            text = if (beatingInflation) {
                "Your account is beating inflation — that's unusual. Worth checking it isn't " +
                    "a promotional rate that expires."
            } else {
                "In today's money, your ${Money.amount(principal, currency)} is worth " +
                    "${Money.amount(cashReal, currency)} after $n years."
            },
            tone = if (beatingInflation) Tone.GOOD else Tone.COST,
        )
    }
}
