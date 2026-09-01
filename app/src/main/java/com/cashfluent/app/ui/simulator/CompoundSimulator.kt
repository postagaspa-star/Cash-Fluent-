package com.cashfluent.app.ui.simulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Tone
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.finance.SavingPlan
import com.cashfluent.app.ui.components.ChartLegend
import com.cashfluent.app.ui.components.ChartSeries
import com.cashfluent.app.ui.components.LineChart
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.theme.CashfluentTheme

private const val HORIZON = 58

@Composable
fun CompoundSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var monthly by rememberSaveable { mutableFloatStateOf(100f) }
    var startAge by rememberSaveable { mutableFloatStateOf(18f) }
    var stopAge by rememberSaveable { mutableFloatStateOf(28f) }
    var rate by rememberSaveable { mutableFloatStateOf(7f) }

    // Dragging "stop" below "start" is easy to do by accident, so it is corrected rather
    // than rejected — the plan simply pays for zero months and says so.
    val start = startAge.toInt()
    val stop = stopAge.toInt().coerceAtLeast(start)
    val paysNothing = stop == start

    val plan = SavingPlan(
        monthlyAmount = monthly.toDouble(),
        startAge = start,
        stopAge = stop,
        annualRate = rate / 100.0,
    )
    val curve = plan.curve(HORIZON)
    val total = plan.valueAt(HORIZON)
    val contributed = plan.totalContributed
    val added = total - contributed

    SimulatorScaffold(
        onReset = {
            monthly = 100f; startAge = 18f; stopAge = 28f; rate = 7f
        },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Monthly amount",
            display = Money.amount(monthly.toDouble(), currency),
            value = monthly,
            onValueChange = { monthly = it },
            valueRange = 10f..500f,
            step = 10f,
            minLabel = Money.amount(10.0, currency),
            maxLabel = Money.amount(500.0, currency),
        )
        LabeledSlider(
            label = "Start at age",
            display = start.toString(),
            value = startAge,
            onValueChange = { startAge = it },
            valueRange = 16f..40f,
            step = 1f,
            minLabel = "16",
            maxLabel = "40",
        )
        LabeledSlider(
            label = "Stop paying at age",
            display = stop.toString(),
            value = stopAge,
            onValueChange = { stopAge = it },
            valueRange = 17f..60f,
            step = 1f,
            minLabel = "17",
            maxLabel = "60",
        )
        LabeledSlider(
            label = "Yearly return",
            display = Money.percent(rate / 100.0),
            value = rate,
            onValueChange = { rate = it },
            valueRange = 2f..10f,
            step = 0.5f,
            minLabel = "2%",
            maxLabel = "10%",
        )

        ChartLegend(
            items = listOf(
                "what it's worth" to colors.grow,
                "what you put in" to colors.muted,
            ),
        )

        LineChart(
            series = listOf(
                ChartSeries(
                    values = curve.map { it.contributed.toFloat() },
                    color = colors.muted,
                    dashed = true,
                ),
                ChartSeries(
                    values = curve.map { it.value.toFloat() },
                    color = colors.grow,
                    filled = true,
                ),
            ),
            contentDescription = "From age $start to $HORIZON, what you put in stops at " +
                "${Money.amount(contributed, currency)} while the total keeps climbing to " +
                Money.amount(total, currency) + ".",
            startLabel = "age $start",
            endLabel = "age $HORIZON",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "You put in",
                value = Money.amount(contributed, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Time added",
                value = Money.amount(added, currency),
                valueColor = colors.grow,
                modifier = Modifier.weight(1f),
            )
        }

        TotalStrip(
            label = "Total at $HORIZON",
            value = Money.amount(total, currency),
            tone = Tone.GOOD,
        )

        Column {
            PlainEnglishResult(
                text = if (paysNothing) {
                    "Move the second slider past the first — right now you aren't paying " +
                        "anything in."
                } else {
                    "At ${Money.percent(rate / 100.0)}, ${Money.amount(monthly.toDouble(), currency)} " +
                        "a month from $start to $stop becomes ${Money.amount(total, currency)} " +
                        "by age $HORIZON."
                },
                tone = if (paysNothing) Tone.COST else null,
            )
        }
    }
}
