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
import com.cashfluent.app.domain.finance.Credit
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.finance.UtilisationBand
import com.cashfluent.app.ui.components.BarPart
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.StackedBar
import com.cashfluent.app.ui.theme.CashfluentTheme

/**
 * Module 08. The bar is the card itself: the used part against the part still free.
 * Colour follows the band, so the moment a slider crosses 30% the screen says so.
 */
@Composable
fun CreditSimulator(currency: Currency, modifier: Modifier = Modifier) {
    val colors = CashfluentTheme.colors

    var limit by rememberSaveable { mutableStateOf(1_000f) }
    var balance by rememberSaveable { mutableStateOf(450f) }
    var target by rememberSaveable { mutableStateOf(30f) }

    val used = balance.coerceAtMost(limit)
    val utilisation = Credit.utilisation(used.toDouble(), limit.toDouble())
    val targetFraction = target / 100.0
    val payment = Credit.paymentToReach(used.toDouble(), limit.toDouble(), targetFraction)
    val ceiling = Credit.balanceCeiling(limit.toDouble(), targetFraction)
    val band = Credit.band(utilisation)

    val bandColor = when (band) {
        UtilisationBand.COMFORTABLE -> colors.grow
        UtilisationBand.WATCH -> colors.goldInk
        UtilisationBand.HIGH -> colors.cost
    }

    SimulatorScaffold(
        onReset = { limit = 1_000f; balance = 450f; target = 30f },
        modifier = modifier,
    ) {
        LabeledSlider(
            label = "Your credit limit",
            display = Money.amount(limit.toDouble(), currency),
            value = limit,
            // Lowering the limit takes the balance down with it, so the two sliders can
            // never disagree about what is on the card.
            onValueChange = { limit = it; if (balance > it) balance = it },
            valueRange = 200f..10_000f,
            step = 100f,
            minLabel = Money.amount(200.0, currency),
            maxLabel = Money.amount(10_000.0, currency),
        )
        // The range follows the limit: you cannot owe more than the card allows, and the
        // thumb should stop where the number stops.
        LabeledSlider(
            label = "On the card when the statement is cut",
            display = Money.amount(used.toDouble(), currency),
            value = used,
            onValueChange = { balance = it },
            valueRange = 0f..limit,
            step = 50f,
            minLabel = Money.amount(0.0, currency),
            maxLabel = Money.amount(limit.toDouble(), currency),
        )
        LabeledSlider(
            label = "Where you want to land",
            display = Money.percent(targetFraction, decimals = 0),
            value = target,
            onValueChange = { target = it },
            valueRange = 5f..60f,
            step = 5f,
            minLabel = "5%",
            maxLabel = "60%",
        )

        StackedBar(
            parts = listOf(
                BarPart("used", used, bandColor),
                BarPart("still free", (limit - used).coerceAtLeast(0f), colors.line),
            ),
            contentDescription = "You are using ${Money.percent(utilisation, decimals = 0)} of " +
                "your limit: ${Money.amount(used.toDouble(), currency)} of " +
                "${Money.amount(limit.toDouble(), currency)}.",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = "Utilisation",
                value = Money.percent(utilisation, decimals = 0),
                valueColor = bandColor,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Ceiling",
                value = Money.amount(ceiling, currency),
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = "Pay",
                value = Money.amount(payment, currency),
                valueColor = if (payment > 0.0) colors.cost else colors.grow,
                modifier = Modifier.weight(1f),
            )
        }

        PlainEnglishResult(
            text = if (payment <= 0.0) {
                "At ${Money.amount(used.toDouble(), currency)} on a " +
                    "${Money.amount(limit.toDouble(), currency)} limit you're already at " +
                    "${Money.percent(utilisation, decimals = 0)} — inside your " +
                    "${Money.percent(targetFraction, decimals = 0)} target with nothing to pay."
            } else {
                "You're at ${Money.percent(utilisation, decimals = 0)}. To be recorded at " +
                    "${Money.percent(targetFraction, decimals = 0)} you can owe at most " +
                    "${Money.amount(ceiling, currency)}, so pay " +
                    "${Money.amount(payment, currency)} — before the statement date, not " +
                    "before the due date."
            },
            tone = if (payment <= 0.0) Tone.GOOD else null,
        )
    }
}
