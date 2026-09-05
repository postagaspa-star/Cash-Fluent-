package com.cashfluent.app.ui.league

import android.content.Intent
import androidx.compose.foundation.background
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
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.game.GameRules
import com.cashfluent.app.domain.game.Medal
import com.cashfluent.app.domain.league.LeagueCards
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.ui.components.Callout
import com.cashfluent.app.ui.components.MedalPill
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.PrimaryButton
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.SectionHeader
import com.cashfluent.app.ui.components.TextAction
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType
import kotlinx.coroutines.launch

/**
 * The board, your card, and the two things you can do with a card: send yours, paste
 * theirs. There is no server behind this screen, and it says so.
 */
@Composable
fun LeagueScreen(
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
    viewModel: LeagueViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

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
            item {
                NameField(
                    stored = state.player.name,
                    onChange = viewModel::setName,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResultTile(
                        label = UiStrings.THIS_WEEK,
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
                        label = UiStrings.MEDALS,
                        value = state.lessons.count { it.medal != Medal.NONE }.toString(),
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

            item { SectionHeader(UiStrings.THIS_WEEK) }

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

            item { SectionHeader(UiStrings.MEDALS, color = colors.goldInk) }

            items(state.lessons, key = { it.module.id }) { lesson ->
                LessonMedalRow(lesson = lesson, onClick = { onOpenGame(lesson.module.id) })
                HorizontalDivider(color = colors.line)
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
                text = "${UiStrings.points(card.totalPoints)} ${UiStrings.ALL_TIME.lowercase()} · " +
                    "${card.medalCount} ${UiStrings.MEDALS.lowercase()}",
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

@Composable
private fun LessonMedalRow(lesson: LessonMedal, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = lesson.module.displayNumber,
            style = CashfluentType.dataSmall,
            color = colors.muted,
            modifier = Modifier.width(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = lesson.module.title, style = MaterialTheme.typography.bodyMedium, color = colors.ink)
            if (lesson.best > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = UiStrings.best(lesson.best, GameRules.MAX_SCORE),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        if (lesson.medal != Medal.NONE) {
            MedalPill(lesson.medal)
        } else {
            Text(text = UiStrings.PLAY, style = MaterialTheme.typography.bodyMedium, color = colors.grow)
        }
    }
}
