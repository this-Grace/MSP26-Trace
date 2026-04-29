package it.unibo.trace.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.unibo.trace.ui.composable.InputField
import it.unibo.trace.ui.composable.TopBar
import it.unibo.trace.ui.viewmodel.AddTodoViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun AddTodoScreen(
    navController: NavHostController,
    viewModel: AddTodoViewModel = viewModel()
) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
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

            InputField(
                value = viewModel.todoName,
                onValueChange = { viewModel.todoName = it },
                label = "Task Name",
                placeholder = "e.g. Buy milk"
            )

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.saveTodo {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isSaving && viewModel.todoName.isNotBlank()
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Task")
                }
            }
        }
    }
}
