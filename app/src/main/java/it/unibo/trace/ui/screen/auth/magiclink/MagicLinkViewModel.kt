package it.unibo.trace.ui.screen.auth.magiclink

import it.unibo.trace.R
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
            UiMessenger.show(R.string.error_enter_email)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                authService.sendMagicLink(email)
                _uiState.update { it.copy(isSent = true) }
                UiMessenger.show(R.string.magic_link_sent_success)
            } catch (e: Exception) {
                UiMessenger.show(R.string.error_magic_link_failed)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
