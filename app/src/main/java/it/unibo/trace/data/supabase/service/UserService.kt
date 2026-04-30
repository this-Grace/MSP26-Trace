package it.unibo.trace.data.supabase.service

import io.github.jan.supabase.postgrest.postgrest
import it.unibo.trace.data.supabase.entities.ProfileInfo
import it.unibo.trace.data.supabase.supabase
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.ZoneId
import kotlin.time.ExperimentalTime

/**
 * Service object handling user-related operations via Supabase.
 */
object UserService {
    /**
     * Deletes the current user's account by calling a remote stored procedure.
     * Note: This only deletes the database record/user via RPC, 
     * the session should be cleared separately.
     */
    suspend fun deleteAccount() {
        supabase.postgrest.rpc("delete_user")
    }

    /**
     * Retrieves and maps the current user's profile information.
     */
    @OptIn(ExperimentalTime::class)
    fun getProfileInfo(): ProfileInfo? {
        val user = AuthService.getCurrentUser() ?: return null
        
        val lastLoginDate = user.lastSignInAt?.let {
            java.time.Instant.ofEpochMilli(it.toEpochMilliseconds())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        return ProfileInfo(
            email = user.email ?: "Not available",
            loginType = user.appMetadata?.get("provider")?.jsonPrimitive?.contentOrNull
                ?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            lastLogin = lastLoginDate
        )
    }
}
