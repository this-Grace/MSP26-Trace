package it.unibo.trace.ui.screen.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.composable.ProfileActionButton
import it.unibo.trace.ui.composable.ProfileInfoItem
import it.unibo.trace.ui.composable.ThemeSelector
import it.unibo.trace.ui.composable.TopBar
import it.unibo.trace.ui.viewmodel.user.ProfileEvent
import it.unibo.trace.ui.viewmodel.user.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
                is ProfileEvent.DeleteSuccess -> {
                    Toast.makeText(context, "Account successfully deleted", Toast.LENGTH_LONG).show()
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

    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                title = "Profile",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
            ) {
                AsyncImage(
                    model = "https://api.dicebear.com/9.x/avataaars/svg?seed=${uiState.email}",
                    contentDescription = "Avatar",
                    imageLoader = svgImageLoader,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Informations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoItem("Email", uiState.email)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoItem("Login type", uiState.loginType)
                    Spacer(modifier = Modifier.height(24.dp))
                    ThemeSelector(
                        selectedTheme = theme,
                        onThemeSelected = { viewModel.setTheme(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileActionButton(
                    text = "DELETE",
                    icon = Icons.Default.Delete,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { viewModel.deleteAccount() },
                    modifier = Modifier.weight(1f)
                )

                ProfileActionButton(
                    text = "SIGN OUT",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = { viewModel.logout() },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Last login: ${
                    uiState.lastLogin?.format(
                        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.ITALY)
                    ) ?: "Never"
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
