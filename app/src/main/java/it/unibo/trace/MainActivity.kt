package it.unibo.trace

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import io.github.jan.supabase.auth.handleDeeplinks
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import it.unibo.trace.ui.viewmodel.MainViewModel
import it.unibo.trace.utils.MessageDuration
import it.unibo.trace.utils.UiMessenger

/**
 * The main entry point of the application.
 *
 * This activity is responsible for:
 * - Handling Supabase authentication deep links.
 * - Initializing the Compose UI content.
 * - Managing the global application theme via [MainViewModel].
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val theme by mainViewModel.theme.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                UiMessenger.messages.collect { message ->
                    val duration = if (message.duration == MessageDuration.LONG)
                        Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                    Toast.makeText(context, message.text, duration).show()
                }
            }

            TraceTheme(appTheme = theme) {
                NavGraph()
            }
        }
    }
}
