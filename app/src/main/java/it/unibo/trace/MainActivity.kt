package it.unibo.trace

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import org.koin.android.ext.android.inject
import androidx.compose.material3.SnackbarResult
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
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                UiMessenger.messages.collect { message ->
                    val result = snackbarHostState.showSnackbar(
                        message = message.text,
                        actionLabel = message.actionLabel,
                        duration = message.duration
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        message.onAction?.invoke()
                    }
                }
            }

            TraceTheme(appTheme = theme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .imePadding()
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.handleAuthCallback(intent)
    }
}
