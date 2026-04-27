package it.unibo.trace.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import it.unibo.trace.ui.components.TopBar

@Composable
fun DetailsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopBar(
                title = "Details",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Welcome to the details screen!")
        }
    }
}
