package it.unibo.trace.data.supabase.service

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import it.unibo.trace.data.supabase.supabase

/**
 * Service object handling authentication logic via Supabase Auth.
 */
object AuthService {
    private val auth = supabase.auth

    /**
     * Signs in a user using email and password.
     */
    suspend fun signIn(email: String, pass: String) {
        auth.signInWith(Email) {
            this.email = email
            password = pass
        }
    }

    /**
     * Registers a new user with email and password.
     */
    suspend fun signUp(email: String, pass: String) {
        auth.signUpWith(Email, "it.unibo.trace://login-callback") {
            this.email = email
            password = pass
        }
    }

    /**
     * Initiates sign-in with GitHub OAuth.
     */
    suspend fun signInWithGithub() {
        auth.signInWith(Github)
    }

    /**
     * Signs out the current user.
     */
    suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Sends a password reset email to the user.
     */
    suspend fun sendResetPasswordEmail(email: String) {
        auth.resetPasswordForEmail(
            email,
            "it.unibo.trace://login-callback"
        )
    }

    /**
     * Updates the password for the currently authenticated user.
     */
    suspend fun updatePassword(newPassword: String) {
        auth.updateUser {
            password = newPassword
        }
    }

    /**
     * Returns the currently authenticated user, or null if no user is logged in.
     */
    fun getCurrentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }
}
