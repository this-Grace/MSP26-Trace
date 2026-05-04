package it.unibo.trace.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.trace.ui.screen.AddTodoScreen
import it.unibo.trace.ui.screen.HomeScreen
import it.unibo.trace.ui.screen.user.ProfileScreen
import it.unibo.trace.ui.screen.auth.ForgotPasswordScreen
import it.unibo.trace.ui.screen.auth.LoginScreen
import it.unibo.trace.ui.screen.auth.RegistrationScreen
import it.unibo.trace.ui.screen.auth.ResetPasswordScreen
import it.unibo.trace.ui.viewmodel.MainViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val sessionStatus by mainViewModel.sessionStatus.collectAsState()
    val isAuthenticated = sessionStatus is SessionStatus.Authenticated

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Login> {
            UnauthenticatedRoute(isAuthenticated, navController) {
                LoginScreen(navController)
            }
        }
        composable<Route.Registration> {
            UnauthenticatedRoute(isAuthenticated, navController) {
                RegistrationScreen(navController)
            }
        }
        composable<Route.ForgotPassword> {
            UnauthenticatedRoute(isAuthenticated, navController) {
                ForgotPasswordScreen(navController)
            }
        }
        composable<Route.ResetPassword> {
            ResetPasswordScreen(navController)
        }
        composable<Route.Home> {
            ProtectedRoute(isAuthenticated, navController) {
                HomeScreen(navController)
            }
        }
        composable<Route.Profile> {
            ProtectedRoute(isAuthenticated, navController) {
                ProfileScreen(navController)
            }
        }
        composable<Route.AddTodo> {
            ProtectedRoute(isAuthenticated, navController) {
                AddTodoScreen(navController)
            }
        }
    }
}

@Composable
fun ProtectedRoute(
    isAuthenticated: Boolean,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    if (isAuthenticated) {
        content()
    } else {
        LaunchedEffect(Unit) {
            navController.navigate(Route.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

@Composable
fun UnauthenticatedRoute(
    isAuthenticated: Boolean,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    if (!isAuthenticated) {
        content()
    } else {
        LaunchedEffect(Unit) {
            navController.navigate(Route.Home) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
