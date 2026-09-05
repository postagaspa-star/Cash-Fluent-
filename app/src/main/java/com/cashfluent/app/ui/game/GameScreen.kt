package com.cashfluent.app.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.cashfluent.app.content.Tone
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.finance.Money
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Quantity
import com.cashfluent.app.domain.game.Scoring
import com.cashfluent.app.domain.league.Zone
import com.cashfluent.app.ui.components.Callout
import com.cashfluent.app.ui.components.LabeledSlider
import com.cashfluent.app.ui.components.Pill
import com.cashfluent.app.ui.components.PlainEnglishResult
import com.cashfluent.app.ui.components.PrimaryButton
import com.cashfluent.app.ui.components.ResultTile
import com.cashfluent.app.ui.components.SectionProgress
import com.cashfluent.app.ui.components.SubLabel
import com.cashfluent.app.ui.components.TextAction
import com.cashfluent.app.ui.components.TopBar
import com.cashfluent.app.ui.components.TotalStrip
import com.cashfluent.app.ui.components.withCurrency
import com.cashfluent.app.ui.theme.CashfluentTheme
import com.cashfluent.app.ui.theme.CashfluentType
import kotlin.math.roundToInt

/**
 * One mini-game: a handful of rounds of one formula. Every round ends the same way —
 * your number next to the real one, the points, and the calculation written out. The
 * reveal is the teaching; the score is what keeps you playing.
 */
@Composable
fun GameScreen(
    gameId: String,
    onBack: () -> Unit,
    onOpenGame: (String) -> Unit,
    onOpenGames: () -> Unit,
    onOpenLeague: () -> Unit,
    viewModel: GameViewModel = viewModel(),
) {
    val colors = CashfluentTheme.colors
    LaunchedEffect(gameId) { viewModel.start(gameId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val board by viewModel.board.collectAsStateWithLifecycle()
    val miniGame = state.miniGame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .safeDrawingPadding(),
    ) {
        TopBar(title = miniGame?.title ?: "", onBack = onBack)

        if (miniGame == null || state.game == null) return@Column

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.finished) {
                item {
                    ScoreBlock(
                        state = state,
                        board = board,
                        onPlayAgain = viewModel::playAgain,
                        onAnother = { onOpenGame(viewModel.anotherGameId()) },
                        onOpenGames = onOpenGames,
                        onOpenLeague = onOpenLeague,
                    )
                }
                return@LazyColumn
            }

            val round = state.round ?: return@LazyColumn

            item {
                Column {
                    val topic = state.topic
                    if (topic != null) {
                        Text(
                            text = "${topic.displayNumber} · ${topic.title} · ${miniGame.mechanic.label}",
                            style = CashfluentType.dataSmall,
                            color = colors.muted,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        SubLabel(UiStrings.round(state.index + 1, state.roundCount), modifier = Modifier.weight(1f))
                        Text(
                            text = UiStrings.pointsSoFar(state.total),
                            style = CashfluentType.dataSmall,
                            color = colors.muted,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SectionProgress(reached = state.points.size, total = state.roundCount)
                }
            }

            item {
                Text(
                    text = round.prompt.withCurrency(currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }

            item {
                when (round) {
                    is NumberRound -> NumberInput(
                        round = round,
                        guess = state.guess,
                        revealed = state.revealed,
                        earned = state.lastPoints,
                        currency = currency,
                        onGuess = viewModel::setGuess,
                    )
                    is ChoiceRound -> ChoiceInput(
                        round = round,
                        picked = state.picked,
                        revealed = state.revealed,
                        currency = currency,
                        onPick = viewModel::pick,
                    )
                }
            }

            if (state.revealed) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PlainEnglishResult(
                            text = UiStrings.roundVerdict(state.lastPoints),
                            tone = if (state.lastPoints >= Scoring.MAX_ROUND / 2) Tone.GOOD else Tone.COST,
                        )
                        Callout(
                            label = UiStrings.CALCULATION,
                            body = round.explanation.withCurrency(currency),
                            background = colors.sunk,
                            foreground = colors.inkSecondary,
                        )
                    }
                }
            }

            item {
                PrimaryButton(
                    text = when {
                        !state.revealed -> UiStrings.LOCK_IN
                        state.isLastRound -> UiStrings.SEE_SCORE
                        else -> UiStrings.NEXT_ROUND
                    },
                    enabled = state.revealed || state.canLockIn,
                    onClick = { if (state.revealed) viewModel.next() else viewModel.lockIn() },
                )
            }

            if (state.index == 0 && !state.revealed) {
                item {
                    Text(
                        text = UiStrings.GAME_INTRO,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.muted,
                    )
                }
            }
        }
    }
}

/** How a quantity reads on screen. The recipe picked the kind; the currency is the reader's. */
fun Quantity.format(value: Double, currency: Currency): String = when (this) {
    Quantity.AMOUNT -> Money.amount(value, currency)
    Quantity.AMOUNT_CENTS -> Money.preciseAmount(value, currency)
    Quantity.PERCENT -> Money.percent(value, decimals = 0)
    Quantity.PERCENT_PRECISE -> Money.percent(value, decimals = 1)
    Quantity.MONTHS -> "${value.roundToInt()} months"
    Quantity.YEARS -> "${value.roundToInt()} years"
}

@Composable
private fun NumberInput(
    round: NumberRound,
    guess: Double,
    revealed: Boolean,
    earned: Int,
    currency: Currency,
    onGuess: (Double) -> Unit,
) {
    val colors = CashfluentTheme.colors
    if (!revealed) {
        LabeledSlider(
            label = UiStrings.YOUR_ANSWER,
            display = round.quantity.format(guess, currency),
            value = guess.toFloat(),
            onValueChange = { onGuess(it.toDouble()) },
            valueRange = round.min.toFloat()..round.max.toFloat(),
            step = round.step.toFloat(),
            minLabel = round.quantity.format(round.min, currency),
            maxLabel = round.quantity.format(round.max, currency),
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = UiStrings.YOUR_ANSWER,
                value = round.quantity.format(guess, currency),
                valueColor = if (earned >= Scoring.MAX_ROUND / 2) colors.grow else colors.cost,
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                label = UiStrings.THE_ANSWER,
                value = round.quantity.format(round.truth, currency),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The same shape as the lesson's check, so nothing has to be learnt twice. */
@Composable
private fun ChoiceInput(
    round: ChoiceRound,
    picked: Int?,
    revealed: Boolean,
    currency: Currency,
    onPick: (Int) -> Unit,
) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        round.options.forEachIndexed { index, option ->
            val isCorrect = index == round.correctIndex
            val isPicked = picked == index
            val background = when {
                revealed && isCorrect -> colors.growSoft
                revealed && isPicked -> colors.costSoft
                isPicked -> colors.goldSoft
                else -> colors.surface
            }
            val border = when {
                revealed && isCorrect -> colors.grow
                revealed && isPicked -> colors.cost
                isPicked -> colors.gold
                else -> colors.line
            }
            val foreground = when {
                revealed && isCorrect -> colors.growInk
                revealed && isPicked -> colors.costInk
                isPicked -> colors.goldInk
                else -> colors.inkSecondary
            }
            val marker = when {
                revealed && isCorrect -> "✓"
                revealed && isPicked -> "✕"
                else -> ('A' + index).toString()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(background, RoundedCornerShape(12.dp))
                    .border(1.dp, border, RoundedCornerShape(12.dp))
                    .selectable(selected = isPicked, enabled = !revealed, role = Role.RadioButton) { onPick(index) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = marker, style = CashfluentType.data, color = foreground, modifier = Modifier.width(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = option.withCurrency(currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = foreground,
                )
            }
        }
    }
    if (!revealed && picked == null) {
        Text(
            text = UiStrings.PICK_ONE,
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ScoreBlock(
    state: GameState,
    board: BoardPeek?,
    onPlayAgain: () -> Unit,
    onAnother: () -> Unit,
    onOpenGames: () -> Unit,
    onOpenLeague: () -> Unit,
) {
    val colors = CashfluentTheme.colors
    val outcome = state.outcome
    val best = outcome?.best ?: maxOf(state.best, state.total)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = UiStrings.YOUR_SCORE,
            style = MaterialTheme.typography.titleLarge,
            color = colors.grow,
        )
        state.topic?.let { topic ->
            Text(
                text = "${state.miniGame?.title} · ${topic.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }

        // Green when this is the best you have played it, neutral otherwise. Clay means
        // what something costs you, and a first attempt at a mini-game costs you nothing.
        TotalStrip(
            label = UiStrings.SCORE,
            value = UiStrings.outOf(state.total, state.maxScore),
            tone = if (outcome?.newBest == true) Tone.GOOD else null,
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultTile(
                label = UiStrings.BEST,
                value = UiStrings.outOf(best, state.maxScore),
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            ResultTile(
                label = UiStrings.WEEK_SHORT,
                value = UiStrings.points(outcome?.weekPoints ?: state.total),
                valueColor = colors.grow,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }

        if (outcome != null && outcome.newBest) {
            Row { Pill(UiStrings.NEW_BEST, colors.growInk, colors.growSoft) }
        }

        if (board != null) {
            BoardPeekBlock(board = board, onOpenLeague = onOpenLeague)
        }

        PrimaryButton(text = UiStrings.ANOTHER_GAME, onClick = onAnother)
        TextAction(text = UiStrings.PLAY_AGAIN, onClick = onPlayAgain)
        // The board above is the way into the league; without one, the link has to be.
        TextAction(
            text = if (board == null) UiStrings.SEE_LEAGUE else UiStrings.ALL_GAMES,
            onClick = if (board == null) onOpenLeague else onOpenGames,
        )
    }
}

/**
 * What the points just bought: your place, the three rows around you, and the number that
 * would carry you into the promotion zone. This is the only moment in the app where
 * earning and consequence sit in the same screenful.
 */
@Composable
private fun BoardPeekBlock(board: BoardPeek, onOpenLeague: () -> Unit) {
    val colors = CashfluentTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onOpenLeague)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        SubLabel(UiStrings.WHERE_YOU_STAND)
        Spacer(Modifier.height(6.dp))
        Text(
            text = UiStrings.standing(board.position, board.size),
            style = CashfluentType.value,
            color = colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = when {
                board.zone == Zone.PROMOTION -> UiStrings.IN_PROMOTION
                board.zone == Zone.DEMOTION -> UiStrings.IN_DEMOTION
                board.gap != null -> UiStrings.gapToPromotion(board.gap)
                else -> UiStrings.LEAGUE_ARROW
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (board.zone == Zone.DEMOTION) colors.costInk else colors.muted,
        )
        Spacer(Modifier.height(12.dp))
        board.rows.forEach { standing ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = standing.position.toString(),
                    style = CashfluentType.dataSmall,
                    color = colors.muted,
                    modifier = Modifier.width(24.dp),
                )
                Text(
                    text = standing.entrant.name.ifBlank { UiStrings.UNNAMED },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (standing.isYou) colors.growInk else colors.ink,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = UiStrings.points(standing.weekPoints),
                    style = CashfluentType.dataSmall,
                    color = if (standing.isYou) colors.growInk else colors.muted,
                )
            }
        }
    }
}
