package it.unibo.trace.ui.screen.home.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.navigation.NavHostController
import it.unibo.trace.ui.composable.TraceTopBar
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.input.TraceTextField

/**
 * Screen for adding a new task to the todo list.
 */
@Composable
fun AddTodoScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AddTodoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TraceTopBar(
                title = "New Task",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What needs to be done?",
                style = MaterialTheme.typography.headlineSmall
            )

            TraceTextField(
                label = "Task Name",
                value = uiState.todoName,
                onValueChange = { viewModel.updateTodoName(it) },
                placeholder = "e.g. Buy milk"
            )

            TraceTextField(
                label = "Description (Optional)",
                value = uiState.todoDescription,
                onValueChange = { viewModel.updateTodoDescription(it) },
                placeholder = "Add more details here...",
                modifier = Modifier.weight(1f, fill = false)
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TraceButton(
                text = "Add",
                isLoading = uiState.isSaving,
                onClick = {
                    viewModel.saveTodo(onSuccess = {
                        navController.popBackStack()
                    })
                }
            )
        }
    }
}
