package it.unibo.trace.utils

import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.HttpRequestException
import java.net.UnknownHostException

/**
 * Extension function to map common exceptions to user-friendly messages.
 */
fun Throwable.toUserMessage(): String {
    return when (this) {
        is UnknownHostException, is HttpRequestException -> {
            "Network error. Please check your internet connection."
        }
        is RestException -> {
            when (this.statusCode) {
                401 -> "Unauthorized. Please sign in again."
                403 -> "Forbidden action."
                404 -> "Resource not found."
                409 -> "Conflict occurred (e.g., item already exists)."
                else -> "Server error (${this.statusCode}). Please try again later."
            }
        }
        else -> {
            val msg = this.localizedMessage ?: "An unexpected error occurred."
            if (msg.contains("timeout", ignoreCase = true)) {
                "Connection timed out. Please try again."
            } else {
                msg
            }
        }
    }
}
