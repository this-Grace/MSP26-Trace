package it.unibo.trace.ui.viewmodel.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.UserService
import it.unibo.trace.ui.theme.AppTheme
import it.unibo.trace.utils.MessageDuration
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * UI State for the Profile screen.
 */
data class ProfileUiState(
    val email: String = "",
    val loginType: String = "",
    val lastLogin: LocalDateTime? = null
)

/**
 * One-time events for the Profile screen.
 */
sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data object DeleteSuccess : ProfileEvent()
//    data class Error(val message: String) : ProfileEvent()
}

/**
 * ViewModel for managing user profile data and application theme settings.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val themeRepository = ThemeRepository(application)
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

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
        UserService.getProfileInfo()?.let { profile ->
            _uiState.value = ProfileUiState(
                email = profile.email,
                loginType = profile.loginType,
                lastLogin = profile.lastLogin
            )
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
     * Signs out the current user and emits a [ProfileEvent.LogoutSuccess] event.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                AuthService.signOut()
                UiMessenger.show("Logout effettuato")
                _events.emit(ProfileEvent.LogoutSuccess)
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "Errore logout")
            }
        }
    }

    /**
     * Deletes the user account and signs out.
     */
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                UserService.deleteAccount()
                AuthService.signOut()
                UiMessenger.show("Account eliminato correttamente", MessageDuration.LONG)
                _events.emit(ProfileEvent.DeleteSuccess)
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "Errore eliminazione account")
            }
        }
    }
}
