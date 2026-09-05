package com.cashfluent.app.ui.league

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import com.cashfluent.app.domain.league.LeagueCards
import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.domain.league.Tier
import com.cashfluent.app.domain.league.Zone
import com.cashfluent.app.ui.components.Callout
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.PrimaryButton
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.SectionHeader
import com.cashfluent.app.ui.components.TextAction
import com.cashfluent.app.ui.components.TierBadge
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.components.tierColors
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType
import kotlinx.coroutines.launch

/**
 * The board for this week, the rung you are on, and the two things you can do with a
 * card: send yours, paste theirs. Zones follow the rules in Promotion — top five up,
 * bottom five down — and last Monday's verdict is said once at the top.
 */
@Composable
fun LeagueScreen(
    onBack: () -> Unit,
    onOpenGames: () -> Unit,
    viewModel: LeagueViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val (tierForeground, tierBackground) = tierColors(state.player.tier)

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
                }
            }

            item {
                NameField(stored = state.player.name, onChange = viewModel::setName)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResultTile(
                        label = UiStrings.WEEK_SHORT,
                        value = UiStrings.points(state.player.weekPoints),
                        valueColor = colors.grow,
                        modifier = Modifier.weight(1f),
                    )
                    ResultTile(
                        label = UiStrings.ALL_TIME,
                        value = UiStrings.points(state.player.totalPoints),
                        modifier = Modifier.weight(1f),
                    )
                    ResultTile(
                        label = UiStrings.GAMES_PLAYED,
                        value = state.player.gamesPlayed.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PrimaryButton(
                        text = UiStrings.SHARE_CARD,
                        enabled = state.player.hasName,
                        onClick = {
                            scope.launch {
                                val text = viewModel.shareText()
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(send, UiStrings.SHARE_CARD))
                            }
                        },
                    )
                    if (!state.player.hasName) {
                        Text(
                            text = UiStrings.NAME_FIRST,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.muted,
                        )
                    }
                    TextAction(
                        text = UiStrings.PASTE_CARD,
                        onClick = { viewModel.importText(clipboard.getText()?.text) },
                    )
                }
            }

            state.message?.let { message ->
                item {
                    PlainEnglishResult(
                        text = message,
                        modifier = Modifier.clickable(role = Role.Button, onClick = viewModel::dismissMessage),
                    )
                }
            }

            item {
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

            items(state.standings, key = { it.card.id }) { standing ->
                StandingRow(
                    standing = standing,
                    onRemove = { viewModel.removeFriend(standing.card.id) },
                )
                HorizontalDivider(color = colors.line)
            }

            if (state.alone) {
                item {
                    Text(
                        text = UiStrings.LEAGUE_EMPTY,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted,
                    )
                }
            }

            item {
                Callout(
                    label = UiStrings.LEAGUE_HOW_TITLE,
                    body = UiStrings.LEAGUE_HOW,
                    background = colors.surfaceAlt,
                    foreground = colors.inkSecondary,
                )
            }

            item { SectionHeader(UiStrings.LADDER, color = colors.goldInk) }

            items(Tier.entries.reversed(), key = { "tier-${it.name}" }) { tier ->
                LadderRow(tier = tier, current = tier == state.player.tier)
            }

            item {
                Spacer(Modifier.height(8.dp))
                TextAction(text = UiStrings.ALL_GAMES, onClick = onOpenGames)
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
            val next = raw.take(LeagueCards.MAX_NAME)
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
private fun StandingRow(standing: Standing, onRemove: () -> Unit) {
    val colors = CashfluentTheme.colors
    val card = standing.card
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
                    text = card.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (standing.isYou) colors.growInk else colors.ink,
                )
                if (standing.isYou) {
                    Spacer(Modifier.width(8.dp))
                    Pill(UiStrings.YOU, colors.growInk, colors.surface)
                }
            }
            Text(
                text = "${UiStrings.points(card.totalPoints)} ${UiStrings.ALL_TIME.lowercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
            )
        }
        Text(
            text = UiStrings.points(standing.weekPoints),
            style = CashfluentType.valueSmall,
            color = if (standing.isYou) colors.growInk else colors.ink,
        )
        if (!standing.isYou) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(role = Role.Button, onClick = onRemove)
                    .semantics { contentDescription = UiStrings.remove(card.name) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.muted,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
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
