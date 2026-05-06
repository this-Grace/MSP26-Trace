package it.unibo.trace

import io.github.jan.supabase.SupabaseClient
import it.unibo.trace.data.ThemeRepository
import it.unibo.trace.data.supabase.service.AuthService
import it.unibo.trace.data.supabase.service.TodoService
import it.unibo.trace.data.supabase.service.UserService
import it.unibo.trace.data.supabase.supabase
import it.unibo.trace.ui.screen.todo.AddTodoViewModel
import it.unibo.trace.ui.screen.home.HomeViewModel
import it.unibo.trace.ui.MainViewModel
import it.unibo.trace.ui.screen.auth.forgotpassword.ForgotPasswordViewModel
import it.unibo.trace.ui.screen.auth.signin.SignInViewModel
import it.unibo.trace.ui.screen.auth.singup.SignUpViewModel
import it.unibo.trace.ui.screen.auth.resetpassword.ResetPasswordViewModel
import it.unibo.trace.ui.screen.profile.ProfileViewModel
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
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { ResetPasswordViewModel(get()) }
}