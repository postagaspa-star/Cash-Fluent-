package com.cashfluent.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.data.UserSettings
import com.cashfluent.app.data.model.ModuleStatus
import com.cashfluent.app.data.model.Player
import com.cashfluent.app.data.model.Progress
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.game.Medal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeModuleRow(
    val module: Module,
    val status: ModuleStatus,
    val unlocked: Boolean,
    val isStartHere: Boolean,
    val lockedBehind: String?,
    val medal: Medal,
)

data class HomeState(
    val rows: List<HomeModuleRow>,
    val doneCount: Int,
    val total: Int,
    val showMethodCard: Boolean,
    val points: Int,
    val weekPoints: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else doneCount.toFloat() / total
}

class HomeViewModel : ViewModel() {

    private val progressRepository = ServiceLocator.progressRepository
    private val settingsRepository = ServiceLocator.settingsRepository
    private val playerRepository = ServiceLocator.playerRepository

    val state: StateFlow<HomeState> =
        combine(progressRepository.progress, settingsRepository.settings, playerRepository.player, ::buildState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = buildState(Progress(), UserSettings(), Player()),
            )

    fun dismissMethodCard() {
        viewModelScope.launch { settingsRepository.dismissMethodCard() }
    }

    private companion object {
        fun buildState(progress: Progress, settings: UserSettings, player: Player): HomeState {
            val startHere = progress.firstUnfinished(Modules.coreIds)
            val rows = Modules.all.map { module ->
                val unlocked = !settings.guidedPath ||
                    Modules.isUnlocked(module) { progress.isDone(it) }
                HomeModuleRow(
                    module = module,
                    status = progress.statusOf(module.id),
                    unlocked = unlocked,
                    isStartHere = module.id == startHere && unlocked,
                    lockedBehind = if (unlocked) null else previousNumberOf(module),
                    medal = player.medalFor(module.id),
                )
            }
            return HomeState(
                rows = rows,
                doneCount = progress.doneCountIn(Modules.coreIds),
                total = Modules.coreIds.size,
                showMethodCard = !settings.methodCardDismissed,
                points = player.totalPoints,
                weekPoints = player.weekPoints,
            )
        }

        fun previousNumberOf(module: Module): String? {
            val index = Modules.all.indexOfFirst { it.id == module.id }
            return Modules.all.getOrNull(index - 1)?.displayNumber
        }
    }
}
