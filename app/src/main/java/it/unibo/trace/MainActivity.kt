package it.unibo.trace

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import org.koin.android.ext.android.inject
import it.unibo.trace.ui.viewmodel.MainViewModel
import it.unibo.trace.utils.MessageDuration
import it.unibo.trace.utils.UiMessenger
import it.unibo.trace.data.supabase.supabase
import io.github.jan.supabase.auth.handleDeeplinks

/**
 * The main entry point of the application.
 *
 * This activity is responsible for:
 * - Handling Supabase authentication deep links.
 * - Initializing the Compose UI content.
 * - Managing the global application theme via [MainViewModel].
 */
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabase.handleDeeplinks(intent)

        enableEdgeToEdge()
        setContent {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabase.handleDeeplinks(intent)
    }
}
