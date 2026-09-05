package com.cashfluent.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.data.model.GameOutcome
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.finance.Currency
import com.cashfluent.app.domain.game.ChoiceRound
import com.cashfluent.app.domain.game.Game
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.MiniGames
import com.cashfluent.app.domain.game.NumberRound
import com.cashfluent.app.domain.game.Round
import com.cashfluent.app.domain.game.Scoring
import com.cashfluent.app.domain.game.game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameState(
    val miniGame: MiniGame? = null,
    val topic: Module? = null,
    val game: Game? = null,
    val index: Int = 0,
    val guess: Double = 0.0,
    val picked: Int? = null,
    val revealed: Boolean = false,
    val points: List<Int> = emptyList(),
    val finished: Boolean = false,
    val outcome: GameOutcome? = null,
    val best: Int = 0,
) {
    val round: Round? get() = game?.rounds?.getOrNull(index)
    val roundCount: Int get() = miniGame?.rounds ?: 0
    val maxScore: Int get() = miniGame?.maxScore ?: 0
    val total: Int get() = points.sum()
    val lastPoints: Int get() = points.lastOrNull() ?: 0
    val canLockIn: Boolean get() = round is NumberRound || picked != null
    val isLastRound: Boolean get() = index >= roundCount - 1
}

/**
 * One game at a time. The rounds live here rather than on disk on purpose: leaving a
 * game and coming back deals a fresh one, and there is nothing to resume, because the
 * point of a round is the number you have not seen before.
 */
class GameViewModel : ViewModel() {

    private val league = ServiceLocator.league
    private val settingsRepository = ServiceLocator.settingsRepository

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    val currency: StateFlow<Currency> = settingsRepository.settings
        .map { it.currency }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Currency.DEFAULT)

    /** Deals [gameId]; asking again for the same game keeps the one in play. */
    fun start(gameId: String) {
        if (_state.value.miniGame?.id == gameId) return
        deal(gameId)
    }

    fun playAgain() {
        val id = _state.value.miniGame?.id ?: return
        deal(id)
    }

    /** A different game, picked at random. Returns its id so the screen can navigate. */
    fun anotherGameId(): String = MiniGames.random(Random(System.nanoTime()), _state.value.miniGame?.id).id

    private fun deal(gameId: String) {
        val miniGame = MiniGames.byId(gameId) ?: return
        val game = miniGame.game(seed = System.nanoTime())
        _state.value = GameState(
            miniGame = miniGame,
            topic = Modules.byId(miniGame.topicId),
            game = game,
            guess = startingGuess(game.rounds.first()),
        )
        viewModelScope.launch {
            val best = league.player.first().bestFor(gameId)
            _state.update { it.copy(best = best) }
        }
    }

    /** The slider starts in the middle, so the thumb points at nothing in particular. */
    private fun startingGuess(round: Round): Double =
        (round as? NumberRound)?.let { it.min + it.span / 2 } ?: 0.0

    fun setGuess(value: Double) {
        _state.update { if (it.revealed) it else it.copy(guess = value) }
    }

    fun pick(option: Int) {
        _state.update { if (it.revealed) it else it.copy(picked = option) }
    }

    fun lockIn() {
        _state.update { current ->
            val round = current.round ?: return@update current
            if (current.revealed || !current.canLockIn) return@update current
            val earned = when (round) {
                is NumberRound -> Scoring.number(round, current.guess)
                is ChoiceRound -> Scoring.choice(round, current.picked ?: -1)
            }
            current.copy(revealed = true, points = current.points + earned)
        }
    }

    fun next() {
        val current = _state.value
        if (!current.revealed) return
        if (current.isLastRound) {
            finish(current)
        } else {
            val nextRound = current.game!!.rounds[current.index + 1]
            _state.value = current.copy(
                index = current.index + 1,
                guess = startingGuess(nextRound),
                picked = null,
                revealed = false,
            )
        }
    }

    private fun finish(current: GameState) {
        _state.value = current.copy(finished = true)
        viewModelScope.launch {
            val outcome = league.recordGame(current.miniGame!!.id, current.total)
            _state.update { it.copy(outcome = outcome, best = outcome.best) }
        }
    }
}
