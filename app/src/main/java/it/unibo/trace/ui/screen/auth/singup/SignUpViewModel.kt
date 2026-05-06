package it.unibo.trace.ui.screen.auth.singup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.utils.MessageDuration
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Registration screen.
 */
data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel for handling new user registration.
 */
class SignUpViewModel(private val authService: AuthService) : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    /**
     * Attempts to sign up a new user with email and password.
     */
    fun signUp(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank()) {
            UiMessenger.show("Please fill all fields")
            return
        }
        if (state.password != state.confirmPassword) {
            UiMessenger.show("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authService.signUp(state.email, state.password)
                UiMessenger.show("Account created! Please check your email.", MessageDuration.LONG)
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("already registered", ignoreCase = true) == true -> "Email already in use"
                    e.message?.contains("weak", ignoreCase = true) == true -> "Password is too weak"
                    else -> "Registration failed. Please try again."
                }
                UiMessenger.show(msg)
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
                authService.signInWithGithub()
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "GitHub Login failed")
            }
        }
    }
}
