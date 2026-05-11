package it.unibo.trace.ui.screen.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Login screen.
 */
data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel for handling user authentication (Email/Password and GitHub).
 */
class SignInViewModel(private val authService: AuthService) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Attempts to sign in with email and password.
     */
    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            UiMessenger.show("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authService.signIn(state.email, state.password)
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("invalid", ignoreCase = true) == true -> "Invalid email or password"
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error, please check your connection"
                    else -> "Login failed. Please try again."
                }
                UiMessenger.show(msg)
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
                authService.signInWithGithub()
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "GitHub Login failed")
            }
        }
    }
}
