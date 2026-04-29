package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.builtin.Email
import it.unibo.trace.data.supabase.supabase
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun signUp(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
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
                supabase.auth.signUpWith(Email, "it.unibo.trace://login-callback") {
                    this.email = this@RegistrationViewModel.email
                    this.password = this@RegistrationViewModel.password
                }
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("already registered", ignoreCase = true) == true -> "Email already in use"
                    e.message?.contains("weak", ignoreCase = true) == true -> "Password is too weak"
                    else -> "Registration failed. Please try again."
                }
                onError(msg)
            }
            isLoading = false
        }
    }

    fun signUpWithGithub(onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                supabase.auth.signInWith(Github)
            }.onFailure {
                onError("GitHub Login failed")
            }
        }
    }
}