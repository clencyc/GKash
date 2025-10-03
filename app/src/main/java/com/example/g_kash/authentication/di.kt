package com.example.g_kash.authentication


import android.content.Context
import android.content.SharedPreferences
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import com.example.g_kash.authentication.data.AuthRepositoryImpl
import com.example.g_kash.authentication.data.createHttpClient
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.authentication.domain.CreateAccountUseCase
import com.example.g_kash.authentication.domain.CreatePinUseCase
import com.example.g_kash.authentication.domain.LoginUseCase
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.ConfirmPinViewModel
import com.example.g_kash.authentication.presentation.CreateAccountViewModel
import com.example.g_kash.authentication.presentation.CreatePinViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // SharedPreferences
    single<SharedPreferences> {
        androidContext().getSharedPreferences("gkash_prefs", Context.MODE_PRIVATE)
    }

    // HTTP Client
    single { createHttpClient() }

    // API Service
    single<ApiService> { ApiServiceImpl(get()) }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // Use Cases
    single { CreateAccountUseCase(get()) }
    single { CreatePinUseCase(get()) }
    single { LoginUseCase(get()) }

    // ViewModels
    viewModel { CreateAccountViewModel(get()) }
    viewModel { CreatePinViewModel(get()) }
    viewModel { ConfirmPinViewModel(get()) }
    viewModel { AuthViewModel(get()) }
}