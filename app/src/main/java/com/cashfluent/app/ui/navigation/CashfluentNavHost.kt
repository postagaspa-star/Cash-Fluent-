package com.cashfluent.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun CashfluentNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {

        composable(Destinations.HOME) {
            ScaffoldingScreen(
                title = "Home",
                note = "The module list, progress and the method card land here.",
                onBack = null,
            )
        }

        composable(
            route = Destinations.MODULE_ROUTE,
            arguments = listOf(navArgument(Destinations.MODULE_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            val moduleId = entry.arguments?.getString(Destinations.MODULE_ID_ARG).orEmpty()
            ScaffoldingScreen(
                title = "Module $moduleId",
                note = "The three blocks, the simulator and the quick check land here.",
                onBack = navController::popBackStack,
            )
        }

        composable(Destinations.SETTINGS) {
            ScaffoldingScreen(
                title = "Settings",
                note = "Currency, guided path and reset land here.",
                onBack = navController::popBackStack,
            )
        }

        composable(Destinations.ABOUT) {
            ScaffoldingScreen(
                title = "Why Cashfluent exists",
                note = "The problem, the method, and what this app is not.",
                onBack = navController::popBackStack,
            )
        }
    }
}
