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
import com.cashfluent.app.domain.finance.SideIncome
import com.cashfluent.app.ui.components.BarPart
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.StackedBar
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.theme.CashfluentTheme

/**
 * Module 09. The bar splits a year's takings three ways, and the only slice that was
 * ever yours to spend is the green one.
 */
@Composable
fun SideIncomeSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var monthly by rememberSaveable { mutableStateOf(500f) }
    var expenses by rememberSaveable { mutableStateOf(900f) }
    var rate by rememberSaveable { mutableStateOf(25f) }

    val fraction = rate / 100.0
    val year = SideIncome.year(monthly.toDouble(), expenses.toDouble(), fraction)
    val setAside = SideIncome.flatSetAside(monthly.toDouble(), fraction)
    val cushion = SideIncome.cushion(monthly.toDouble(), expenses.toDouble(), fraction)

    // Costs can exceed what came in. The bar shows that as a year with nothing left
    // rather than a negative slice, and the sentence below says so in words.
    val spentOnWork = year.expenses.coerceAtMost(year.gross)
    val keep = year.keep.coerceAtLeast(0.0)

    SimulatorScaffold(
        onReset = { monthly = 500f; expenses = 900f; rate = 25f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "What you're paid a month",
            display = Money.amount(monthly.toDouble(), currency),
            value = monthly,
            onValueChange = { monthly = it },
            valueRange = 50f..3_000f,
            step = 50f,
            minLabel = Money.amount(50.0, currency),
            maxLabel = Money.amount(3_000.0, currency),
        )
        LabeledSlider(
            label = "What the work costs you, a year",
            display = Money.amount(expenses.toDouble(), currency),
            value = expenses,
            onValueChange = { expenses = it },
            valueRange = 0f..6_000f,
            step = 100f,
            minLabel = Money.amount(0.0, currency),
            maxLabel = Money.amount(6_000.0, currency),
        )
        LabeledSlider(
            label = "Combined tax and contributions",
            display = Money.percent(fraction, decimals = 0),
            value = rate,
            onValueChange = { rate = it },
            valueRange = 0f..50f,
            step = 1f,
            minLabel = "0%",
            maxLabel = "50%",
        )

        StackedBar(
            parts = listOf(
                BarPart("yours", keep.toFloat(), colors.grow),
                BarPart("tax", year.taxDue.toFloat(), colors.cost),
                BarPart("costs", spentOnWork.toFloat(), colors.muted),
            ),
            contentDescription = "Of ${Money.amount(year.gross, currency)} that arrived, " +
                "${Money.amount(keep, currency)} was yours, " +
                "${Money.amount(year.taxDue, currency)} was tax, and " +
                "${Money.amount(spentOnWork, currency)} went on doing the work.",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Taxable",
                value = Money.amount(year.profit, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "The bill",
                value = Money.amount(year.taxDue, currency),
                valueColor = colors.cost,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Real rate",
                value = Money.percent(year.effectiveRateOnGross, decimals = 1),
                modifier = Modifier.weight(1f),
            )
        }

        TotalStrip(
            label = "Hold back from every payment",
            value = Money.preciseAmount(setAside, currency),
        )

        PlainEnglishResult(
            text = buildString {
                append("${Money.amount(year.gross, currency)} arrived over the year, but only ")
                append("${Money.amount(year.profit, currency)} of it is taxable once the ")
                append("${Money.amount(year.expenses, currency)} the work cost you comes off. ")
                append("Move ${Money.preciseAmount(setAside, currency)} out of every payment ")
                append("and by the bill's arrival you'll have collected ")
                append("${Money.amount(setAside * 12, currency)} for a ")
                append("${Money.amount(year.taxDue, currency)} bill")
                if (cushion > 0.5) {
                    append(" — ${Money.amount(cushion, currency)} spare, and it's yours.")
                } else {
                    append(", which covers it exactly.")
                }
            },
            tone = Tone.GOOD,
        )
    }
}
