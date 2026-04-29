package it.unibo.trace.ui.screen.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.composable.ProfileInfoItem
import it.unibo.trace.ui.composable.ThemeSelector
import it.unibo.trace.ui.composable.TopBar
import it.unibo.trace.ui.viewmodel.user.ProfileEvent
import it.unibo.trace.ui.viewmodel.user.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen displaying the user's profile information and theme settings.
 *
 * @param navController Controller for navigating between screens.
 * @param viewModel ViewModel providing the screen's state and actions.
 */
 @Composable
 fun ProfileScreen(
     navController: NavHostController,
     modifier: Modifier = Modifier,
     viewModel: ProfileViewModel = viewModel()
 ) {
     val context = LocalContext.current
     val uiState by viewModel.uiState.collectAsState()
     val theme by viewModel.theme.collectAsState()

     LaunchedEffect(Unit) {
         viewModel.events.collectLatest { event ->
             when (event) {
                 is ProfileEvent.LogoutSuccess -> {
                     Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                     navController.navigate(Route.Login) {
                         popUpTo(Route.Home) { inclusive = true }
                     }
                 }
                 is ProfileEvent.Error -> {
                     Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                 }
             }
         }
     }

     Scaffold(
         modifier = modifier,
         topBar = {
            TopBar(
                title = "Profile",
                onNavigateBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.logout() },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfileInfoItem(label = "Email", value = uiState.email)
            ProfileInfoItem(label = "Login Type", value = uiState.loginType)
            ProfileInfoItem(label = "Last Login", value = uiState.lastLogin)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ThemeSelector(
                selectedTheme = theme,
                onThemeSelected = { viewModel.setTheme(it) }
            )
        }
    }
}
