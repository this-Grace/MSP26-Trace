package it.unibo.trace.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.service.AuthService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Login screen.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * One-time events for the Login screen.
 */
sealed class LoginEvent {
    data object LoginSuccess : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

/**
 * ViewModel for handling user authentication (Email/Password and GitHub).
 */
class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Attempts to sign in with email and password.
     */
    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            viewModelScope.launch { _events.emit(LoginEvent.Error("Please fill all fields")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                AuthService.signIn(state.email, state.password)
                _events.emit(LoginEvent.LoginSuccess)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("invalid", ignoreCase = true) == true -> "Invalid email or password"
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error, please check your connection"
                    else -> "Login failed. Please try again."
                }
                _events.emit(LoginEvent.Error(msg))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Initiates GitHub OAuth authentication.
     */
    fun signInWithGithub() {
        viewModelScope.launch {
            try {
                AuthService.signInWithGithub()
            } catch (e: Exception) {
                _events.emit(LoginEvent.Error("GitHub Login failed"))
            }
        }
    }
}
