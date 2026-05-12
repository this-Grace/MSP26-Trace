package it.unibo.trace.ui.screen.home.task.add

import androidx.compose.ui.res.stringResource
import it.unibo.trace.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.unibo.trace.ui.composable.TraceTopBar
import it.unibo.trace.ui.composable.button.TraceButton
import it.unibo.trace.ui.composable.input.TraceTextField
import org.koin.androidx.compose.koinViewModel

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
                title = stringResource(R.string.add_task_title),
                onNavigateBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                TraceButton(
                    text = stringResource(R.string.create_task_button),
                    isLoading = uiState.isSaving,
                    onClick = {
                        viewModel.saveTodo(onSuccess = {
                            navController.popBackStack()
                        })
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddTask,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.add_task_headline),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.add_task_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TraceTextField(
                label = stringResource(R.string.task_name_label),
                value = uiState.todoName,
                onValueChange = { viewModel.updateTodoName(it) },
                placeholder = stringResource(R.string.task_name_placeholder),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            TraceTextField(
                label = stringResource(R.string.task_description_label),
                value = uiState.todoDescription,
                onValueChange = { viewModel.updateTodoDescription(it) },
                placeholder = stringResource(R.string.task_description_placeholder),
                singleLine = false,
                minLines = 4,
            )
        }
    }
}
