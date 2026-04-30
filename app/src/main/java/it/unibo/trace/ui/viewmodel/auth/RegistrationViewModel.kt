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
 * UI State for the Registration screen.
 */
data class RegistrationUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * One-time events for the Registration screen.
 */
sealed class RegistrationEvent {
    data object RegistrationSuccess : RegistrationEvent()
    data class Error(val message: String) : RegistrationEvent()
}

/**
 * ViewModel for handling new user registration.
 */
class RegistrationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events = _events.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Attempts to sign up a new user with email and password.
     */
    fun signUp() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank()) {
            viewModelScope.launch { _events.emit(RegistrationEvent.Error("Please fill all fields")) }
            return
        }
        if (state.password != state.confirmPassword) {
            viewModelScope.launch { _events.emit(RegistrationEvent.Error("Passwords do not match")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                AuthService.signUp(state.email, state.password)
                _events.emit(RegistrationEvent.RegistrationSuccess)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("already registered", ignoreCase = true) == true -> "Email already in use"
                    e.message?.contains("weak", ignoreCase = true) == true -> "Password is too weak"
                    else -> "Registration failed. Please try again."
                }
                _events.emit(RegistrationEvent.Error(msg))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Initiates GitHub OAuth registration/login.
     */
    fun signUpWithGithub() {
        viewModelScope.launch {
            try {
                AuthService.signInWithGithub()
            } catch (e: Exception) {
                _events.emit(RegistrationEvent.Error("GitHub Login failed"))
            }
        }
    }
}
