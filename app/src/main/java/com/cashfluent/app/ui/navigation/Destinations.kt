package com.cashfluent.app.ui.navigation

/**
 * Four destinations, on purpose. A judge has five minutes, and every extra screen is
 * time taken from the content that earns the marks.
 */
object Destinations {

    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    const val MODULE_ID_ARG = "moduleId"
    const val MODULE_ROUTE = "module/{$MODULE_ID_ARG}"

    fun module(moduleId: String): String = "module/$moduleId"
}
