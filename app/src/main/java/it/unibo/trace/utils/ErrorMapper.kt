package it.unibo.trace.utils

import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.HttpRequestException
import it.unibo.trace.R
import java.net.UnknownHostException

/**
 * Extension function to map common exceptions to user-friendly message resource IDs.
 */
fun Throwable.toUserMessageResId(): Int {
    return when (this) {
        is UnknownHostException, is HttpRequestException -> {
            R.string.error_network_connection
        }
        is RestException -> {
            when (this.statusCode) {
                401 -> R.string.error_unauthorized
                403 -> R.string.error_forbidden
                404 -> R.string.error_not_found
                409 -> R.string.error_conflict
                else -> R.string.error_server
            }
        }
        else -> {
            val msg = this.localizedMessage ?: ""
            if (msg.contains("timeout", ignoreCase = true)) {
                R.string.error_timeout
            } else {
                R.string.error_unexpected
            }
        }
    }
}
