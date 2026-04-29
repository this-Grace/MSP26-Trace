package it.unibo.trace.ui.viewmodel.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

/**
 * UI State for the Profile screen.
 */
data class ProfileUiState(
    val email: String = "",
    val loginType: String = "",
    val lastLogin: String = ""
)

/**
 * One-time events for the Profile screen.
 */
sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
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

    @OptIn(ExperimentalTime::class)
    private fun loadUserProfile() {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            _uiState.value = ProfileUiState(
                email = user.email ?: "Not available",
                loginType = user.appMetadata?.get("provider")?.jsonPrimitive?.contentOrNull
                    ?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                lastLogin = user.lastSignInAt?.toString() ?: "Never"
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
                supabase.auth.signOut()
                _events.emit(ProfileEvent.LogoutSuccess)
            } catch (e: Exception) {
                _events.emit(ProfileEvent.Error(e.localizedMessage ?: "Logout failed"))
            }
        }
    }
}
