package it.unibo.trace

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.jan.supabase.auth.handleDeeplinks
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import it.unibo.trace.ui.viewmodel.MainViewModel

/**
 * The main entry point of the application.
 *
 * This activity is responsible for:
 * - Handling Supabase authentication deep links.
 * - Initializing the Compose UI content.
 * - Managing the global application theme via [MainViewModel].
 */
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleRecoveryIntent(intent)
        supabase.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            val theme by mainViewModel.theme.collectAsState()

            TraceTheme(appTheme = theme) {
                NavGraph(mainViewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRecoveryIntent(intent)
        supabase.handleDeeplinks(intent)
    }

    private fun handleRecoveryIntent(intent: Intent) {
        val dataString = intent.dataString ?: return
        if (dataString.contains("type=recovery", ignoreCase = true)) {
            mainViewModel.setPendingReset(true)
        }
    }
}
