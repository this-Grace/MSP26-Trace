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
 * UI State for the ResetPassword screen.
 */
data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * One-time events for the ResetPassword screen.
 */
sealed class ResetPasswordEvent {
    data object PasswordUpdated : ResetPasswordEvent()
    data class Error(val message: String) : ResetPasswordEvent()
}

/**
 * ViewModel for handling password updates (e.g., after clicking a reset link).
 */
class ResetPasswordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResetPasswordEvent>()
    val events = _events.asSharedFlow()

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
     * Updates the user's password in Supabase.
     */
    fun updatePassword() {
        val state = _uiState.value
        if (state.password.isBlank() || state.confirmPassword.isBlank()) {
            viewModelScope.launch { _events.emit(ResetPasswordEvent.Error("Please fill all fields")) }
            return
        }
        if (state.password != state.confirmPassword) {
            viewModelScope.launch { _events.emit(ResetPasswordEvent.Error("Passwords do not match")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                AuthService.updatePassword(state.password)
                _events.emit(ResetPasswordEvent.PasswordUpdated)
            } catch (e: Exception) {
                _events.emit(ResetPasswordEvent.Error("Failed to update password. Try again."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
