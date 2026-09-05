package com.cashfluent.app.ui.navigation

/**
 * Seven destinations. Four for the lessons — a judge has five minutes, and every extra
 * screen is time taken from the content that earns the marks — plus the games section,
 * one game, and the league the games feed.
 */
object Destinations {

    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val LEAGUE = "league"

    const val MODULE_ID_ARG = "moduleId"
    const val MODULE_ROUTE = "module/{$MODULE_ID_ARG}"

    const val TOPIC_ARG = "topic"
    const val GAMES_ROUTE = "games?$TOPIC_ARG={$TOPIC_ARG}"

    const val GAME_ID_ARG = "gameId"
    const val GAME_ROUTE = "game/{$GAME_ID_ARG}"

    fun module(moduleId: String): String = "module/$moduleId"

    /** The catalogue, scrolled to a topic when one is given. */
    fun games(topicId: String? = null): String = if (topicId == null) "games" else "games?$TOPIC_ARG=$topicId"

    fun game(gameId: String): String = "game/$gameId"
}
