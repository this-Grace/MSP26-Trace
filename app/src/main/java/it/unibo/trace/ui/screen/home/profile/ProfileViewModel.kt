package it.unibo.trace.ui.screen.home.profile

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.R
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.UserService
import it.unibo.trace.ui.theme.AppTheme
import androidx.compose.material3.SnackbarDuration
import it.unibo.trace.utils.BiometricAuthenticator
import it.unibo.trace.utils.UiMessenger
import it.unibo.trace.utils.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * UI State for the Profile screen.
 *
 * @property email The user's email address.
 * @property avatarUrl The URL of the user's avatar image.
 * @property loginType The method used for login (e.g., magic link).
 * @property lastLogin The timestamp of the last successful login.
 * @property showDeleteDialog Whether the account deletion dialog is visible.
 * @property showImagePicker Whether the image picker dialog is visible.
 * @property deleteConfirmEmail The email entered by the user to confirm deletion.
 * @property isDeleting Whether an account deletion process is currently in progress.
 */
data class ProfileUiState(
    val email: String = "",
    val avatarUrl: String? = null,
    val loginType: String = "",
    val lastLogin: LocalDateTime? = null,
    val showDeleteDialog: Boolean = false,
    val showImagePicker: Boolean = false,
    val deleteConfirmEmail: String = "",
    val isDeleting: Boolean = false
) {
    /**
     * Returns the user's avatar URL or a fallback DiceBear SVG if none exists.
     */
    val effectiveAvatarUrl: String
        get() = avatarUrl ?: "https://api.dicebear.com/9.x/avataaars/svg?seed=$email"

    /**
     * Italy-formatted last login string.
     */
    val formattedLastLogin: String
        get() = lastLogin?.format(
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.ITALY)
        ) ?: "Never"
}

/**
 * ViewModel for managing user profile data and application theme settings.
 */
class ProfileViewModel(
    private val themeRepository: ThemeRepository,
    private val authService: AuthService,
    private val userService: UserService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var tempUri: Uri? = null

    val theme: StateFlow<AppTheme> = themeRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    init {
        loadUserProfile()
    }

    /**
     * Fetches the current user's profile information from the database.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                userService.getProfileInfo()?.let { profile ->
                    _uiState.update {
                        it.copy(
                            email = profile.email,
                            avatarUrl = profile.avatarUrl,
                            loginType = profile.loginType,
                            lastLogin = profile.lastLogin
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fail or log, as getProfileInfo handles its own logical errors
            }
        }
    }

    /**
     * Sets the visibility of the image picker dialog.
     */
    fun setShowImagePicker(show: Boolean) {
        _uiState.update { it.copy(showImagePicker = show) }
    }

    /**
     * Decides whether to launch the camera or request permissions.
     */
    fun handleCameraAction(
        context: Context,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startCamera(context, cameraLauncher)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Prepares a temporary file and launches the camera.
     */
    fun startCamera(context: Context, launcher: ManagedActivityResultLauncher<Uri, Boolean>) {
        tempUri = getTmpUri(context)
        tempUri?.let { launcher.launch(it) }
    }

    /**
     * Called when the camera returns a success result.
     */
    fun onCameraResult(contentResolver: ContentResolver) {
        tempUri?.let { uploadProfilePicture(it, contentResolver) }
    }

    /**
     * Compresses and uploads the selected image as the user's profile avatar.
     */
    fun uploadProfilePicture(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.id ?: return@launch
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) {
                    UiMessenger.show("Could not decode image")
                    return@launch
                }

                // Scale down if necessary
                val maxDimension = 800
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                    val scale = maxDimension.toFloat() / maxOf(width, height)
                    Bitmap.createScaledBitmap(
                        originalBitmap,
                        (width * scale).toInt(),
                        (height * scale).toInt(),
                        true
                    )
                } else {
                    originalBitmap
                }

                val outputStream = ByteArrayOutputStream()
                // Use JPEG and 70 quality for compression to keep file size small
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()

                val imageUrl = userService.uploadAvatar(userId, bytes)
                userService.updateAvatarUrl(imageUrl)
                val timestampedUrl = "$imageUrl?t=${System.currentTimeMillis()}"

                _uiState.update { it.copy(avatarUrl = timestampedUrl) }
                UiMessenger.show("Photo updated successfully!")

                // Clean up bitmaps
                if (scaledBitmap != originalBitmap) {
                    scaledBitmap.recycle()
                }
                originalBitmap.recycle()
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage())
            }
        }
    }

    /**
     * Deletes the current user's profile picture and updates the state.
     */
    fun removeProfilePicture() {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.id ?: return@launch

                userService.deleteAvatar(userId)
                _uiState.update { it.copy(avatarUrl = null) }

                UiMessenger.show("Photo removed")
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage())
            }
        }
    }

    /**
     * Creates a temporary file URI for camera capture.
     */
    fun getTmpUri(context: Context): Uri {
        val tmpFile = File.createTempFile("avatar_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tmpFile
        )
    }

    /**
     * Sets the visibility of the account deletion confirmation dialog.
     */
    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show, deleteConfirmEmail = "") }
    }

    /**
     * Updates the email string entered to confirm account deletion.
     */
    fun updateDeleteConfirmEmail(email: String) {
        _uiState.update { it.copy(deleteConfirmEmail = email) }
    }

    /**
     * Handles the click on the delete account button, deciding whether to show
     * biometric authentication or the fallback delete dialog.
     */
    fun handleDeleteClick(authenticator: BiometricAuthenticator?, context: Context) {
        val canAuth = authenticator?.canAuthenticate()
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            authenticator.authenticate(
                title = context.getString(R.string.delete_account),
                subtitle = context.getString(R.string.verify_identity),
                onSuccess = { deleteAccount() },
                onError = { code, _ ->
                    if (code != BiometricPrompt.ERROR_USER_CANCELED) {
                        setShowDeleteDialog(true)
                    }
                }
            )
        } else {
            setShowDeleteDialog(true)
        }
    }

    /**
     * Updates the application theme preference.
     */
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeRepository.setTheme(theme)
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authService.signOut()
                UiMessenger.show("Logout done successfully!")
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage())
            }
        }
    }

    /**
     * Deletes the user account and signs out.
     */
    fun deleteAccount() {
        if (_uiState.value.showDeleteDialog && _uiState.value.deleteConfirmEmail != _uiState.value.email) {
            UiMessenger.show("Email does not match")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                val userId = authService.getCurrentUser()?.id ?: return@launch
                userService.deleteAvatar(userId)
                userService.deleteAccount()
                authService.signOut()
                UiMessenger.show("Account deleted successfully!", SnackbarDuration.Long)
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage())
            } finally {
                _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
            }
        }
    }
}
