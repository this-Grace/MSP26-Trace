package it.unibo.trace.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.composable.TopBar
import it.unibo.trace.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBar(
                title = "Home",
                onProfileClick = { navController.navigate(Route.Profile) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    viewModel.logout(
                        onSuccess = {
                            Toast.makeText(context, "Logout successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(Route.Login) {
                                popUpTo(Route.Home) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Toast.makeText(context, "Logout failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            }
        }
    ) { innerPadding ->
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Text(
                    text = viewModel.errorMessage!!,
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                    color = MaterialTheme.colorScheme.error
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
                        viewModel.items,
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