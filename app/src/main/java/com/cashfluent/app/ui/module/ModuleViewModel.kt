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
) {
    val allAnswered: Boolean
        get() = module != null && answers.size >= module.check.size
}

class ModuleViewModel : ViewModel() {

    private val progressRepository = ServiceLocator.progressRepository
    private val settingsRepository = ServiceLocator.settingsRepository

    private val moduleId = MutableStateFlow<String?>(null)

    val state: StateFlow<ModuleUiState> = combine(
        moduleId,
        progressRepository.progress,
        settingsRepository.settings,
    ) { id, progress, settings ->
        val module = id?.let { Modules.byId(it) }
        ModuleUiState(
            module = module,
            answers = module?.let { progress.of(it.id).answers }.orEmpty(),
            currency = settings.currency,
            isDone = module?.let { progress.isDone(it.id) } ?: false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModuleUiState())

    /** Opening a module marks it started; re-binding the same id does nothing. */
    fun bind(id: String) {
        if (moduleId.value == id) return
        moduleId.value = id
        viewModelScope.launch { progressRepository.markStarted(id) }
    }

    /**
     * Answers can be changed as often as you like — there is no score to protect. The
     * module is marked done once every question has an answer, right or wrong, because
     * the explanation is what the check is for.
     */
    fun answer(questionIndex: Int, optionIndex: Int) {
        val module = state.value.module ?: return
        viewModelScope.launch {
            progressRepository.recordAnswer(module.id, questionIndex, optionIndex)
            val answered = state.value.answers.keys + questionIndex
            if (answered.size >= module.check.size) {
                progressRepository.markDone(module.id)
            }
        }
    }
}
