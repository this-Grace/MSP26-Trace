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
import it.unibo.trace.ui.MainViewModel
import it.unibo.trace.utils.UiMessenger

/**
 * The main entry point of the application.
 */
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.handleAuthCallback(intent)

        enableEdgeToEdge()
        setContent {
            val theme by mainViewModel.theme.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                UiMessenger.messages.collect { message ->
                    Toast.makeText(context, message.text, message.duration).show()
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
        mainViewModel.handleAuthCallback(intent)
    }
}
