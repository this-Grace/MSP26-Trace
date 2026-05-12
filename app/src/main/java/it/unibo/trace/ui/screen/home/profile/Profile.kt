package it.unibo.trace.ui.screen.home.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.decode.SvgDecoder
import it.unibo.trace.R
import it.unibo.trace.ui.composable.TraceInfoItem
import it.unibo.trace.ui.composable.TraceTopBar
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.card.TraceCard
import it.unibo.trace.ui.composable.dialog.DeleteAccountDialog
import it.unibo.trace.ui.composable.image.ProfileAvatar
import it.unibo.trace.ui.composable.sheet.ImageSourcePickerSheet
import it.unibo.trace.ui.theme.AppTheme
import it.unibo.trace.utils.BiometricAuthenticator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            uri -> uri?.let { viewModel.uploadProfilePicture(it, context.contentResolver) }
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            success -> if (success) viewModel.onCameraResult(context.contentResolver)
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted -> if (isGranted) viewModel.startCamera(context, cameraLauncher)
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
        DeleteAccountDialog(
            emailInput = uiState.deleteConfirmEmail,
            isDeleting = uiState.isDeleting,
            onEmailChange = viewModel::updateDeleteConfirmEmail,
            onConfirm = viewModel::deleteAccount,
            onDismiss = { viewModel.setShowDeleteDialog(false) }
        )
    }

    if (uiState.showImagePicker) {
        ImageSourcePickerSheet(
            onDismiss = { viewModel.setShowImagePicker(false) },
            onTakePhoto = {
                viewModel.setShowImagePicker(false)
                viewModel.handleCameraAction(context, permissionLauncher, cameraLauncher)
            },
            onChooseGallery = {
                viewModel.setShowImagePicker(false)
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                viewModel.setShowImagePicker(false)
                viewModel.removeProfilePicture()
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TraceTopBar(
                title = stringResource(R.string.profile_title),
                onNavigateBack = { navController.popBackStack() },
                actions = {
                    val icon = when (theme) {
                        AppTheme.LIGHT -> Icons.Rounded.LightMode
                        AppTheme.DARK -> Icons.Rounded.Brightness4
                        AppTheme.SYSTEM -> Icons.Rounded.BrightnessAuto
                    }
                    IconButton(onClick = {
                        val nextTheme = when (theme) {
                            AppTheme.SYSTEM -> AppTheme.LIGHT
                            AppTheme.LIGHT -> AppTheme.DARK
                            AppTheme.DARK -> AppTheme.SYSTEM
                        }
                        viewModel.setTheme(nextTheme)
                    }) {
                        Icon(imageVector = icon, contentDescription = "Toggle theme")
                    }
                }
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
            ProfileAvatar(
                url = uiState.effectiveAvatarUrl,
                imageLoader = svgImageLoader,
                onEditClick = { viewModel.setShowImagePicker(true) }
            )

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
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete")
                        }
                    )

                    TraceButton(
                        text = stringResource(R.string.sign_out),
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.weight(1f),
                        outlined = true,
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
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
