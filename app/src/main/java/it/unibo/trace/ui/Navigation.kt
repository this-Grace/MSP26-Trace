package it.unibo.trace.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import it.unibo.trace.ui.screen.DetailsScreen
import it.unibo.trace.ui.screen.HomeScreen
import it.unibo.trace.ui.screen.auth.ForgotPasswordScreen
import it.unibo.trace.ui.screen.auth.LoginScreen
import it.unibo.trace.ui.screen.auth.RegistrationScreen
import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable data object Login : Route
    @Serializable data object Registration : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data object Home : Route
    @Serializable data object Details : Route
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Login
    ) {
        composable<Route.Login> {
            LoginScreen(navController)
        }
        composable<Route.Registration> {
            RegistrationScreen(navController)
        }
        composable<Route.ForgotPassword> {
            ForgotPasswordScreen(navController)
        }
        composable<Route.Home> {
            HomeScreen(navController)
        }
        composable<Route.Details> {
            DetailsScreen(navController)
        }
    }
}

