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
import com.cashfluent.app.domain.finance.Debt
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.finance.Payoff
import com.cashfluent.app.ui.components.ChartSeries
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.LineChart
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.theme.CashfluentTheme
import kotlin.math.roundToInt

@Composable
fun DebtSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var balance by rememberSaveable { mutableStateOf(800f) }
    var apr by rememberSaveable { mutableStateOf(20f) }
    var payment by rememberSaveable { mutableStateOf(25f) }

    val result = Debt.payoff(balance.toDouble(), apr / 100.0, payment.toDouble())
    val cleared = result as? Payoff.Clears
    val stuck = result as? Payoff.NeverClears
    val curve = Debt.balanceCurve(balance.toDouble(), apr / 100.0, payment.toDouble(), maxMonths = 120)

    SimulatorScaffold(
        onReset = { balance = 800f; apr = 20f; payment = 25f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "What you owe",
            display = Money.amount(balance.toDouble(), currency),
            value = balance,
            onValueChange = { balance = it },
            valueRange = 100f..5_000f,
            step = 50f,
            minLabel = Money.amount(100.0, currency),
            maxLabel = Money.amount(5_000.0, currency),
        )
        LabeledSlider(
            label = "APR",
            display = Money.percent(apr / 100.0),
            value = apr,
            onValueChange = { apr = it },
            valueRange = 0f..30f,
            step = 0.5f,
            minLabel = "0%",
            maxLabel = "30%",
        )
        LabeledSlider(
            label = "What you pay each month",
            display = Money.amount(payment.toDouble(), currency),
            value = payment,
            onValueChange = { payment = it },
            valueRange = 5f..500f,
            step = 5f,
            minLabel = Money.amount(5.0, currency),
            maxLabel = Money.amount(500.0, currency),
        )

        LineChart(
            series = listOf(
                ChartSeries(
                    values = curve.map { it.toFloat() },
                    color = if (stuck != null) colors.cost else colors.grow,
                    filled = true,
                ),
            ),
            contentDescription = if (stuck != null) {
                "The balance never falls: it stays flat or climbs, because the payment is " +
                    "smaller than the interest."
            } else {
                "The balance falls from ${Money.amount(balance.toDouble(), currency)} to zero " +
                    "over ${cleared?.wholeMonths ?: 0} months."
            },
            startLabel = "now",
            endLabel = if (stuck != null) "never" else "${cleared?.wholeMonths ?: 0} months",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Months",
                value = if (stuck != null) "∞" else (cleared?.wholeMonths ?: 0).toString(),
                valueColor = if (stuck != null) colors.cost else colors.ink,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Total paid",
                value = if (stuck != null) "∞" else Money.amount(cleared?.totalPaid ?: 0.0, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Interest",
                value = if (stuck != null) "∞" else Money.amount(cleared?.totalInterest ?: 0.0, currency),
                valueColor = colors.cost,
                modifier = Modifier.weight(1f),
            )
        }

        // The most useful moment in the app: the payment that never clears anything.
        if (stuck != null) {
            PlainEnglishResult(
                text = "At this payment the balance never goes down. The interest alone is " +
                    "${Money.preciseAmount(stuck.monthlyInterest, currency)} a month.",
                tone = Tone.COST,
            )
        } else {
            val better = Debt.savingsFromPayingMore(balance.toDouble(), apr / 100.0, payment.toDouble(), 10.0)
            PlainEnglishResult(
                text = buildString {
                    append("At ${Money.amount(payment.toDouble(), currency)} a month this takes ")
                    append("${cleared?.wholeMonths ?: 0} months and costs you ")
                    append("${Money.amount(cleared?.totalInterest ?: 0.0, currency)} in interest.")
                    if (better != null && better.first >= 1.0) {
                        append(" Pay ${Money.amount(10.0, currency)} more a month and you finish ")
                        append("${better.first.roundToInt()} months sooner and save ")
                        append("${Money.amount(better.second, currency)}.")
                    }
                },
            )
        }
    }
}
