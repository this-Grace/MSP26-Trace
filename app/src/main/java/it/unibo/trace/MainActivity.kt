package it.unibo.trace

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.postgrest.Postgrest
import it.unibo.trace.ui.NavGraph
import it.unibo.trace.ui.Route
import it.unibo.trace.ui.theme.TraceTheme

val Context.dataStore by preferencesDataStore(name = "supabase_session")

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
    install(Auth)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep links for OAuth redirect
        supabase.handleDeeplinks(intent)

        setContent {
            TraceTheme {
                val navController = rememberNavController()
                
                // Determine start destination based on session status
                val startDestination = if (supabase.auth.currentSessionOrNull() != null) {
                    Route.Home
                } else {
                    Route.Login
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}
