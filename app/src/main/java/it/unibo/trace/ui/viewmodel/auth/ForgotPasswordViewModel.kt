package it.unibo.trace.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase.supabase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
 * One-time events for the ForgotPassword screen.
 */
sealed class ForgotPasswordEvent {
    data object ResetLinkSent : ForgotPasswordEvent()
    data class Error(val message: String) : ForgotPasswordEvent()
}

/**
 * ViewModel for handling password reset requests via email.
 */
class ForgotPasswordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events = _events.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Sends a password reset link to the provided email address.
     */
    fun sendResetLink() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            viewModelScope.launch { _events.emit(ForgotPasswordEvent.Error("Please enter your email")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                supabase.auth.resetPasswordForEmail(
                    email,
                    "it.unibo.trace://login-callback"
                )
                _events.emit(ForgotPasswordEvent.ResetLinkSent)
            } catch (e: Exception) {
                val msg = if (e.message?.contains("network", ignoreCase = true) == true) {
                    "Network error, please check your connection"
                } else {
                    "Failed to send reset link. Verify your email."
                }
                _events.emit(ForgotPasswordEvent.Error(msg))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
