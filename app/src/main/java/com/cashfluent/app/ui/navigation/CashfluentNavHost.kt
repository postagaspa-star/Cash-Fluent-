package com.cashfluent.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cashfluent.app.ui.about.AboutScreen
import com.cashfluent.app.ui.game.GameScreen
import com.cashfluent.app.ui.games.GamesScreen
import com.cashfluent.app.ui.home.HomeScreen
import com.cashfluent.app.ui.league.LeagueScreen
import com.cashfluent.app.ui.module.ModuleScreen
import com.cashfluent.app.ui.settings.SettingsScreen

/**
 * Four places and three tasks.
 *
 * The places — home, the games, the board, the settings — carry the bar along the bottom
 * and are always one tap from each other. The tasks — a lesson, a game round, the About
 * page — are opened on top, keep their own back arrow, and show no bar: a lesson has its
 * own way forward in the bottom right corner, and a game round should not have four
 * tappable words beside the answer control.
 */
@Composable
fun CashfluentNavHost(navController: NavHostController = rememberNavController()) {

    fun bar(current: Section): @Composable () -> Unit = {
        SectionBar(current = current, onSelect = { navController.select(it) })
    }

    NavHost(navController = navController, startDestination = Destinations.HOME) {

        composable(Destinations.HOME) {
            HomeScreen(
                onOpenModule = { id -> navController.open(Destinations.module(id)) },
                onOpenAbout = { navController.open(Destinations.ABOUT) },
                onOpenLeague = { navController.select(Section.LEAGUE) },
                onOpenGames = { navController.select(Section.GAMES) },
                bottomBar = bar(Section.HOME),
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
                onOpenGames = { topicId -> navController.open(Destinations.games(topicId)) },
            )
        }

        composable(
            route = Destinations.GAMES_ROUTE,
            arguments = listOf(
                navArgument(Destinations.TOPIC_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            GamesScreen(
                scrollToTopicId = entry.arguments?.getString(Destinations.TOPIC_ARG),
                onOpenGame = { id -> navController.open(Destinations.game(id)) },
                onOpenLeague = { navController.select(Section.LEAGUE) },
                bottomBar = bar(Section.GAMES),
            )
        }

        composable(
            route = Destinations.GAME_ROUTE,
            arguments = listOf(navArgument(Destinations.GAME_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            val gameId = entry.arguments?.getString(Destinations.GAME_ID_ARG).orEmpty()
            GameScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onOpenGame = { id ->
                    // One game replaces the last, so "another game" never piles up a back stack.
                    navController.navigate(Destinations.game(id)) {
                        popUpTo(Destinations.GAME_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenGames = { navController.select(Section.GAMES) },
                onOpenLeague = { navController.select(Section.LEAGUE) },
            )
        }

        composable(Destinations.LEAGUE) {
            LeagueScreen(bottomBar = bar(Section.LEAGUE))
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onOpenAbout = { navController.open(Destinations.ABOUT) },
                bottomBar = bar(Section.SETTINGS),
            )
        }

        composable(Destinations.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Moving between the four places, rather than stacking them: each keeps its scroll
 * position, only one copy of it is ever on the stack, and Back from any of them returns
 * to home and then leaves the app.
 */
private fun NavHostController.select(section: Section) = navigate(section.route) {
    popUpTo(graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/**
 * Two taps in quick succession — easy on a card that fills the width of the screen —
 * used to push the same destination twice, so the first press of Back went nowhere.
 */
private fun NavHostController.open(route: String) = navigate(route) { launchSingleTop = true }
