package it.unibo.trace.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import it.unibo.trace.BuildConfig

/**
 * Global Supabase client instance configured with Auth and Postgrest plugins.
 * Uses credentials provided in BuildConfig (SUPABASE_URL and SUPABASE_KEY).
 */
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
    install(Auth)
}
