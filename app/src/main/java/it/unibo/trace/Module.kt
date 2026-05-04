package it.unibo.trace

import io.github.jan.supabase.SupabaseClient
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.data.supabase.service.UserService
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.viewmodel.AddTodoViewModel
import it.unibo.trace.ui.viewmodel.HomeViewModel
import it.unibo.trace.ui.viewmodel.MainViewModel
import it.unibo.trace.ui.viewmodel.auth.ForgotPasswordViewModel
import it.unibo.trace.ui.viewmodel.auth.LoginViewModel
import it.unibo.trace.ui.viewmodel.auth.RegistrationViewModel
import it.unibo.trace.ui.viewmodel.auth.ResetPasswordViewModel
import it.unibo.trace.ui.viewmodel.user.ProfileViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single<SupabaseClient> { supabase }
}

val dataModule = module {
    single { ThemeRepository(androidApplication()) }
    single { AuthService(get()) }
    single { TodoService(get()) }
    single { UserService(get(), get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { AddTodoViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { RegistrationViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
}