package com.cashfluent.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var confirmingReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(title = UiStrings.SETTINGS, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        ) {
            item {
                SectionLabel(UiStrings.GROUP_DISPLAY, color = colors.muted)
                Spacer(Modifier.height(6.dp))
            }

            item {
                SettingRow(
                    title = UiStrings.CURRENCY_TITLE,
                    subtitle = UiStrings.CURRENCY_SUB,
                ) {
                    CurrencyPicker(
                        selected = settings.currency,
                        onSelect = viewModel::setCurrency,
                    )
                }
            }

            item {
                Spacer(Modifier.height(22.dp))
                SectionLabel(UiStrings.GROUP_LEARNING, color = colors.muted)
                Spacer(Modifier.height(6.dp))
            }

            item {
                SettingRow(
                    title = UiStrings.GUIDED_TITLE,
                    subtitle = UiStrings.GUIDED_SUB,
                ) {
                    Switch(
                        checked = settings.guidedPath,
                        onCheckedChange = viewModel::setGuidedPath,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.paper,
                            checkedTrackColor = colors.grow,
                            uncheckedThumbColor = colors.muted,
                            uncheckedTrackColor = colors.surfaceAlt,
                            uncheckedBorderColor = colors.lineStrong,
                        ),
                    )
                }
            }

            item {
                SettingRow(
                    title = UiStrings.RESET_TITLE,
                    subtitle = UiStrings.RESET_SUB,
                ) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { confirmingReset = true }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = UiStrings.RESET_ACTION,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.cost,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(22.dp))
                SectionLabel(UiStrings.GROUP_ABOUT, color = colors.muted)
                Spacer(Modifier.height(6.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAbout)
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = UiStrings.ABOUT_TITLE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.ink,
                        )
                        Text(
                            text = UiStrings.ABOUT_SUB,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    Text(text = "→", style = MaterialTheme.typography.bodyLarge, color = colors.muted)
                }
                HorizontalDivider(color = colors.line)
            }

            item {
                Text(
                    text = UiStrings.PRIVACY_NOTE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .background(colors.surfaceAlt, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                )
            }

            item {
                Text(
                    text = UiStrings.VERSION,
                    style = CashfluentType.dataSmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                )
            }
        }
    }

    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text(UiStrings.RESET_CONFIRM_TITLE) },
            text = { Text(UiStrings.RESET_CONFIRM_BODY) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgress()
                    confirmingReset = false
                }) { Text(UiStrings.RESET_CONFIRM_OK, color = colors.cost) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingReset = false }) {
                    Text(UiStrings.RESET_CONFIRM_CANCEL)
                }
            },
            containerColor = colors.surface,
            titleContentColor = colors.ink,
            textContentColor = colors.inkSecondary,
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
) {
    val colors = CashfluentTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                Spacer(Modifier.height(3.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            }
            trailing()
        }
        HorizontalDivider(color = colors.line)
    }
}

@Composable
private fun CurrencyPicker(selected: Currency, onSelect: (Currency) -> Unit) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = Modifier
            .border(1.dp, colors.lineStrong, RoundedCornerShape(10.dp))
            .height(48.dp),
    ) {
        Currency.entries.forEach { currency ->
            val isSelected = currency == selected
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 48.dp)
                    .background(if (isSelected) colors.grow else colors.surface)
                    .clickable { onSelect(currency) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currency.symbol,
                    style = CashfluentType.data,
                    color = if (isSelected) colors.paper else colors.muted,
                )
            }
        }
    }
}
