package com.cashfluent.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashfluent.app.data.UserSettings
import com.cashfluent.app.di.ServiceLocator
import com.cashfluent.app.domain.finance.Currency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val settingsRepository = ServiceLocator.settingsRepository
    private val progressRepository = ServiceLocator.progressRepository
    private val league = ServiceLocator.league

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun setCurrency(currency: Currency) {
        viewModelScope.launch { settingsRepository.setCurrency(currency) }
    }

    /** Turning the guided path on or off never touches progress. */
    fun setGuidedPath(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGuidedPath(enabled) }
    }

    /** Lessons, points and the rung go back to zero. The nickname and the id stay. */
    fun resetProgress() {
        viewModelScope.launch {
            progressRepository.reset()
            league.reset()
        }
    }
}
