package it.unibo.trace.data.supabase.service

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import it.unibo.trace.data.supabase.entities.ProfileInfo
import it.unibo.trace.utils.UiMessenger
import it.unibo.trace.utils.toUserMessage
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.ZoneId
import kotlin.time.ExperimentalTime

/**
 * Service class handling user-related operations via Supabase.
 */
class UserService(private val authService: AuthService, private val supabase: SupabaseClient) {
    /**
     * Deletes the current user's account by calling a remote stored procedure.
     * Note: This only deletes the database record/user via RPC, 
     * the session should be cleared separately.
     */
    suspend fun deleteAccount() {
        supabase.postgrest.rpc("delete_user")
    }

    /**
     * Uploads the user's avatar image to Supabase storage.
     * @param userId The ID of the user.
     * @param bytes The image data in bytes.
     * @return The public URL of the uploaded avatar.
     */
    suspend fun uploadAvatar(userId: String, bytes: ByteArray): String {
        val bucket = supabase.storage.from("avatars")

        val fileName = "$userId/avatar.png"

        bucket.upload(fileName, bytes) {
            upsert = true
            contentType = io.ktor.http.ContentType.Image.PNG
        }

        return bucket.publicUrl(fileName)
    }

    /**
     * Updates the avatar URL in the user's profile database record.
     * @param url The new avatar URL to store.
     */
    suspend fun updateAvatarUrl(url: String) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        val data = mapOf(
            "id" to userId,
            "avatar_url" to url
        )
        supabase.from("Profiles").upsert(data)
    }

    /**
     * Deletes the user's avatar image from storage and clears the URL in the database.
     * @param userId The ID of the user.
     */
    suspend fun deleteAvatar(userId: String) {
        val path = "$userId/avatar.png"
        supabase.storage.from("avatars").delete(path)
        supabase.from("Profiles").update(
            mapOf("avatar_url" to null)
        ) {
            filter { eq("id", userId) }
        }
    }

    /**
     * Retrieves and maps the current user's profile information.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun getProfileInfo(): ProfileInfo? {
        val user = authService.getCurrentUser() ?: return null
        val userId = user.id

        val avatarFromDb = try {
            val result = supabase.from("Profiles")
                .select {
                    filter { eq("id", userId) }
                }

            if (result.data != "[]" && result.data.isNotEmpty()) {
                val profile = result.decodeSingleOrNull<Map<String, String>>()
                profile?.get("avatar_url")
            } else {
                null
            }
        } catch (e: Exception) {
            UiMessenger.show(e.toUserMessage())
            null
        }

        val lastLoginDate = user.lastSignInAt?.let {
            java.time.Instant.ofEpochMilli(it.toEpochMilliseconds())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        return ProfileInfo(
            email = user.email ?: "Not available",
            avatarUrl = avatarFromDb,
            loginType = user.appMetadata?.get("provider")?.jsonPrimitive?.contentOrNull
                ?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            lastLogin = lastLoginDate
        )
    }
}
