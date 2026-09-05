package com.cashfluent.app.ui.navigation

/**
 * Six destinations. Four for the lessons — a judge has five minutes, and every extra
 * screen is time taken from the content that earns the marks — plus the game a lesson
 * ends in and the league the games feed.
 */
object Destinations {

    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val LEAGUE = "league"

    const val MODULE_ID_ARG = "moduleId"
    const val MODULE_ROUTE = "module/{$MODULE_ID_ARG}"
    const val GAME_ROUTE = "game/{$MODULE_ID_ARG}"

    fun module(moduleId: String): String = "module/$moduleId"

    fun game(moduleId: String): String = "game/$moduleId"
}
