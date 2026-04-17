package com.example.g_kash.di

import android.util.Log
import com.example.g_kash.accounts.data.AccountsApiService
import com.example.g_kash.accounts.domain.AccountsRepository
import com.example.g_kash.accounts.domain.AccountsRepositoryImpl
import com.example.g_kash.accounts.presentation.AccountsViewModel
import com.example.g_kash.transactions.presentation.TransactionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import io.ktor.client.*
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.transactions.data.TransactionRepositoryImpl
import com.example.g_kash.transactions.domain.TransactionRepository
import com.example.g_kash.data.SessionStorage
import com.example.g_kash.authentication.data.AuthRepositoryImpl
import com.example.g_kash.authentication.data.createHttpClient
import com.example.g_kash.authentication.domain.CreateAccountUseCase
import com.example.g_kash.authentication.domain.CreatePinUseCase
import com.example.g_kash.authentication.domain.LoginUseCase
import com.example.g_kash.authentication.domain.RegisterWithIdUseCase
import com.example.g_kash.authentication.domain.AddPhoneUseCase
import com.example.g_kash.authentication.domain.CreatePinKycUseCase
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.CreateAccountViewModel
import com.example.g_kash.authentication.presentation.CreatePinViewModel
import com.example.g_kash.authentication.presentation.UserViewModel
import com.example.g_kash.authentication.presentation.KycViewModel
import com.example.g_kash.core.data.AlphaVantageApiService
import com.example.g_kash.core.data.AlphaVantageApiServiceImpl
import com.example.g_kash.core.domain.FinancialLearningRepository
import com.example.g_kash.core.domain.FinancialLearningRepositoryImpl
import com.example.g_kash.core.presentation.FinancialLearningViewModel
import com.example.g_kash.chat.presentation.ChatViewModel
import com.example.g_kash.chat.data.ChatBotApiService
import com.example.g_kash.chat.data.ChatBotRepositoryImpl
import com.example.g_kash.chat.domain.ChatBotRepository
import com.example.g_kash.profile.presentation.ProfileViewModel
import com.example.g_kash.wallet.data.BalanceRepository
import com.example.g_kash.points.domain.*
import com.example.g_kash.points.data.MockPointsRepository
import com.example.g_kash.points.presentation.PointsViewModel
import com.example.g_kash.investment.presentation.InvestmentAccountCreationViewModel
import com.example.g_kash.investment.data.InvestmentRepository
import com.example.g_kash.investment.data.KtorInvestmentRepository
import com.example.g_kash.investment.presentation.InvestmentViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val API_BASE_URL = "https://gkash.onrender.com/api"

val networkModule = module {

    // 1. Define SessionStorage first, as others depend on it.
    single {
        SessionStorage(androidContext())
    }

    // 2. Define the main HttpClient, which depends on SessionStorage.
    single<HttpClient> {
        val sessionStorage = get<SessionStorage>() // Explicitly get SessionStorage

        HttpClient(Android) {
            expectSuccess = false // Don't throw exceptions on non-2xx responses

            defaultRequest {
                url(API_BASE_URL)
                contentType(ContentType.Application.Json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 60000 // 60 seconds max for requests (Render cold starts)
                connectTimeoutMillis = 20000 // 20 seconds to establish connection
                socketTimeoutMillis = 60000  // 60 seconds for socket operations
            }

            install(Logging) {
                level = LogLevel.ALL
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
                    encodeDefaults = true
                })
            }

            install(Auth) {
                bearer {
                    sendWithoutRequest { request ->
                        // Send bearer token with all requests
                        true
                    }
                    
                    loadTokens {
                        // Get token immediately without retries - no need to wait on registration calls
                        val token = sessionStorage.authTokenStream.first()
                        Log.d("KtorAuth", "loadTokens called - Token: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}")
                        if (token != null) {
                            Log.d("KtorAuth", "Providing Bearer token for request")
                            BearerTokens(accessToken = token, refreshToken = "")
                        } else {
                            Log.d("KtorAuth", "No token available - request will be sent without Authorization header")
                            null
                        }
                    }

                    refreshTokens {
                        // DO NOT clear session here — a 401 from a data-format error
                        // (wrong body, missing account_id, etc.) would otherwise wipe
                        // the user's stored token and force a surprise logout.
                        Log.w("KtorAuth", "Token refresh triggered (401). No refresh mechanism — returning null.")
                        null
                    }
                }
            }
        }
    }

    single<ApiService> {
        ApiServiceImpl(get<HttpClient>(), get<SessionStorage>())
    }


    // 4. Keep other specific ApiService definitions.
    single<AlphaVantageApiService> { AlphaVantageApiServiceImpl(get(named("alpha_vantage"))) }
    single { AccountsApiService(get(), get()) }
    single { ChatBotApiService(get()) }
    single<com.example.g_kash.otp.domain.OtpApiService> { com.example.g_kash.otp.data.OtpApiServiceImpl(get()) }
    factory { com.example.g_kash.otp.domain.SendOtpUseCase(get()) }
    factory { com.example.g_kash.otp.domain.VerifyOtpUseCase(get()) }

    // Separate HttpClient for Alpha Vantage API (no auth needed)
    single<HttpClient>(qualifier = named("alpha_vantage")) {
        HttpClient(Android) {
            install(Logging) {
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}

val appModule = module {

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<AccountsRepository> { AccountsRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<BalanceRepository> { BalanceRepository() }
    single<InvestmentRepository> { KtorInvestmentRepository(get(), get()) }
    single<FinancialLearningRepository> { FinancialLearningRepositoryImpl(get()) }
    single<ChatBotRepository> { ChatBotRepositoryImpl(get(), get()) }
    single<PointsRepository> { MockPointsRepository() }
    single<com.example.g_kash.profile.domain.ProfileRepository> {
        com.example.g_kash.profile.data.ProfileRepositoryImpl(apiService = get())
    }

    // Firebase Analytics
    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(androidContext()) }

    // USE CASES
    factory { CreateAccountUseCase(get()) }
    factory { CreatePinUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { RegisterWithIdUseCase(get()) }
    factory { AddPhoneUseCase(get()) }
    factory { CreatePinKycUseCase(get()) }

    // VIEWMODELS
    viewModel { AuthViewModel(get()) }
    viewModel { UserViewModel(get()) }
    viewModel { KycViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AccountsViewModel(get()) }
    viewModel { FinancialLearningViewModel(get()) }
    viewModel { InvestmentAccountCreationViewModel(get()) }
    viewModel { InvestmentViewModel(get(), get(), get(), get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { TransactionsViewModel(get(), get()) }
    viewModel { PointsViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
