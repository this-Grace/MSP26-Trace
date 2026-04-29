package it.unibo.trace.ui.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
 * One-time events for the Home screen.
 */
sealed class HomeEvent {
    data class ShowMessage(val message: String) : HomeEvent()
}

/**
 * ViewModel for managing the main Todo list and task completion logic.
 */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private val deletionJobs = mutableMapOf<Long, Job>()

    init {
        fetchTodos()
    }

    /**
     * Fetches todos belonging to the current authenticated user.
     */
    fun fetchTodos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) {
                    _uiState.update { it.copy(errorMessage = "User not authenticated", isLoading = false) }
                    return@launch
                }

                val list = withContext(Dispatchers.IO) {
                    TodoService.getTodos(user.id)
                }
                _uiState.update { it.copy(items = list, errorMessage = null) }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = "Error: ${e.localizedMessage}"
                _uiState.update { it.copy(errorMessage = errorMsg) }
                _events.emit(HomeEvent.ShowMessage("Failed to fetch tasks"))
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
                TodoService.deleteTodo(todoId)
            }
            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        items = state.items.filter { it.id != todoId },
                        pendingDeletion = state.pendingDeletion - todoId
                    )
                }
                deletionJobs.remove(todoId)
                _events.emit(HomeEvent.ShowMessage("Task completed!"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(pendingDeletion = it.pendingDeletion - todoId) }
                _events.emit(HomeEvent.ShowMessage("Delete failed: ${e.localizedMessage}"))
            }
        }
    }

    /**
     * Signs out the current user.
     */
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
