package it.unibo.trace.ui.screen.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.composable.ProfileInfoItem
import it.unibo.trace.ui.composable.TopBar
import it.unibo.trace.ui.viewmodel.ProfileViewModel
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()

    val email = user?.email ?: "Not available"
    val loginType = user?.appMetadata?.get("provider")?.jsonPrimitive?.contentOrNull ?: "Unknown"
    val lastLogin = user?.lastSignInAt?.toString() ?: "Never"

    Scaffold(
        topBar = {
            TopBar(
                title = "Profile",
                onNavigateBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.logout(
                        onSuccess = {
                            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(Route.Login) {
                                popUpTo(Route.Home) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, "Logout failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
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
            ProfileInfoItem(label = "Email", value = email)
            ProfileInfoItem(label = "Login Type", value = loginType.replaceFirstChar { it.uppercase() })
            ProfileInfoItem(label = "Last Login", value = lastLogin)
        }
    }
}
