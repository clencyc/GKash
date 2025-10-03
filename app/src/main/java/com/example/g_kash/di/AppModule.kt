// In com.example.g_kash.di.AppModule.kt

package com.example.g_kash.di

import android.content.SharedPreferences
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import com.example.g_kash.authentication.data.AuthRepositoryImpl
import com.example.g_kash.authentication.data.createHttpClient // Assuming this function is in your DI package too
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.ConfirmPinViewModel
import com.example.g_kash.authentication.presentation.CreateAccountViewModel
import com.example.g_kash.authentication.presentation.CreatePinViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    viewModel { CreateAccountViewModel(get()) } // Depends on CreateAccountUseCase
    viewModel { CreatePinViewModel(get()) }     // Depends on CreatePinUseCase
    viewModel { ConfirmPinViewModel(get()) }    // Depends on CreatePinUseCase
    // AuthViewModel depends on AuthRepository
    viewModel { AuthViewModel(get()) }

    // Use Cases (assuming they are defined elsewhere and inject AuthRepository)
    // single { CreateAccountUseCase(get()) }
    // single { CreatePinUseCase(get()) }

    // Repositories
    // AuthRepositoryImpl depends on ApiService and SharedPreferences
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // API Service
    // ApiServiceImpl depends on HttpClient
    single<ApiService> { ApiServiceImpl(get()) }

    // Shared Preferences
    single<SharedPreferences> {
        androidContext().getSharedPreferences("g_kash_prefs", android.content.Context.MODE_PRIVATE)
    }
}

