package it.unibo.trace.ui.screen.home.profile

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.biometric.BiometricPrompt
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import kotlinx.coroutines.flow.update

/**
 * UI State for the Profile screen.
 */
data class ProfileUiState(
    val email: String = "",
    val avatarUrl: String? = null,
    val loginType: String = "",
    val lastLogin: LocalDateTime? = null,
    val showDeleteDialog: Boolean = false,
    val deleteConfirmEmail: String = "",
    val isDeleting: Boolean = false
) {

    val effectiveAvatarUrl: String
        get() = avatarUrl ?: "https://api.dicebear.com/9.x/avataaars/svg?seed=$email"
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

    val theme: StateFlow<AppTheme> = themeRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
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
        }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show, deleteConfirmEmail = "") }
    }

    fun updateDeleteConfirmEmail(email: String) {
        _uiState.update { it.copy(deleteConfirmEmail = email) }
    }

    fun uploadProfilePicture(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUser()?.id ?: return@launch
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                val outputStream = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()

                val imageUrl = userService.uploadAvatar(userId, bytes)
                userService.updateAvatarUrl(imageUrl)
                val timestampedUrl = "$imageUrl?t=${System.currentTimeMillis()}"

                _uiState.update { it.copy(avatarUrl = timestampedUrl) }
                UiMessenger.show("Update photo!")
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage())
            }
        }
    }

    fun getTmpUri(context: Context): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
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
