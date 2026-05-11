package it.unibo.trace.ui.screen.home.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.res.stringResource
import it.unibo.trace.R
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.uploadProfilePicture(it, context.contentResolver) }
    }

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
            title = { Text(stringResource(R.string.delete_account)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.delete_account_confirm))
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
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TraceTopBar(
                title = stringResource(R.string.profile_title),
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
                    model = uiState.effectiveAvatarUrl,
                    contentDescription = stringResource(R.string.avatar_content_description),
                    imageLoader = svgImageLoader,
                    modifier = Modifier.fillMaxSize()
                )

                SmallFloatingActionButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Update profile photo",
                    )
                }
            }

            TraceCard(title = stringResource(R.string.account_info)) {
                TraceInfoItem(
                    label = stringResource(R.string.email_label),
                    value = uiState.email,
                    icon = Icons.Default.Email
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                TraceInfoItem(
                    label = stringResource(R.string.login_method_label),
                    value = uiState.loginType,
                    icon = Icons.Default.Badge
                )
            }

            TraceCard(title = stringResource(R.string.appearance)) {
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
                        text = stringResource(R.string.delete),
                        onClick = { viewModel.handleDeleteClick(authenticator, context) },
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                        }
                    )

                    TraceButton(
                        text = stringResource(R.string.sign_out),
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.weight(1f),
                        outlined = true,
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.last_login, uiState.formattedLastLogin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
