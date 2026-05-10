package it.unibo.trace.ui.screen.home.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Todo Detail screen.
 */
data class TodoDetailUiState(
    val todo: TodoItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for displaying the details of a specific Todo item.
 */
class TodoDetailViewModel(
    private val todoService: TodoService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoDetailUiState())
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    /**
     * Fetches the todo item by its ID.
     *
     * @param id The ID of the todo to fetch.
     */
    fun fetchTodo(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val item = todoService.getTodoById(id)
                if (item != null) {
                    _uiState.update { it.copy(todo = item) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Task not found") }
                    UiMessenger.show("Task not found")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
                UiMessenger.show("Error fetching task: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
