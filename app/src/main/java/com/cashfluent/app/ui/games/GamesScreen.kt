package com.cashfluent.app.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.game.GameRules
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PrimaryButton
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.SectionHeader
import com.cashfluent.app.ui.components.TierBadge
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * The catalogue: every mini-game, grouped by the lesson topic it draws on. Its own
 * section, reachable from Home, from any lesson's end, and from the league — a game is
 * a minute, and the points count towards the board.
 */
@Composable
fun GamesScreen(
    scrollToTopicId: String?,
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
    onOpenLeague: () -> Unit,
    viewModel: GamesViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Header items come first; each topic then takes one header item plus one per game.
    val headerItems = 2
    LaunchedEffect(scrollToTopicId, state.sections.size) {
        if (scrollToTopicId == null || state.sections.isEmpty()) return@LaunchedEffect
        var index = headerItems
        for (section in state.sections) {
            if (section.module.id == scrollToTopicId) {
                listState.scrollToItem(index)
                return@LaunchedEffect
            }
            index += 1 + section.games.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(title = UiStrings.GAMES, onBack = onBack)

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = UiStrings.GAMES_INTRO,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkSecondary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ResultTile(
                            label = UiStrings.WEEK_SHORT,
                            value = UiStrings.points(state.weekPoints),
                            valueColor = colors.grow,
                            modifier = Modifier.weight(1f),
                        )
                        ResultTile(
                            label = UiStrings.ALL_TIME,
                            value = UiStrings.points(state.totalPoints),
                            modifier = Modifier.weight(1f),
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(role = Role.Button, onClick = onOpenLeague)
                                .background(colors.surface, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = UiStrings.LEAGUE.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.muted,
                            )
                            Spacer(Modifier.height(6.dp))
                            TierBadge(state.tier)
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = UiStrings.SURPRISE_ME,
                        onClick = { onOpenGame(viewModel.surprise().id) },
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${UiStrings.gamesCount(state.played)} played of ${state.total}",
                        style = CashfluentType.dataSmall,
                        color = colors.muted,
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            state.sections.forEach { section ->
                item(key = "topic-${section.module.id}") {
                    SectionHeader("${section.module.displayNumber} · ${section.module.title}")
                    Spacer(Modifier.height(6.dp))
                }
                section.games.forEach { row ->
                    item(key = row.game.id) {
                        GameRow(row = row, onClick = { onOpenGame(row.game.id) })
                        HorizontalDivider(color = colors.line)
                    }
                }
                item(key = "gap-${section.module.id}") { Spacer(Modifier.height(28.dp)) }
            }
        }
    }
}

@Composable
private fun GameRow(row: GameRow, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = row.game.title, style = MaterialTheme.typography.bodyMedium, color = colors.ink)
            Spacer(Modifier.height(2.dp))
            Text(text = row.game.blurb, style = MaterialTheme.typography.bodySmall, color = colors.muted)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(row.game.mechanic.label, colors.inkSecondary, colors.surfaceAlt)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (row.best > 0) UiStrings.bestShort(row.best, GameRules.MAX_SCORE) else UiStrings.NOT_PLAYED,
                    style = CashfluentType.dataSmall,
                    color = if (row.best > 0) colors.growInk else colors.muted,
                )
            }
        }
        Text(text = "→", style = MaterialTheme.typography.bodyLarge, color = colors.grow)
    }
}
