package com.cashfluent.app.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.game.MiniGame
import com.cashfluent.app.domain.game.MiniGames
import com.cashfluent.app.domain.league.Tier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameRow(val game: MiniGame, val best: Int)

data class TopicSection(val module: Module, val games: List<GameRow>)

data class GamesState(
    val sections: List<TopicSection> = emptyList(),
    val weekPoints: Int = 0,
    val totalPoints: Int = 0,
    val tier: Tier = Tier.FIRST,
    val played: Int = 0,
) {
    val total: Int get() = sections.sumOf { it.games.size }
}

class GamesViewModel : ViewModel() {

    private val playerRepository = ServiceLocator.playerRepository

    init {
        viewModelScope.launch { playerRepository.settle() }
    }

    val state: StateFlow<GamesState> = playerRepository.player
        .map(::buildState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildState(Player()))

    /** Any game at all, never the one just named. */
    fun surprise(excludingId: String? = null): MiniGame = MiniGames.random(Random(System.nanoTime()), excludingId)

    private companion object {
        fun buildState(player: Player) = GamesState(
            sections = Modules.all.map { module ->
                TopicSection(module, MiniGames.forTopic(module.id).map { GameRow(it, player.bestFor(it.id)) })
            },
            weekPoints = player.weekPoints,
            totalPoints = player.totalPoints,
            tier = player.tier,
            played = player.gamesWithBest,
        )
    }
}
