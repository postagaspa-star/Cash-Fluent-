package com.cashfluent.app.ui.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.Tone
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.finance.Payslip
import com.cashfluent.app.ui.components.BarPart
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.StackedBar
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.theme.CashfluentTheme

private const val RAISE = 2_000.0

@Composable
fun PayslipSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var gross by rememberSaveable { mutableFloatStateOf(24_000f) }
    var contributionRate by rememberSaveable { mutableFloatStateOf(9f) }
    var raiseTaken by rememberSaveable { mutableStateOf(false) }

    val rate = contributionRate / 100.0
    val slip = Payslip.compute(gross.toDouble(), rate)
    val marginal = Payslip.marginalRate(slip.taxableIncome)
    val raiseGain = Payslip.netGainFromRaise(gross.toDouble(), RAISE, rate)

    SimulatorScaffold(
        onReset = { gross = 24_000f; contributionRate = 9f; raiseTaken = false },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Gross yearly salary",
            display = Money.amount(gross.toDouble(), currency),
            value = gross,
            onValueChange = { gross = it; raiseTaken = false },
            valueRange = 8_000f..80_000f,
            step = 500f,
            minLabel = Money.amount(8_000.0, currency),
            maxLabel = Money.amount(80_000.0, currency),
        )
        LabeledSlider(
            label = "Contribution rate",
            display = Money.percent(rate),
            value = contributionRate,
            onValueChange = { contributionRate = it; raiseTaken = false },
            valueRange = 0f..20f,
            step = 0.5f,
            minLabel = "0%",
            maxLabel = "20%",
        )

        StackedBar(
            parts = listOf(
                BarPart("Net", slip.net.toFloat(), colors.grow),
                BarPart("Contributions", slip.contributions.toFloat(), colors.gold),
                BarPart("Tax", slip.tax.toFloat(), colors.cost),
            ),
            contentDescription = "Of ${Money.amount(slip.gross, currency)} gross, " +
                "${Money.amount(slip.net, currency)} is net, " +
                "${Money.amount(slip.contributions, currency)} is contributions and " +
                "${Money.amount(slip.tax, currency)} is tax.",
        )

        TotalStrip(
            label = "Lands in your account",
            value = Money.preciseAmount(slip.monthlyNet, currency) + " / month",
            tone = Tone.GOOD,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Average rate",
                value = Money.percent(slip.averageRate),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Marginal rate",
                value = Money.percent(marginal, decimals = 0),
                modifier = Modifier.weight(1f),
            )
        }

        // The interactive version of the module's punchline: press it and watch the
        // "raises can leave you worse off" belief fail in front of you.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(colors.grow, RoundedCornerShape(12.dp))
                .clickable { raiseTaken = !raiseTaken },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (raiseTaken) "Take the raise away" else "Give yourself a 2,000 raise",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.paper,
            )
        }

        PlainEnglishResult(
            text = if (raiseTaken) {
                "A ${Money.amount(RAISE, currency)} raise puts ${Money.amount(raiseGain, currency)} " +
                    "more in your pocket — ${Money.percent(raiseGain / RAISE, 0)} of it. Still " +
                    "never negative."
            } else {
                "On ${Money.amount(slip.gross, currency)} gross you take home " +
                    "${Money.preciseAmount(slip.monthlyNet, currency)} a month. Your average " +
                    "deduction is ${Money.percent(slip.averageRate)}, your marginal rate is " +
                    "${Money.percent(marginal, decimals = 0)}."
            },
            tone = if (raiseTaken) Tone.GOOD else null,
        )

        Text(
            text = "Simplified illustrative rates — not your country's.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
        )
    }
}
