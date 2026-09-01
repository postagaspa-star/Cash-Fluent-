package com.cashfluent.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cashfluent.app.domain.finance.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "cashfluent_settings")

data class UserSettings(
    val currency: Currency = Currency.DEFAULT,
    /** Off by default: every module is open unless someone asks for the locked path. */
    val guidedPath: Boolean = false,
    val methodCardDismissed: Boolean = false,
)

class SettingsRepository(private val context: Context) {

    val settings: Flow<UserSettings> = context.settingsStore.data.map { prefs ->
        UserSettings(
            currency = Currency.fromCode(prefs[CURRENCY]),
            guidedPath = prefs[GUIDED_PATH] ?: false,
            methodCardDismissed = prefs[METHOD_CARD_DISMISSED] ?: false,
        )
    }

    suspend fun setCurrency(currency: Currency) {
        context.settingsStore.edit { it[CURRENCY] = currency.code }
    }

    suspend fun setGuidedPath(enabled: Boolean) {
        context.settingsStore.edit { it[GUIDED_PATH] = enabled }
    }

    suspend fun dismissMethodCard() {
        context.settingsStore.edit { it[METHOD_CARD_DISMISSED] = true }
    }

    private companion object {
        val CURRENCY = stringPreferencesKey("currency")
        val GUIDED_PATH = booleanPreferencesKey("guided_path")
        val METHOD_CARD_DISMISSED = booleanPreferencesKey("method_card_dismissed")
    }
}
