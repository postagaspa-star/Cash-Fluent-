package com.cashfluent.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.data.UserSettings
import com.cashfluent.app.data.model.ModuleStatus
import com.cashfluent.app.data.model.Progress
import com.cashfluent.app.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeModuleRow(
    val module: Module,
    val status: ModuleStatus,
    val unlocked: Boolean,
    val isStartHere: Boolean,
    val lockedBehind: String?,
)

data class HomeState(
    val rows: List<HomeModuleRow>,
    val doneCount: Int,
    val total: Int,
    val showMethodCard: Boolean,
) {
    val fraction: Float get() = if (total == 0) 0f else doneCount.toFloat() / total
}

class HomeViewModel : ViewModel() {

    private val progressRepository = ServiceLocator.progressRepository
    private val settingsRepository = ServiceLocator.settingsRepository

    val state: StateFlow<HomeState> =
        combine(progressRepository.progress, settingsRepository.settings, ::buildState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = buildState(Progress(), UserSettings()),
            )

    val guidedPath: StateFlow<Boolean> = settingsRepository.settings
        .map { it.guidedPath }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun dismissMethodCard() {
        viewModelScope.launch { settingsRepository.dismissMethodCard() }
    }

    private companion object {
        fun buildState(progress: Progress, settings: UserSettings): HomeState {
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
                )
            }
            return HomeState(
                rows = rows,
                doneCount = progress.doneCountIn(Modules.coreIds),
                total = Modules.coreIds.size,
                showMethodCard = !settings.methodCardDismissed,
            )
        }

        fun previousNumberOf(module: Module): String? {
            val index = Modules.all.indexOfFirst { it.id == module.id }
            return Modules.all.getOrNull(index - 1)?.displayNumber
        }
    }
}
