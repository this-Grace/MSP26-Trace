package it.unibo.trace.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.jan.supabase.postgrest.from
import it.unibo.trace.data.TodoItem
import it.unibo.trace.supabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavHostController) {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                items = supabase.from("Todos")
                    .select().decodeList<TodoItem>()
                errorMessage = null
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Connection error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items,
                        key = { item -> item.id },
                    ) { item ->
                        Text(
                            item.name,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}
