package com.cashfluent.app.ui.league

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashfluent.app.content.Tone
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Nickname
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.Zone
import com.cashfluent.app.ui.components.Callout
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.SectionHeader
import com.cashfluent.app.ui.components.TextAction
import com.cashfluent.app.ui.components.TierBadge
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.components.tierColors
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * The board for this week, live, and the rung you are on. Zones follow the rules in
 * Promotion — top five up, bottom five down — and last Monday's verdict is said once at
 * the top. When the league cannot be reached the screen says so, in one line, and the
 * points stay on the phone until it can.
 */
@Composable
fun LeagueScreen(
    onBack: () -> Unit,
    viewModel: LeagueViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val (tierForeground, tierBackground) = tierColors(state.player.tier)
    var showHow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(title = UiStrings.LEAGUE_TITLE, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            state.outcome?.let { outcome ->
                item {
                    PlainEnglishResult(
                        text = "${UiStrings.outcomeBanner(outcome)} ${UiStrings.OUTCOME_DISMISS}.",
                        tone = when (outcome.movement) {
                            Movement.PROMOTED -> Tone.GOOD
                            Movement.DEMOTED -> Tone.COST
                            Movement.STAYED -> null
                        },
                        modifier = Modifier.clickable(role = Role.Button, onClick = viewModel::dismissOutcome),
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tierBackground, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                ) {
                    Text(
                        text = UiStrings.leagueName(state.player.tier),
                        style = MaterialTheme.typography.titleLarge,
                        color = tierForeground,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = UiStrings.LEAGUE_RULES,
                        style = MaterialTheme.typography.bodySmall,
                        color = tierForeground,
                    )
                    if (state.player.seated) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = UiStrings.boardStatus(state.standings.size, state.daysLeft),
                            style = CashfluentType.dataSmall,
                            color = tierForeground,
                        )
                    }
                }
            }

            item {
                // IntrinsicSize.Min plus fillMaxHeight: at 200% text "ALL TIME" wraps, and
                // without this its tile grows past the two beside it and the row goes ragged.
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResultTile(
                        label = UiStrings.WEEK_SHORT,
                        value = UiStrings.points(state.player.weekPoints),
                        valueColor = colors.grow,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    ResultTile(
                        label = UiStrings.ALL_TIME,
                        value = UiStrings.points(state.player.totalPoints),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    ResultTile(
                        label = UiStrings.GAMES_PLAYED,
                        value = state.player.gamesPlayed.toString(),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            when {
                !state.player.seated && state.offline -> item {
                    PlainEnglishResult(
                        text = UiStrings.LEAGUE_OFFLINE,
                        tone = Tone.COST,
                        modifier = Modifier.clickable(role = Role.Button, onClick = viewModel::retry),
                    )
                }
                !state.player.seated -> item {
                    Text(
                        text = UiStrings.LEAGUE_CONNECTING,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
                state.offline -> item {
                    Text(
                        text = UiStrings.LEAGUE_STALE,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }

            // With no seat there is no board, and a heading over nothing reads as a fault.
            if (state.standings.isNotEmpty()) item {
                SectionHeader(UiStrings.THIS_WEEK)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${UiStrings.ZONE_UP} ${UiStrings.ZONE_UP_DESC} · ${UiStrings.ZONE_DOWN} ${UiStrings.ZONE_DOWN_DESC}",
                    style = CashfluentType.dataSmall,
                    color = colors.muted,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "${UiStrings.ZONE_UP_DESC}, ${UiStrings.ZONE_DOWN_DESC}"
                    },
                )
            }

            items(state.standings, key = { it.entrant.id }) { standing ->
                StandingRow(standing)
                HorizontalDivider(color = colors.line)
            }

            if (state.player.seated && state.alone) {
                item {
                    Text(
                        text = UiStrings.LEAGUE_ALONE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
            }

            item { SectionHeader(UiStrings.LADDER, color = colors.goldInk) }

            items(Tier.entries.reversed(), key = { "tier-${it.name}" }) { tier ->
                LadderRow(tier = tier, current = tier == state.player.tier)
            }

            item {
                Spacer(Modifier.height(8.dp))
                NameField(stored = state.player.name, onChange = viewModel::setName)
            }

            item {
                TextAction(
                    text = UiStrings.HOW_THIS_WORKS,
                    onClick = { showHow = !showHow },
                )
            }

            if (showHow) {
                item {
                    Callout(
                        label = UiStrings.LEAGUE_HOW_TITLE,
                        body = UiStrings.LEAGUE_HOW,
                        background = colors.surfaceAlt,
                        foreground = colors.inkSecondary,
                    )
                }
                item {
                    Callout(
                        label = UiStrings.LEAGUE_PRIVACY_TITLE,
                        body = UiStrings.LEAGUE_PRIVACY,
                        background = colors.surfaceAlt,
                        foreground = colors.inkSecondary,
                    )
                }
            }
        }
    }
}

/**
 * The nickname is typed here and written as it changes. The field keeps its own text so
 * a write coming back round through the flow cannot fight the keyboard mid-word.
 */
@Composable
private fun NameField(stored: String, onChange: (String) -> Unit) {
    val colors = CashfluentTheme.colors
    var typed by remember { mutableStateOf<String?>(null) }
    val value = typed ?: stored

    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val next = raw.take(Nickname.MAX)
            typed = next
            onChange(next)
        },
        label = { Text(UiStrings.YOUR_NAME) },
        supportingText = { Text(UiStrings.NAME_HINT, color = colors.muted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.grow,
            unfocusedBorderColor = colors.lineStrong,
            focusedLabelColor = colors.grow,
            unfocusedLabelColor = colors.muted,
            cursorColor = colors.grow,
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun StandingRow(standing: Standing) {
    val colors = CashfluentTheme.colors
    val entrant = standing.entrant
    val zoneColor = when (standing.zone) {
        Zone.PROMOTION -> colors.grow
        Zone.DEMOTION -> colors.cost
        Zone.SAFE -> colors.muted
    }
    val zoneMark = when (standing.zone) {
        Zone.PROMOTION -> UiStrings.ZONE_UP
        Zone.DEMOTION -> UiStrings.ZONE_DOWN
        Zone.SAFE -> ""
    }
    val zoneDescription = when (standing.zone) {
        Zone.PROMOTION -> UiStrings.ZONE_UP_DESC
        Zone.DEMOTION -> UiStrings.ZONE_DOWN_DESC
        Zone.SAFE -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (standing.isYou) Modifier.background(colors.growSoft, RoundedCornerShape(10.dp)) else Modifier)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = standing.position.toString(),
            style = CashfluentType.data,
            color = if (standing.position == 1) colors.goldInk else colors.muted,
            modifier = Modifier.width(24.dp),
        )
        Text(
            text = zoneMark,
            style = CashfluentType.data,
            color = zoneColor,
            modifier = Modifier
                .width(16.dp)
                .semantics { contentDescription = zoneDescription },
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entrant.name.ifBlank { UiStrings.UNNAMED },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (standing.isYou) colors.growInk else colors.ink,
                )
                if (standing.isYou) {
                    Spacer(Modifier.width(8.dp))
                    Pill(UiStrings.YOU, colors.growInk, colors.surface)
                }
            }
            Text(
                text = "${UiStrings.points(entrant.totalPoints)} ${UiStrings.ALL_TIME.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        Text(
            text = UiStrings.points(standing.weekPoints),
            style = CashfluentType.valueSmall,
            color = if (standing.isYou) colors.growInk else colors.ink,
        )
    }
}

/** One rung, top of the ladder first. The one you are on is framed. */
@Composable
private fun LadderRow(tier: Tier, current: Boolean) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (current) Modifier.border(1.5.dp, colors.grow, RoundedCornerShape(10.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = (Tier.entries.size - tier.ordinal).toString(),
            style = CashfluentType.dataSmall,
            color = colors.muted,
            modifier = Modifier.width(20.dp),
        )
        TierBadge(tier)
        Spacer(Modifier.weight(1f))
        if (current) {
            Text(text = UiStrings.YOU_ARE_HERE, style = CashfluentType.dataSmall, color = colors.grow)
        }
    }
}
