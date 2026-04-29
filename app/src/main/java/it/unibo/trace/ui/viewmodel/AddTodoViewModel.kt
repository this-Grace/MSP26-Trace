package it.unibo.trace.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.data.supabase.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AddTodoViewModel : ViewModel() {
    var todoName by mutableStateOf("")
    var isSaving by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    fun saveTodo(onSuccess: () -> Unit) {
        if (todoName.isBlank()) {
            errorMessage = "Name cannot be empty"
            return
        }

        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) {
                    errorMessage = "User not logged in"
                    return@launch
                }

                val newTodo = TodoItem(
                    name = todoName.trim(),
                    uid = user.id
                )

                withContext(Dispatchers.IO) {
                    TodoService.insertTodo(newTodo)
                }
                _events.emit("Task created successfully!")
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Failed to save todo"
                _events.emit("Error: ${e.localizedMessage}")
            } finally {
                isSaving = false
            }
        }
    }
}
