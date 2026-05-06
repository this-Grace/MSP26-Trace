package it.unibo.trace.ui.screen.auth.magiclink

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
 * UI State for the Magic Link Sign In screen.
 */
data class MagicLinkUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isSent: Boolean = false
)

/**
 * ViewModel for handling Magic Link authentication.
 */
class MagicLinkViewModel(private val authService: AuthService) : ViewModel() {
    private val _uiState = MutableStateFlow(MagicLinkUiState())
    val uiState: StateFlow<MagicLinkUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Sends a magic link to the provided email.
     */
    fun sendMagicLink() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            UiMessenger.show("Please enter your email")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authService.sendMagicLink(email)
                _uiState.update { it.copy(isSent = true) }
                UiMessenger.show("Magic link sent! Check your inbox.")
            } catch (e: Exception) {
                UiMessenger.show(e.localizedMessage ?: "Failed to send magic link")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
