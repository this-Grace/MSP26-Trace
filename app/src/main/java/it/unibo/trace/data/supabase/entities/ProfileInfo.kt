package it.unibo.trace.data.supabase.entities

import java.time.LocalDateTime

/**
 * Data class representing basic user profile information.
 */
data class ProfileInfo(
    val email: String,
    val avatarUrl: String? = null,
    val loginType: String,
    val lastLogin: LocalDateTime?
)
