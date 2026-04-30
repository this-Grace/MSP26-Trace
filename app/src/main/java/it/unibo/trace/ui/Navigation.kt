package it.unibo.trace.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.screen.AddTodoScreen
import it.unibo.trace.ui.screen.HomeScreen
import it.unibo.trace.ui.screen.user.ProfileScreen
import it.unibo.trace.ui.screen.auth.ForgotPasswordScreen
import it.unibo.trace.ui.screen.auth.LoginScreen
import it.unibo.trace.ui.screen.auth.RegistrationScreen
import it.unibo.trace.ui.screen.auth.ResetPasswordScreen
import it.unibo.trace.ui.viewmodel.MainViewModel
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Registration : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data object ResetPassword : Route
    @Serializable data object Home : Route
    @Serializable data object Profile : Route
    @Serializable data object AddTodo : Route
}

@Composable
fun NavGraph(
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val pendingReset by mainViewModel.pendingReset.collectAsState()
    LaunchedEffect(pendingReset) {
        if (pendingReset) {
            navController.navigate(Route.ResetPassword) {
                launchSingleTop = true
            }
            mainViewModel.clearPendingReset()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Home
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
        composable<Route.ResetPassword> {
            ResetPasswordScreen(navController)
        }
        composable<Route.Home> {
            ProtectedRoute(navController) {
                HomeScreen(navController)
            }
        }
        composable<Route.Profile> {
            ProtectedRoute(navController) {
                ProfileScreen(navController)
            }
        }
        composable<Route.AddTodo> {
            ProtectedRoute(navController) {
                AddTodoScreen(navController)
            }
        }
    }
}

@Composable
fun ProtectedRoute(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val session = supabase.auth.currentSessionOrNull()
    if (session == null) {
        LaunchedEffect(Unit) {
            navController.navigate(Route.Login) {
                popUpTo(Route.Home) { inclusive = true }
            }
        }
    } else {
        content()
    }
}
