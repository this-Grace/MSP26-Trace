package it.unibo.trace.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Centralized service to send UI messages (Toasts, Snackbars) from ViewModels.
 *
 * This object uses a [MutableSharedFlow] to broadcast messages to the UI layer
 * without requiring a reference to the Android Context within the ViewModel,
 * ensuring a clean separation of concerns and improved testability.
 */
object UiMessenger {
    private val _messages = MutableSharedFlow<UserMessage>(extraBufferCapacity = 1)

    /**
     * A flow of [UserMessage] that should be collected by the UI (e.g., in MainActivity).
     */
    val messages = _messages.asSharedFlow()

    /**
     * Emits a new message to be displayed to the user.
     *
     * @param text The string content of the message.
     * @param duration The display duration (SHORT or LONG). Defaults to [MessageDuration.SHORT].
     */
    fun show(text: String, duration: MessageDuration = MessageDuration.SHORT) {
        _messages.tryEmit(UserMessage(text, duration))
    }
}

/**
 * Represents a message to be displayed in the UI.
 *
 * @property text The message content.
 * @property duration How long the message should be visible.
 */
data class UserMessage(val text: String, val duration: MessageDuration)

/**
 * Defines the duration for which a UI message is displayed.
 */
enum class MessageDuration { SHORT, LONG }
