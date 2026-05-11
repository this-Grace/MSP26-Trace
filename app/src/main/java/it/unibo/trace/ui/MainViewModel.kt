package it.unibo.trace.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.ui.theme.AppTheme
import androidx.compose.material3.SnackbarDuration
import it.unibo.trace.utils.UiMessenger
import it.unibo.trace.utils.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    themeRepository: ThemeRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    val theme: StateFlow<AppTheme> = themeRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    val sessionStatus: StateFlow<SessionStatus> = supabase.auth.sessionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStatus.Initializing
        )

    private val _navigationEvent = Channel<String>(Channel.BUFFERED)
    val navigationEvent: Flow<String> = _navigationEvent.receiveAsFlow()

    fun handleAuthCallback(intent: Intent) {
        val data = intent.data ?: return

        val hasError = data.getQueryParameter("error") != null
        if (hasError) {
            UiMessenger.show("Reset link expired. Please request a new one.", SnackbarDuration.Long)
            return
        }

        viewModelScope.launch {
            try {
                val code = data.getQueryParameter("code")
                val isRecovery = data.host == "reset-password"
                val isLoginCallback = data.host == "login-callback"

                if (code != null && (isRecovery || isLoginCallback)) {
                    supabase.auth.exchangeCodeForSession(code)

                    if (isRecovery) {
                        _navigationEvent.send("reset-password")
                    }
                }
            } catch (e: Exception) {
                UiMessenger.show(e.toUserMessage(), SnackbarDuration.Long)
            }
        }
    }
}
