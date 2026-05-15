package it.unibo.trace.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.trace.ui.screen.home.task.add.AddTodoScreen
import it.unibo.trace.ui.screen.home.task.detail.TodoDetailScreen
import it.unibo.trace.ui.screen.home.HomeScreen
import it.unibo.trace.ui.screen.home.profile.ProfileScreen
import it.unibo.trace.ui.screen.auth.forgotpassword.ForgotPasswordScreen
import it.unibo.trace.ui.screen.auth.signin.SignInScreen
import it.unibo.trace.ui.screen.auth.magiclink.MagicLinkSignInScreen
import it.unibo.trace.ui.screen.auth.signup.SignUpScreen
import it.unibo.trace.ui.screen.auth.resetpassword.ResetPasswordScreen
import it.unibo.trace.ui.screen.home.map.MapScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Registration : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data object ResetPassword : Route
    @Serializable data object MagicLink : Route
    @Serializable data object Home : Route
    @Serializable data object Profile : Route
    @Serializable data object AddTodo : Route
    @Serializable data object Map : Route
    @Serializable data class TodoDetail(val id: Long) : Route
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val sessionStatus by mainViewModel.sessionStatus.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.navigationEvent.collect { routeStr ->
            if (routeStr == "reset-password") {
                navController.navigate(Route.ResetPassword) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Home
    ) {
        composable<Route.Login> {
            UnauthenticatedRoute(sessionStatus, navController) {
                SignInScreen(navController)
            }
        }
        composable<Route.Registration> {
            UnauthenticatedRoute(sessionStatus, navController) {
                SignUpScreen(navController)
            }
        }
        composable<Route.ForgotPassword> {
            UnauthenticatedRoute(sessionStatus, navController) {
                ForgotPasswordScreen(navController)
            }
        }
        composable<Route.MagicLink> {
            UnauthenticatedRoute(sessionStatus, navController) {
                MagicLinkSignInScreen(navController)
            }
        }
        composable<Route.ResetPassword> {
            ResetPasswordScreen(navController)
        }
        composable<Route.Home> {
            ProtectedRoute(sessionStatus, navController) {
                HomeScreen(navController)
            }
        }
        composable<Route.Profile> {
            ProtectedRoute(sessionStatus, navController) {
                ProfileScreen(navController)
            }
        }
        composable<Route.AddTodo> {
            ProtectedRoute(sessionStatus, navController) {
                AddTodoScreen(navController)
            }
        }
        composable<Route.TodoDetail> { backStackEntry ->
            ProtectedRoute(sessionStatus, navController) {
                val detail = backStackEntry.toRoute<Route.TodoDetail>()
                TodoDetailScreen(navController, detail.id)
            }
        }
        composable<Route.Map> {
            ProtectedRoute(sessionStatus, navController) {
                MapScreen(navController)
            }
        }
    }
}

@Composable
fun ProtectedRoute(
    sessionStatus: SessionStatus,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    when (sessionStatus) {
        is SessionStatus.Authenticated -> content()
        is SessionStatus.NotAuthenticated -> {
            LaunchedEffect(sessionStatus) {
                navController.navigate(Route.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        else -> { /* Initializing: wait */ }
    }
}

@Composable
fun UnauthenticatedRoute(
    sessionStatus: SessionStatus,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    when (sessionStatus) {
        is SessionStatus.NotAuthenticated -> content()
        is SessionStatus.Authenticated -> {
            LaunchedEffect(sessionStatus) {
                // Only navigate to Home if we are currently on an unauthenticated route
                // and not already navigating elsewhere.
                if (currentRoute != Route.ResetPassword::class.qualifiedName) {
                    navController.navigate(Route.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
        else -> { /* Initializing: wait */ }
    }
}
