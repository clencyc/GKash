package com.example.g_kash.di

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.g_kash.accounts.data.AccountsApiService
import com.example.g_kash.accounts.domain.AccountsRepository
import com.example.g_kash.accounts.domain.AccountsRepositoryImpl
import com.example.g_kash.accounts.presentation.AccountsViewModel
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import com.example.g_kash.authentication.data.AuthRepositoryImpl
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.ConfirmPinViewModel
import com.example.g_kash.authentication.presentation.CreateAccountViewModel
import com.example.g_kash.authentication.presentation.CreatePinViewModel
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
private const val API_BASE_URL = "https://gkash.onrender.com/api"
private const val AUTH_TOKEN_PREFS_KEY = "auth_token"
val networkModule: Module = module {
    // Provide SharedPreferences for token retrieval
    single<SharedPreferences> {
        androidContext().getSharedPreferences("g_kash_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Provide a way to get the token (synchronously for HttpClient config)
    single<TokenProvider> {
        object : TokenProvider {
            override fun getToken(): String? {
                val token = get<SharedPreferences>().getString(AUTH_TOKEN_PREFS_KEY, null)
                if (token != null) {
                    Log.d("AuthFlow", "TokenProvider retrieved token from SharedPreferences: ${token.take(4)}...")

                } else {
                    Log.d("AuthFlow", "TokenProvider: No token found in sharedPreferences.")
                }
                // Retrieve token from SharedPreferences
                return token
            }
        }
    }

    single<HttpClient> {
        HttpClient(Android.create()) {
            install(Logging) {
                level = LogLevel.ALL // Set to ALL for debugging, INFO for production
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }

            // Install Auth feature to handle Bearer tokens
            install(Auth) {
                bearer {
                    // This block is called to get the token when an authorized request is made
                    loadTokens {
                        val token = get<TokenProvider>().getToken()
                        if (token != null) {
                            BearerTokens(token, "") // Access token, refresh token (if applicable, leave empty for now)
                        } else {
                            BearerTokens("", "") // No tokens found
                        }
                    }
                }
            }

            defaultRequest {
                url(API_BASE_URL) // Base URL for all requests
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                // Authorization header is now handled by the Auth feature above
            }

            engine {
                // Configure engine if needed
            }
        }
    }

    single<ApiService> { ApiServiceImpl(get()) } // HttpClient is injected via get()
}

// Helper interface to provide token access
interface TokenProvider {
    fun getToken(): String?
}

// You would then combine this with your appModule
// fun getAllModules() = listOf(networkModule, appModule)
// --- Define your app module ---
val appModule = module {
    // ViewModels
    viewModel { CreateAccountViewModel(get()) }
    viewModel { CreatePinViewModel(get()) }
    viewModel { ConfirmPinViewModel(get()) }
    viewModel { AuthViewModel(get()) } // Depends on AuthRepository

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<SharedPreferences> {
        androidContext().getSharedPreferences("g_kash_prefs", android.content.Context.MODE_PRIVATE)
    }

    // Accounts specific definitions
    single {
        AccountsApiService(
            client = get(),
            baseUrl = API_BASE_URL // Use the same base URL or a specific one if needed
        )
    }
    single<AccountsRepository> { AccountsRepositoryImpl(apiService = get()) }
    viewModel { (userId: String) ->
        AccountsViewModel(repository = get(), userId = userId)
    }
}

fun getAllModules() = listOf(
    networkModule,
    appModule
)