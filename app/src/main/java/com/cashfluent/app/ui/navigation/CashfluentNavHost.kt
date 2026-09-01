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
                onOpenModule = { id -> navController.navigate(Destinations.module(id)) },
                onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                onOpenAbout = { navController.navigate(Destinations.ABOUT) },
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
                    }
                },
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(Destinations.ABOUT) },
            )
        }

        composable(Destinations.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
