package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase.supabase
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
        private set

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun updatePassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (password.isBlank() || confirmPassword.isBlank()) {
            onError("Please fill all fields")
            return
        }
        if (password != confirmPassword) {
            onError("Passwords do not match")
            return
        }

        viewModelScope.launch {
            isLoading = true
            runCatching {
                supabase.auth.updateUser {
                    this.password = this@ResetPasswordViewModel.password
                }
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onError("Failed to update password. Try again.")
            }
            isLoading = false
        }
    }
}