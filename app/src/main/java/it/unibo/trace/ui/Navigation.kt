package it.unibo.trace.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

sealed interface Route {
    @Serializable
    data object Login : Route
    @Serializable
    data object Registration : Route
    @Serializable
    data object ForgotPassword : Route
    @Serializable
    data object ResetPassword : Route
    @Serializable
    data object Home : Route
    @Serializable
    data object Profile : Route
    @Serializable
    data object AddTodo : Route
}

@Composable
fun NavGraph(
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val sessionStatus by mainViewModel.sessionStatus.collectAsState()
    val pendingReset by mainViewModel.pendingReset.collectAsState()

    LaunchedEffect(pendingReset) {
        if (pendingReset) {
            navController.navigate(Route.ResetPassword) {
                launchSingleTop = true
            }
            mainViewModel.clearPendingReset()
        }
    }

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.NotAuthenticated -> {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Home) { inclusive = true }
                }
            }

            is SessionStatus.Authenticated -> {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute?.contains("auth") == true || currentRoute?.contains("Login") == true) {
                    navController.navigate(Route.Home) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                }
            }

            else -> {}
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
            ProtectedRoute(sessionStatus) {
                HomeScreen(navController)
            }
        }
        composable<Route.Profile> {
            ProtectedRoute(sessionStatus) {
                ProfileScreen(navController)
            }
        }
        composable<Route.AddTodo> {
            ProtectedRoute(sessionStatus) {
                AddTodoScreen(navController)
            }
        }
    }
}

@Composable
fun ProtectedRoute(
    sessionStatus: SessionStatus,
    content: @Composable () -> Unit
) {
    when (sessionStatus) {
        is SessionStatus.Authenticated -> {
            content()
        }

        is SessionStatus.Initializing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        else -> {}
    }
}
