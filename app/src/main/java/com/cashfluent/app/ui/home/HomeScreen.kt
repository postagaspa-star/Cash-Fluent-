package com.cashfluent.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.data.model.ModuleStatus
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.ProgressBar
import com.cashfluent.app.ui.components.SectionLabel
import com.cashfluent.app.ui.components.TierBadge
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType

/**
 * Three things live in this app — lessons, games, a league — and this screen has to say
 * so in its first second.
 *
 * It used to say it in two twelve-point mono lines above the divider, in the same style
 * as "1 of 10 done". That is the app's statistics voice, so the eye filed both of them
 * as status and never touched them. They are now two cards carrying the largest numerals
 * on the screen, while the lesson keeps the only filled green button: two kinds of
 * prominence, so they do not compete.
 */
@Composable
fun HomeScreen(
    onOpenModule: (String) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLeague: () -> Unit,
    onOpenGames: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hero = state.rows.firstOrNull { it.isStartHere }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        Header(
            done = state.doneCount,
            total = state.total,
            fraction = state.fraction,
            onOpenAbout = onOpenAbout,
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 40.dp),
        ) {
            item {
                SectionCards(state = state, onOpenGames = onOpenGames, onOpenLeague = onOpenLeague)
                Spacer(Modifier.height(32.dp))
            }

            if (hero != null) {
                item {
                    SectionLabel(
                        text = if (hero.status == ModuleStatus.IN_PROGRESS) {
                            UiStrings.SECTION_CONTINUE
                        } else {
                            UiStrings.SECTION_START
                        },
                        color = colors.goldInk,
                    )
                    Spacer(Modifier.height(10.dp))
                    HeroCard(row = hero, onClick = { onOpenModule(hero.module.id) })
                    Spacer(Modifier.height(32.dp))
                }
            } else {
                item {
                    AllDone()
                    Spacer(Modifier.height(32.dp))
                }
            }

            item {
                SectionLabel(UiStrings.SECTION_ALL, color = colors.muted)
                Spacer(Modifier.height(4.dp))
            }

            items(items = state.rows, key = { it.module.id }) { row ->
                LessonRow(row = row, onClick = { onOpenModule(row.module.id) })
                HorizontalDivider(color = colors.line)
            }

            item {
                Text(
                    text = UiStrings.DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                )
            }
        }

        bottomBar()
    }
}

@Composable
private fun Header(
    done: Int,
    total: Int,
    fraction: Float,
    onOpenAbout: () -> Unit,
) {
    val colors = CashfluentTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text("Cash", style = MaterialTheme.typography.titleLarge, color = colors.ink)
                Text("fluent", style = MaterialTheme.typography.titleLarge, color = colors.grow)
            }
            TopAction(text = "Why", onClick = onOpenAbout)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = UiStrings.progress(done, total),
                style = CashfluentType.dataSmall,
                color = colors.muted,
            )
            ProgressBar(fraction, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = colors.line)
    }
}

/**
 * The games and the league, side by side, each carrying its own number in the app's
 * largest numerals. Side by side until the text gets big: past 130% the two cards stack,
 * because two columns of wrapped words is worse than one card after another.
 */
@Composable
private fun SectionCards(state: HomeState, onOpenGames: () -> Unit, onOpenLeague: () -> Unit) {
    val colors = CashfluentTheme.colors
    val stacked = LocalDensity.current.fontScale > 1.3f

    val games: @Composable (Modifier, Boolean) -> Unit = { modifier, fill ->
        SectionCard(
            fill = fill,
            label = UiStrings.GAMES,
            value = state.gamesCount.toString(),
            valueColor = colors.ink,
            caption = UiStrings.miniGamesSub(state.gamesPlayed),
            action = UiStrings.PLAY_ARROW,
            description = "${UiStrings.GAMES}, ${state.gamesCount} ${UiStrings.miniGamesSub(state.gamesPlayed)}",
            onClick = onOpenGames,
            modifier = modifier,
        )
    }
    val league: @Composable (Modifier, Boolean) -> Unit = { modifier, fill ->
        SectionCard(
            fill = fill,
            label = UiStrings.LEAGUE,
            value = Money.number(state.weekPoints.toDouble()),
            valueColor = if (state.weekPoints > 0) colors.grow else colors.muted,
            caption = UiStrings.PTS_THIS_WEEK,
            action = UiStrings.LEAGUE_ARROW,
            badge = { TierBadge(state.tier) },
            description = "${UiStrings.leagueName(state.tier)}, ${UiStrings.points(state.weekPoints)} this week",
            onClick = onOpenLeague,
            modifier = modifier,
        )
    }

    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            games(Modifier.fillMaxWidth(), false)
            league(Modifier.fillMaxWidth(), false)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            games(Modifier.weight(1f).fillMaxHeight(), true)
            league(Modifier.weight(1f).fillMaxHeight(), true)
        }
    }
}

/** One destination, read as one thing: a name, a number, what the number is, the way in. */
@Composable
private fun SectionCard(
    label: String,
    value: String,
    valueColor: Color,
    caption: String,
    action: String,
    description: String,
    onClick: () -> Unit,
    /** True when the card is one of a side-by-side pair, so the two ways in line up. */
    fill: Boolean,
    modifier: Modifier = Modifier,
    badge: (@Composable () -> Unit)? = null,
) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.muted,
        )
        if (badge != null) {
            Spacer(Modifier.height(8.dp))
            badge()
        }
        Spacer(Modifier.height(6.dp))
        Text(text = value, style = CashfluentType.value, color = valueColor)
        Spacer(Modifier.height(2.dp))
        Text(text = caption, style = MaterialTheme.typography.bodySmall, color = colors.muted)
        if (fill) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(10.dp))
        Text(text = action, style = MaterialTheme.typography.bodyMedium, color = colors.grow)
    }
}

@Composable
private fun TopAction(text: String, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
    }
}

/** The one card on the screen that is meant to be tapped. */
@Composable
private fun HeroCard(row: HomeModuleRow, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    val continuing = row.status == ModuleStatus.IN_PROGRESS

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.module.displayNumber,
                style = CashfluentType.dataSmall,
                color = colors.goldInk,
                modifier = Modifier
                    .background(colors.goldSoft, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${row.module.minutes} min",
                style = CashfluentType.dataSmall,
                color = colors.muted,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = row.module.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = row.module.hook,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(colors.grow, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (continuing) UiStrings.ACTION_CONTINUE else UiStrings.ACTION_START,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.paper,
            )
        }
    }
}

@Composable
private fun AllDone() {
    val colors = CashfluentTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.growSoft, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Text(
            text = UiStrings.ALL_DONE_TITLE,
            style = MaterialTheme.typography.titleLarge,
            color = colors.growInk,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = UiStrings.ALL_DONE_BODY,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.growInk,
        )
    }
}

/** Quiet, scannable, one line each. Nothing here competes with the hero card. */
@Composable
private fun LessonRow(row: HomeModuleRow, onClick: () -> Unit) {
    val colors = CashfluentTheme.colors
    val done = row.status == ModuleStatus.DONE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = row.unlocked, role = Role.Button, onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = row.module.displayNumber,
            style = CashfluentType.dataSmall,
            color = if (done) colors.grow else colors.muted,
            modifier = Modifier.width(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.module.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (row.unlocked) colors.ink else colors.muted,
            )
            if (row.lockedBehind != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = UiStrings.locked(row.lockedBehind),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted,
                )
            }
        }
        when {
            !row.unlocked -> Pill("locked", colors.muted, colors.surfaceAlt)
            done -> Pill("done", colors.growInk, colors.growSoft)
            row.status == ModuleStatus.IN_PROGRESS -> Pill("open", colors.goldInk, colors.goldSoft)
            else -> Spacer(Modifier.width(0.dp))
        }
    }
}
