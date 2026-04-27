package it.unibo.trace.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import it.unibo.trace.ui.screen.DetailsScreen
import it.unibo.trace.ui.screen.HomeScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Details : Route
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Home> {
            HomeScreen(navController)
        }
        composable<Route.Details> {
            DetailsScreen(navController)
        }
    }
}
