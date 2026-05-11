package it.unibo.trace.ui.screen.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.service.AuthService
import androidx.compose.material3.SnackbarDuration
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the ForgotPassword screen.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel for handling password reset requests via email.
 */
class ForgotPasswordViewModel(private val authService: AuthService) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Sends a password reset link to the provided email address.
     */
    fun sendResetLink() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            UiMessenger.show("Please enter your email")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authService.sendResetPasswordEmail(email)
                UiMessenger.show("Instructions sent! Please check your inbox.", SnackbarDuration.Long)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("network", ignoreCase = true) == true) {
                    "Network error, please check your connection"
                } else {
                    "Failed to send reset link. Verify your email."
                }
                UiMessenger.show(msg)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
