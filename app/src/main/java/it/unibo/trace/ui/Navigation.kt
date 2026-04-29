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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.trace.supabase
import it.unibo.trace.ui.screen.HomeScreen
import it.unibo.trace.ui.screen.auth.ForgotPasswordScreen
import it.unibo.trace.ui.screen.auth.LoginScreen
import it.unibo.trace.ui.screen.auth.RegistrationScreen
import it.unibo.trace.ui.screen.auth.ResetPasswordScreen
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Registration : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data object ResetPassword : Route
    @Serializable data object Home : Route
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            navController.navigate(Route.Home) {
                popUpTo(Route.Login) { inclusive = true }
            }
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
    }
}

@Composable
fun ProtectedRoute(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    when (sessionStatus) {
        is SessionStatus.Authenticated -> {
            content()
        }
        is SessionStatus.NotAuthenticated -> {
            LaunchedEffect(sessionStatus) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Home) { inclusive = true }
                }
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
