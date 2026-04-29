package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun sendResetLink(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email")
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                supabase.auth.resetPasswordForEmail(
                    email,
                    "it.unibo.trace://login-callback"
                )
                onSuccess()
            } catch (e: Exception) {
                val msg = if (e.message?.contains("network", ignoreCase = true) == true) {
                    "Network error, please check your connection"
                } else {
                    "Failed to send reset link. Verify your email."
                }
                onError(msg)
            } finally {
                isLoading = false
            }
        }
    }
}
