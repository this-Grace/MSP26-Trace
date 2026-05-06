package it.unibo.trace.ui.screen.home.profile

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import it.unibo.trace.ui.composable.TraceInfoItem
import it.unibo.trace.ui.composable.ThemeSelector
import it.unibo.trace.ui.composable.card.TraceCard
import it.unibo.trace.ui.composable.TraceTopBar
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.input.EmailField
import it.unibo.trace.utils.BiometricAuthenticator
import org.koin.androidx.compose.koinViewModel

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
    viewModel: ProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val uiState by viewModel.uiState.collectAsState()
    val theme by viewModel.theme.collectAsState()

    val authenticator = remember(activity) {
        activity?.let { BiometricAuthenticator(it) }
    }

    val svgImageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowDeleteDialog(false) },
            title = { Text("Delete Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This action is irreversible. Please type your email to confirm:")
                    EmailField(
                        value = uiState.deleteConfirmEmail,
                        onValueChange = { viewModel.updateDeleteConfirmEmail(it) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount() },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TraceTopBar(
                title = "Profile",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
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

            TraceCard(title = "Account Information") {
                TraceInfoItem(
                    label = "Email",
                    value = uiState.email,
                    icon = Icons.Default.Email
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                TraceInfoItem(
                    label = "Login Method",
                    value = uiState.loginType,
                    icon = Icons.Default.Badge
                )
            }

            TraceCard(title = "Appearance") {
                ThemeSelector(
                    selectedTheme = theme,
                    onThemeSelected = { viewModel.setTheme(it) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TraceButton(
                        text = "Delete",
                        onClick = {
                            val canAuth = authenticator?.canAuthenticate()
                            if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                                authenticator.authenticate(
                                    title = "Delete Account",
                                    subtitle = "Verify your identity to proceed",
                                    onSuccess = { viewModel.deleteAccount() },
                                    onError = { code, _ ->
                                        if (code != BiometricPrompt.ERROR_USER_CANCELED) {
                                            viewModel.setShowDeleteDialog(true)
                                        }
                                    }
                                )
                            } else {
                                viewModel.setShowDeleteDialog(true)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                        }
                    )

                    TraceButton(
                        text = "Sign Out",
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.weight(1f),
                        outlined = true,
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        }
                    )
                }

                Text(
                    text = "Last login: ${uiState.formattedLastLogin}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
