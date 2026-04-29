package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import it.unibo.trace.data.TodoItem
import it.unibo.trace.data.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.auth.auth

class HomeViewModel : ViewModel() {
    var items by mutableStateOf<List<TodoItem>>(listOf())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchTodos()
    }

    fun fetchTodos() {
        viewModelScope.launch {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    items = supabase.from("Todos")
                        .select().decodeList<TodoItem>()
                }
                errorMessage = null
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Connection error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Logout failed")
            }
        }
    }
}