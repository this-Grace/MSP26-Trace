package it.unibo.trace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.ui.theme.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    themeRepository: ThemeRepository,
    supabase: SupabaseClient
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
}
