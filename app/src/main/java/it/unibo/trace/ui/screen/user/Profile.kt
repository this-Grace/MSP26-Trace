package it.unibo.trace.ui.screen.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import io.github.jan.supabase.auth.auth
import it.unibo.trace.supabase
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.components.TopBar
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()

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
                    scope.launch {
                        try {
                            supabase.auth.signOut()
                            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(Route.Login) {
                                popUpTo(Route.Home) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Logout failed: ${e.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
