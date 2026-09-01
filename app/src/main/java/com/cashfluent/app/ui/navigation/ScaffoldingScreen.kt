package com.cashfluent.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * TEMPORARY. Proves navigation, theming and the type scale are wired end to end while
 * the real screens are being built. Every use of this is replaced by a real screen, and
 * this file is deleted once the last one lands.
 */
@Composable
fun ScaffoldingScreen(
    title: String,
    note: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "SCAFFOLDING",
            style = MaterialTheme.typography.labelMedium,
            color = colors.gold,
        )
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = colors.ink)
        Text(text = note, style = MaterialTheme.typography.bodyLarge, color = colors.inkSecondary)
        Text(
            text = "131,757",
            style = CashfluentType.value,
            color = colors.grow,
        )
        if (onBack != null) {
            TextButton(onClick = onBack) { Text("Back") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScaffoldingScreenPreview() {
    CashfluentTheme {
        ScaffoldingScreen(title = "Home", note = "Placeholder while the real screen is built.", onBack = null)
    }
}
