package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.data.supabase.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    var items = mutableStateListOf<TodoItem>()
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val deletionJobs = mutableMapOf<Long, Job>()
    var pendingDeletion = mutableStateListOf<Long>()
        private set

    init {
        fetchTodos()
    }

    fun fetchTodos() {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) {
                    errorMessage = "User not authenticated"
                    return@launch
                }

                val list = withContext(Dispatchers.IO) {
                    TodoService.getTodos(user.id)
                }
                items.clear()
                items.addAll(list)
                errorMessage = null
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error: ${e.localizedMessage}"
                _events.emit("Failed to fetch: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleTodo(todoId: Long) {
        if (pendingDeletion.contains(todoId)) return

        pendingDeletion.add(todoId)
        
        val job = viewModelScope.launch {
            delay(2000)
            deleteTodo(todoId)
        }
        deletionJobs[todoId] = job
    }

    private suspend fun deleteTodo(todoId: Long) {
        try {
            withContext(Dispatchers.IO) {
                TodoService.deleteTodo(todoId)
            }
            withContext(Dispatchers.Main) {
                items.removeAll { it.id == todoId }
                pendingDeletion.remove(todoId)
                deletionJobs.remove(todoId)
                _events.emit("Task completed!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                pendingDeletion.remove(todoId)
                _events.emit("Delete failed: ${e.localizedMessage}")
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
