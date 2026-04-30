package it.unibo.trace.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.TodoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the AddTodo screen.
 */
data class AddTodoUiState(
    val todoName: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/**
 * One-time events for the AddTodo screen.
 */
sealed class AddTodoEvent {
    data object SaveSuccess : AddTodoEvent()
    data class ShowMessage(val message: String) : AddTodoEvent()
}

/**
 * ViewModel for handling the creation of new Todo tasks.
 */
class AddTodoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddTodoUiState())
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTodoEvent>()
    val events = _events.asSharedFlow()

    fun updateTodoName(name: String) {
        _uiState.update { it.copy(todoName = name, errorMessage = null) }
    }

    /**
     * Saves a new Todo item to the database.
     */
    fun saveTodo() {
        val name = _uiState.value.todoName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val user = AuthService.getCurrentUser()
                if (user == null) {
                    _uiState.update { it.copy(errorMessage = "User not logged in", isSaving = false) }
                    return@launch
                }

                val newTodo = TodoItem(
                    name = name,
                    uid = user.id
                )

                withContext(Dispatchers.IO) {
                    TodoService.insertTodo(newTodo)
                }
                _events.emit(AddTodoEvent.ShowMessage("Task created successfully!"))
                _events.emit(AddTodoEvent.SaveSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.localizedMessage ?: "Failed to save todo"
                _uiState.update { it.copy(errorMessage = msg) }
                _events.emit(AddTodoEvent.ShowMessage("Error: $msg"))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
