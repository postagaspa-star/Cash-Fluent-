package com.cashfluent.app.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.data.model.ModuleStatus
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.ProgressBar
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

@Composable
fun HomeScreen(
    onOpenModule: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        // Words rather than icons: this app is for people who find the subject
        // intimidating, and an unlabelled gear is one more thing to decode.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text("Cash", style = MaterialTheme.typography.titleLarge, color = colors.ink)
                Text("fluent", style = MaterialTheme.typography.titleLarge, color = colors.grow)
            }
            TopAction(text = "Why", onClick = onOpenAbout)
            TopAction(text = UiStrings.SETTINGS, onClick = onOpenSettings)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = UiStrings.TAGLINE,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = UiStrings.progress(state.doneCount, state.total),
                        style = CashfluentType.dataSmall,
                        color = colors.muted,
                    )
                    ProgressBar(state.fraction, modifier = Modifier.weight(1f))
                }
            }

            if (state.showMethodCard) {
                item { MethodCard(onDismiss = viewModel::dismissMethodCard) }
            }

            item {
                SectionLabel(
                    text = UiStrings.SECTION_CORE,
                    color = colors.muted,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                )
            }

            items(items = state.rows, key = { it.module.id }) { row ->
                ModuleCard(row = row, onClick = { onOpenModule(row.module.id) })
            }

            item {
                Text(
                    text = UiStrings.DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun TopAction(text: String, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
    }
}

@Composable
private fun MethodCard(onDismiss: () -> Unit) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.growSoft, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Text(
            text = UiStrings.METHOD_TITLE,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.growInk,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            UiStrings.METHOD_CHIPS.forEach { chip ->
                Text(
                    text = chip,
                    style = CashfluentType.dataSmall,
                    color = colors.growInk,
                    modifier = Modifier
                        .background(colors.surface, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 44.dp)
                .clickable(onClick = onDismiss)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = UiStrings.METHOD_DISMISS,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.growInk,
            )
        }
    }
}

@Composable
private fun ModuleCard(row: HomeModuleRow, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    val highlighted = row.isStartHere

    val badgeForeground = when {
        highlighted -> colors.goldInk
        row.status == ModuleStatus.DONE -> colors.growInk
        else -> colors.muted
    }
    val badgeBackground = when {
        highlighted -> colors.goldSoft
        row.status == ModuleStatus.DONE -> colors.growSoft
        else -> colors.surfaceAlt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(14.dp))
            .border(
                width = if (highlighted) 1.5.dp else 1.dp,
                color = if (highlighted) colors.gold else colors.line,
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(enabled = row.unlocked, onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = row.module.displayNumber,
            style = CashfluentType.dataSmall,
            color = badgeForeground,
            modifier = Modifier
                .background(badgeBackground, RoundedCornerShape(8.dp))
                .padding(horizontal = 7.dp, vertical = 5.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.module.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (row.unlocked) colors.ink else colors.muted,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = row.lockedBehind?.let { UiStrings.locked(it) } ?: row.module.hook,
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }

        Spacer(Modifier.width(2.dp))

        when {
            !row.unlocked -> Pill("locked", colors.muted, colors.surfaceAlt)
            highlighted -> Pill(UiStrings.BADGE_START_HERE.lowercase(), colors.goldInk, colors.goldSoft)
            row.status == ModuleStatus.DONE -> Pill("done", colors.growInk, colors.growSoft)
            row.status == ModuleStatus.IN_PROGRESS -> Pill("open", colors.muted, colors.surfaceAlt)
            else -> Pill("new", colors.muted, colors.surfaceAlt)
        }
    }
}
