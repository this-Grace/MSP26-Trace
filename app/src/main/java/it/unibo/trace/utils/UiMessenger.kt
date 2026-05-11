package it.unibo.trace.utils

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Centralized service to send UI messages (Snackbars) from ViewModels.
 *
 * This object uses a [Channel] to broadcast messages to the UI layer
 * without requiring a reference to the Android Context within the ViewModel.
 */
object UiMessenger {
    private val _messages = Channel<UserMessage>(Channel.BUFFERED)

    /**
     * A flow of [UserMessage] that should be collected by the UI (e.g., in MainActivity).
     */
    val messages: Flow<UserMessage> = _messages.receiveAsFlow()

    /**
     * Emits a new message to be displayed to the user.
     *
     * @param text The string content of the message.
     * @param duration The display duration. Defaults to [SnackbarDuration.Short].
     * @param actionLabel Optional label for the snackbar action button.
     * @param onAction Optional callback to be executed when the action button is clicked.
     */
    fun show(
        text: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _messages.trySend(UserMessage(text, duration, actionLabel, onAction))
    }
}

/**
 * Represents a message to be displayed in the UI.
 *
 * @property text The message content.
 * @property duration How long the message should be visible.
 * @property actionLabel Optional label for an action button.
 * @property onAction Optional callback for the action button.
 */
data class UserMessage(
    val text: String,
    val duration: SnackbarDuration,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)
