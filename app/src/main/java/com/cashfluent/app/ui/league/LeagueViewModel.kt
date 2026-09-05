package com.cashfluent.app.ui.league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.UiStrings
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.league.League
import com.cashfluent.app.domain.league.LeagueCards
import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.domain.league.Week
import com.cashfluent.app.domain.league.WeekOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeagueState(
    val player: Player = Player(),
    val standings: List<Standing> = emptyList(),
    val message: String? = null,
    /** Last week's verdict, if there is one worth saying. */
    val outcome: WeekOutcome? = null,
) {
    val alone: Boolean get() = standings.size <= 1
}

class LeagueViewModel : ViewModel() {

    private val playerRepository = ServiceLocator.playerRepository

    private val message = MutableStateFlow<String?>(null)

    init {
        // Mint the id and close a finished week the first time anyone looks at the board.
        viewModelScope.launch { playerRepository.settle() }
    }

    val state: StateFlow<LeagueState> = combine(
        playerRepository.player,
        playerRepository.lastImport,
        message,
    ) { player, imported, own ->
        LeagueState(
            player = player,
            standings = League.standings(player.card, player.friends, Week.index(), player.tier),
            message = own ?: imported?.let(UiStrings::imported),
            outcome = player.lastOutcome?.takeIf { it.movement != Movement.STAYED || it.weekPoints > 0 },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeagueState())

    fun setName(name: String) {
        viewModelScope.launch { playerRepository.setName(name) }
    }

    /** Whatever is on the clipboard, or was shared in: every card in it joins the league. */
    fun importText(text: String?) {
        if (text.isNullOrBlank()) {
            message.value = UiStrings.CLIPBOARD_EMPTY
            return
        }
        viewModelScope.launch {
            val result = playerRepository.importCards(text)
            message.value = UiStrings.imported(result)
        }
    }

    fun removeFriend(id: String) {
        viewModelScope.launch { playerRepository.removeFriend(id) }
    }

    fun dismissMessage() {
        message.value = null
        playerRepository.clearLastImport()
    }

    fun dismissOutcome() {
        viewModelScope.launch { playerRepository.dismissOutcome() }
    }

    /** The text to hand to another app: a line for people, and the card for the phone. */
    suspend fun shareText(): String {
        val card = playerRepository.myCard()
        return UiStrings.shareText(card, LeagueCards.encode(card))
    }
}
