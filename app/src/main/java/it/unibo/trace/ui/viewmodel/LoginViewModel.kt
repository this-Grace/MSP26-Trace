package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.builtin.Email
import it.unibo.trace.data.supabase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
    }

    fun signIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Please fill all fields")
            return
        }

        viewModelScope.launch {
            isLoading = true
            runCatching {
                supabase.auth.signInWith(Email) {
                    this.email = this@LoginViewModel.email
                    this.password = this@LoginViewModel.password
                }
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                val msg = when {
                    e.message?.contains("invalid", ignoreCase = true) == true -> "Invalid email or password"
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error, please check your connection"
                    else -> "Login failed. Please try again."
                }
                onError(msg)
            }
            isLoading = false
        }
    }

    fun signInWithGithub(onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                supabase.auth.signInWith(Github)
            }.onFailure {
                onError("GitHub Login failed")
            }
        }
    }
}