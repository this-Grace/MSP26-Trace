package it.unibo.trace.ui.screen.home.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the AddTodo screen.
 */
data class AddTodoUiState(
    val todoName: String = "",
    val todoDescription: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for handling the creation of new Todo tasks.
 */
class AddTodoViewModel(
    private val authService: AuthService,
    private val todoService: TodoService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddTodoUiState())
    val uiState: StateFlow<AddTodoUiState> = _uiState.asStateFlow()

    fun updateTodoName(name: String) {
        _uiState.update { it.copy(todoName = name, errorMessage = null) }
    }

    fun updateTodoDescription(description: String) {
        _uiState.update { it.copy(todoDescription = description) }
    }

    /**
     * Saves a new Todo item to the database.
     */
    fun saveTodo(onSuccess: () -> Unit) {
        val name = _uiState.value.todoName.trim()
        val description = _uiState.value.todoDescription.trim().ifBlank { null }
        
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val user = authService.getCurrentUser()
                if (user == null) {
                    _uiState.update { it.copy(isSaving = false) }
                    UiMessenger.show("User not logged in")
                    return@launch
                }

                val newTodo = TodoItem(
                    name = name,
                    description = description,
                    uid = user.id
                )

                withContext(Dispatchers.IO) {
                    todoService.insertTodo(newTodo)
                }

                UiMessenger.show("Task created successfully!")
                onSuccess()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Failed to save todo"
                _uiState.update { it.copy(errorMessage = msg) }
                UiMessenger.show("Error: $msg")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
