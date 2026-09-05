package com.cashfluent.app.ui.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.content.Module
import com.cashfluent.app.content.Modules
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.finance.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModuleUiState(
    val module: Module? = null,
    val answers: Map<Int, Int> = emptyMap(),
    val currency: Currency = Currency.DEFAULT,
    val isDone: Boolean = false,
    /** Best score on this lesson's game, out of [com.cashfluent.app.domain.game.GameRules.MAX_SCORE]. */
    val best: Int = 0,
) {
    val allAnswered: Boolean
        get() = module != null && answers.size >= module.check.size
}

class ModuleViewModel : ViewModel() {

    private val progressRepository = ServiceLocator.progressRepository
    private val settingsRepository = ServiceLocator.settingsRepository
    private val playerRepository = ServiceLocator.playerRepository

    private val moduleId = MutableStateFlow<String?>(null)

    val state: StateFlow<ModuleUiState> = combine(
        moduleId,
        progressRepository.progress,
        settingsRepository.settings,
        playerRepository.player,
    ) { id, progress, settings, player ->
        val module = id?.let { Modules.byId(it) }
        ModuleUiState(
            module = module,
            answers = module?.let { progress.of(it.id).answers }.orEmpty(),
            currency = settings.currency,
            isDone = module?.let { progress.isDone(it.id) } ?: false,
            best = module?.let { player.bestFor(it.id) } ?: 0,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModuleUiState())

    /**
     * Opening a module marks it started; re-binding the same id does nothing. An id that
     * matches no module is shown as empty and is never written to the store.
     */
    fun bind(id: String) {
        if (moduleId.value == id) return
        moduleId.value = id
        if (Modules.byId(id) == null) return
        viewModelScope.launch { progressRepository.markStarted(id) }
    }

    /**
     * Answers can be changed as often as you like — there is no score to protect. The
     * module is marked done once every question has an answer, right or wrong, because
     * the explanation is what the check is for. The repository decides that from what is
     * actually stored, so two quick taps cannot leave a finished module marked open.
     */
    fun answer(questionIndex: Int, optionIndex: Int) {
        val module = state.value.module ?: return
        viewModelScope.launch {
            progressRepository.recordAnswer(
                moduleId = module.id,
                questionIndex = questionIndex,
                optionIndex = optionIndex,
                questionCount = module.check.size,
            )
        }
    }
}
