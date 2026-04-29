package it.unibo.trace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.jan.supabase.auth.handleDeeplinks
import it.unibo.trace.data.supabase
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.theme.TraceTheme

class   MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supabase.handleDeeplinks(intent)
        setContent {
            TraceTheme {
                NavGraph()
            }
        }
    }
}
