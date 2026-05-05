package it.unibo.trace.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.UserService
import it.unibo.trace.ui.theme.AppTheme
import it.unibo.trace.utils.MessageDuration
import it.unibo.trace.utils.UiMessenger
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
    val loginType: String = "",
    val lastLogin: LocalDateTime? = null,
    val showDeleteDialog: Boolean = false,
    val deleteConfirmEmail: String = "",
    val isDeleting: Boolean = false
) {
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
        userService.getProfileInfo()?.let { profile ->
            _uiState.update {
                it.copy(
                    email = profile.email,
                    loginType = profile.loginType,
                    lastLogin = profile.lastLogin
                )
            }
        }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show, deleteConfirmEmail = "") }
    }

    fun updateDeleteConfirmEmail(email: String) {
        _uiState.update { it.copy(deleteConfirmEmail = email) }
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
                UiMessenger.show(e.localizedMessage ?: "Failed to logout")
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
                UiMessenger.show("Account deleted successfully!", MessageDuration.LONG)
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "Error deleting account")
            } finally {
                _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
            }
        }
    }
}
