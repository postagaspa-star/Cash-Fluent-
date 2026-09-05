package com.cashfluent.app.ui.league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.data.league.Connection
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.league.Movement
import com.cashfluent.app.domain.league.Standing
import com.cashfluent.app.domain.league.Week
import com.cashfluent.app.domain.league.WeekOutcome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeagueState(
    val player: Player = Player(),
    val standings: List<Standing> = emptyList(),
    val connection: Connection = Connection.UNKNOWN,
    /** Last week's verdict, if there is one worth saying. */
    val outcome: WeekOutcome? = null,
    val daysLeft: Int = 7,
) {
    val alone: Boolean get() = standings.size <= 1
    val offline: Boolean get() = connection == Connection.OFFLINE
}

class LeagueViewModel : ViewModel() {

    private val league = ServiceLocator.league

    init {
        // Close a finished week and take this week's seat the first time anyone looks at the board.
        viewModelScope.launch { league.settle() }
    }

    val state: StateFlow<LeagueState> = combine(
        league.player,
        league.standings,
        league.connection,
    ) { player, standings, connection ->
        LeagueState(
            player = player,
            standings = standings,
            connection = connection,
            outcome = player.lastOutcome?.takeIf { it.movement != Movement.STAYED || it.weekPoints > 0 },
            daysLeft = Week.daysUntilNext(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeagueState())

    fun setName(name: String) {
        viewModelScope.launch { league.rename(name) }
    }

    fun dismissOutcome() {
        viewModelScope.launch { league.dismissOutcome() }
    }

    /** The network was not there a moment ago; try the whole sequence again. */
    fun retry() {
        viewModelScope.launch { league.settle() }
    }
}
