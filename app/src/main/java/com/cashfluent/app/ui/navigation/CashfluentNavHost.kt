package com.cashfluent.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cashfluent.app.ui.about.AboutScreen
import com.cashfluent.app.ui.home.HomeScreen
import com.cashfluent.app.ui.module.ModuleScreen
import com.cashfluent.app.ui.settings.SettingsScreen

@Composable
fun CashfluentNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {

        composable(Destinations.HOME) {
            HomeScreen(
                onOpenModule = { id -> navController.open(Destinations.module(id)) },
                onOpenSettings = { navController.open(Destinations.SETTINGS) },
                onOpenAbout = { navController.open(Destinations.ABOUT) },
            )
        }

        composable(
            route = Destinations.MODULE_ROUTE,
            arguments = listOf(navArgument(Destinations.MODULE_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            val moduleId = entry.arguments?.getString(Destinations.MODULE_ID_ARG).orEmpty()
            ModuleScreen(
                moduleId = moduleId,
                onBack = { navController.popBackStack() },
                onOpenModule = { id ->
                    // Replace rather than stack, so "up next" cannot build a long back stack.
                    navController.navigate(Destinations.module(id)) {
                        popUpTo(Destinations.HOME)
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.open(Destinations.ABOUT) },
            )
        }

        composable(Destinations.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Two taps in quick succession — easy on a card that fills the width of the screen —
 * used to push the same destination twice, so the first press of Back went nowhere.
 */
private fun NavHostController.open(route: String) = navigate(route) { launchSingleTop = true }
