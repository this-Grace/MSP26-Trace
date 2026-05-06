package it.unibo.trace.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.unibo.trace.data.supabase.entities.TodoItem
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.utils.UiMessenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the Home screen.
 */
data class HomeUiState(
    val items: List<TodoItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pendingDeletion: Set<Long> = emptySet()
)

/**
 * ViewModel for managing the main Todo list and task completion logic.
 */
class HomeViewModel(
    private val authService: AuthService,
    private val todoService: TodoService
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val deletionJobs = mutableMapOf<Long, Job>()

    /**
     * Fetches todos belonging to the current authenticated user.
     */
    fun fetchTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authService.getCurrentUser()
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    UiMessenger.show("User not authenticated")
                    return@launch
                }

                val list = withContext(Dispatchers.IO) {
                    todoService.getTodos(user.id)
                }
                _uiState.update { it.copy(items = list, errorMessage = null) }
            } catch (e: Exception) {
                UiMessenger.show("Failed to fetch tasks: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Toggles a todo item's status, triggering a delayed deletion (completion).
     *
     * @param todoId The unique ID of the todo item.
     */
    fun toggleTodo(todoId: Long) {
        if (_uiState.value.pendingDeletion.contains(todoId)) return

        _uiState.update { it.copy(pendingDeletion = it.pendingDeletion + todoId) }

        val job = viewModelScope.launch {
            delay(2000)
            deleteTodo(todoId)
        }
        deletionJobs[todoId] = job
    }

    private suspend fun deleteTodo(todoId: Long) {
        try {
            withContext(Dispatchers.IO) {
                todoService.deleteTodo(todoId)
            }
            _uiState.update { state ->
                state.copy(
                    items = state.items.filter { it.id != todoId },
                    pendingDeletion = state.pendingDeletion - todoId
                )
            }
            deletionJobs.remove(todoId)
            UiMessenger.show("Task completed!")
        } catch (e: Exception) {
            _uiState.update { it.copy(pendingDeletion = it.pendingDeletion - todoId) }
            UiMessenger.show("Error: ${e.localizedMessage}")
        }
    }
}
