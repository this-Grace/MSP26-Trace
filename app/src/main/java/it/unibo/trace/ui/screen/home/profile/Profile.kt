package it.unibo.trace.ui.screen.home.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import it.unibo.trace.R
import it.unibo.trace.ui.composable.ThemeSelector
import it.unibo.trace.ui.composable.TraceInfoItem
import it.unibo.trace.ui.composable.TraceTopBar
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.card.TraceCard
import it.unibo.trace.ui.composable.input.EmailField
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

@Composable
fun ProfileAvatar(
    url: String,
    imageLoader: ImageLoader,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        SmallFloatingActionButton(
            onClick = onEditClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Update photo")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourcePickerSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.take_photo)) },
                leadingContent = { Icon(Icons.Default.PhotoCamera, "photo") },
                modifier = Modifier.clickable { onTakePhoto() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.choose_gallery)) },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, "gallery") },
                modifier = Modifier.clickable { onChooseGallery() }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.remove_photo),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onRemovePhoto() }
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    emailInput: String,
    isDeleting: Boolean,
    onEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_account)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.delete_account_confirm))
                EmailField(
                    value = emailInput,
                    onValueChange = onEmailChange
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
