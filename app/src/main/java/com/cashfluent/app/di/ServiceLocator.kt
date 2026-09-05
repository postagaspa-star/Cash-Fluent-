package com.cashfluent.app.di

import android.content.Context
import com.cashfluent.app.BuildConfig
import com.cashfluent.app.data.PlayerRepository
import com.cashfluent.app.data.ProgressRepository
import com.cashfluent.app.data.SettingsRepository
import com.cashfluent.app.data.firebase.FirestoreLeagueBackend
import com.cashfluent.app.data.league.LeagueService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Two repositories and one service do not need a framework. Hilt would cost a morning of
 * configuration and buy nothing a reviewer can see, so this is the whole of Cashfluent's DI.
 */
object ServiceLocator {

    private lateinit var applicationContext: Context

    val progressRepository: ProgressRepository by lazy { ProgressRepository(applicationContext) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(applicationContext) }

    /** Points, the seat and the board. Everything about the player goes through here. */
    val league: LeagueService by lazy {
        LeagueService(
            store = PlayerRepository(applicationContext),
            backend = FirestoreLeagueBackend(FirebaseAuth.getInstance(firebase), FirebaseFirestore.getInstance(firebase)),
        )
    }

    /**
     * Firebase is set up here, by hand, rather than by the google-services plugin and a
     * JSON file: three identifiers, visible in one place. They are not secrets — every
     * Firebase app ships them — and what a phone may read or write is decided by the rules
     * on the server, not by knowing them.
     */
    private val firebase: FirebaseApp by lazy {
        FirebaseApp.getApps(applicationContext).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
            ?: FirebaseApp.initializeApp(
                applicationContext,
                FirebaseOptions.Builder()
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .build(),
            )
    }

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}
