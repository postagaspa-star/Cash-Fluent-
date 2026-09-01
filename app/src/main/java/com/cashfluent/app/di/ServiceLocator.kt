package com.cashfluent.app.di

import android.content.Context
import com.cashfluent.app.data.ProgressRepository
import com.cashfluent.app.data.SettingsRepository

/**
 * Four dependencies do not need a framework. Hilt would cost a morning of configuration
 * and buy nothing a reviewer can see, so this is the whole of Cashfluent's DI.
 */
object ServiceLocator {

    private lateinit var applicationContext: Context

    val progressRepository: ProgressRepository by lazy { ProgressRepository(applicationContext) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(applicationContext) }

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}
