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
import com.cashfluent.app.domain.finance.Budget
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.components.BarPart
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.StackedBar
import com.cashfluent.app.ui.theme.CashfluentTheme

@Composable
fun BudgetSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var income by rememberSaveable { mutableFloatStateOf(1_200f) }
    var needs by rememberSaveable { mutableFloatStateOf(670f) }
    var wants by rememberSaveable { mutableFloatStateOf(390f) }

    val net = income.toDouble()
    val actual = Budget.actual(net, needs.toDouble(), wants.toDouble())
    val shares = Budget.shares(actual, net)
    val overspending = Budget.isOverspending(net, needs.toDouble(), wants.toDouble())
    val gap = Budget.futureGapPerMonth(net, actual.future)

    SimulatorScaffold(
        onReset = { income = 1_200f; needs = 670f; wants = 390f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Monthly net income",
            display = Money.amount(net, currency),
            value = income,
            onValueChange = { income = it },
            valueRange = 100f..4_000f,
            step = 50f,
            minLabel = Money.amount(100.0, currency),
            maxLabel = Money.amount(4_000.0, currency),
        )
        LabeledSlider(
            label = "What you spend on needs",
            display = Money.amount(needs.toDouble(), currency),
            value = needs,
            onValueChange = { needs = it },
            valueRange = 0f..4_000f,
            step = 10f,
            minLabel = Money.amount(0.0, currency),
            maxLabel = Money.amount(4_000.0, currency),
        )
        LabeledSlider(
            label = "What you spend on wants",
            display = Money.amount(wants.toDouble(), currency),
            value = wants,
            onValueChange = { wants = it },
            valueRange = 0f..4_000f,
            step = 10f,
            minLabel = Money.amount(0.0, currency),
            maxLabel = Money.amount(4_000.0, currency),
        )

        StackedBar(
            parts = listOf(
                BarPart("Needs", needs, colors.grow),
                BarPart("Wants", wants, colors.gold),
                BarPart("Future", actual.future.toFloat().coerceAtLeast(0f), colors.growSoft),
            ),
            contentDescription = "Your month: needs ${Money.percent(shares.needs, 0)}, " +
                "wants ${Money.percent(shares.wants, 0)}, " +
                "future ${Money.percent(shares.future, 0)}.",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Your future share",
                value = Money.percent(shares.future, 0),
                valueColor = if (overspending) colors.cost else colors.grow,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Target is 20%",
                value = Money.amount(net * Budget.TARGET_FUTURE, currency),
                modifier = Modifier.weight(1f),
            )
        }

        PlainEnglishResult(
            text = when {
                overspending ->
                    "That's ${Money.amount(Budget.overspend(net, needs.toDouble(), wants.toDouble()), currency)} " +
                        "more than you earn. Lower one of the two."
                gap <= 0.0 ->
                    "You're at ${Money.percent(shares.future, 0)} toward your future. That's " +
                        "the target — nothing to fix here."
                else ->
                    "You're putting ${Money.percent(shares.future, 0)} toward your future. The " +
                        "target is 20% — that's ${Money.amount(gap, currency)} a month, " +
                        "${Money.amount(gap * 12, currency)} a year."
            },
            tone = when {
                overspending -> Tone.COST
                gap <= 0.0 -> Tone.GOOD
                else -> null
            },
        )
    }
}
