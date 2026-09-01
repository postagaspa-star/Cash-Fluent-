package com.cashfluent.app.ui.simulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.SimulatorKind
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.ui.components.ResetLink
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.theme.CashfluentTheme

/**
 * "Try it with your numbers" is the second half of block ③, not a fourth block: it is
 * the worked example continued with the reader's own figures.
 *
 * Simulator state lives in the composable rather than a ViewModel. It survives rotation
 * through rememberSaveable, and it is deliberately not persisted to disk — coming back
 * to a module should start from the example, not from whatever was left on the sliders.
 */
@Composable
fun SimulatorPanel(kind: SimulatorKind, currency: Currency, modifier: Modifier = Modifier) {
    when (kind) {
        SimulatorKind.BUDGET -> BudgetSimulator(currency, modifier)
        SimulatorKind.COMPOUND -> CompoundSimulator(currency, modifier)
        SimulatorKind.DEBT -> DebtSimulator(currency, modifier)
        SimulatorKind.INFLATION -> InflationSimulator(currency, modifier)
        SimulatorKind.FEES -> FeesSimulator(currency, modifier)
        SimulatorKind.PAYSLIP -> PayslipSimulator(currency, modifier)
    }
}

@Composable
internal fun SimulatorScaffold(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionLabel(UiStrings.SECTION_TRY_IT, color = CashfluentTheme.colors.goldInk)
        content()
        ResetLink(UiStrings.RESET_EXAMPLE, onReset)
    }
}
