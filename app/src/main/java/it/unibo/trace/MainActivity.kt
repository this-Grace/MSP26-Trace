package it.unibo.trace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.jan.supabase.auth.handleDeeplinks
import it.unibo.trace.data.supabase
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import it.unibo.trace.ui.viewmodel.MainViewModel

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase.handleDeeplinks(intent)
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val theme by mainViewModel.theme.collectAsState()

            TraceTheme(appTheme = theme) {
                NavGraph()
            }
        }
    }
}
