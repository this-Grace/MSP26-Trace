package it.unibo.trace.ui.screen.auth.resetpassword

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
 * UI State for the ResetPassword screen.
 */
data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

/**
 * ViewModel for handling password updates (e.g., after clicking a reset link).
 */
class ResetPasswordViewModel(private val authService: AuthService) : ViewModel() {
    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password) }
    }

    /**
     * Updates the user's password in Supabase.
     */
    fun updatePassword() {
        val state = _uiState.value
        if (state.password.isBlank() || state.confirmPassword.isBlank()) {
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
                authService.updatePassword(state.password)
                UiMessenger.show("Password updated successfully!", MessageDuration.LONG)
                authService.signOut()
                _uiState.update { it.copy(isSuccess = true) }
            } catch (e: Exception) {
                UiMessenger.show("Error: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
