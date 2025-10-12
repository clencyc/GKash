package com.example.g_kash.di


import android.util.Log
import com.example.g_kash.accounts.domain.AccountsRepository
import com.example.g_kash.accounts.domain.AccountsRepositoryImpl
import com.example.g_kash.accounts.presentation.AccountsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import com.example.g_kash.authentication.data.ApiService
import com.example.g_kash.authentication.data.ApiServiceImpl
import io.ktor.client.*
import com.example.g_kash.data.SessionStorage
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.authentication.data.AuthRepositoryImpl
import com.example.g_kash.authentication.data.createHttpClient
import com.example.g_kash.authentication.domain.CreateAccountUseCase
import com.example.g_kash.authentication.domain.CreatePinUseCase
import com.example.g_kash.authentication.domain.LoginUseCase
import com.example.g_kash.authentication.domain.RegisterWithIdUseCase
import com.example.g_kash.authentication.domain.AddPhoneUseCase
import com.example.g_kash.authentication.domain.CreatePinKycUseCase
import com.example.g_kash.authentication.domain.LoginWithNationalIdUseCase
import com.example.g_kash.authentication.presentation.AuthViewModel
import com.example.g_kash.authentication.presentation.CreateAccountViewModel
import com.example.g_kash.authentication.presentation.CreatePinViewModel
import com.example.g_kash.authentication.presentation.UserViewModel
import com.example.g_kash.authentication.presentation.KycViewModel
import com.example.g_kash.core.presentation.FinancialLearningViewModel
import com.example.g_kash.core.data.AlphaVantageApiService
import com.example.g_kash.core.data.AlphaVantageApiServiceImpl
import com.example.g_kash.core.domain.FinancialLearningRepository
import com.example.g_kash.core.domain.FinancialLearningRepositoryImpl
import com.example.g_kash.chat.presentation.ChatViewModel
import com.example.g_kash.chat.data.ChatBotApiService
import com.example.g_kash.chat.data.ChatBotRepositoryImpl
import com.example.g_kash.chat.domain.ChatBotRepository
import com.example.g_kash.profile.presentation.ProfileViewModel
import com.example.g_kash.accounts.data.AccountsApiService
import com.example.g_kash.wallet.data.WalletRepository
import com.example.g_kash.wallet.data.WalletRepositoryImpl
import com.example.g_kash.wallet.presentation.WalletViewModel
import com.example.g_kash.points.domain.*
import com.example.g_kash.points.data.MockPointsRepository
import com.example.g_kash.points.presentation.PointsViewModel
import com.example.g_kash.investment.presentation.InvestmentAccountCreationViewModel
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val API_BASE_URL = "https://gkash.onrender.com/api"

val networkModule = module {

    single {
        // Provide SessionStorage from the androidContext
        SessionStorage(androidContext())
    }

    single<HttpClient> {
        val sessionStorage = get<SessionStorage>()

        HttpClient(Android) {
            expectSuccess = true // Will throw exceptions for non-2xx responses

            defaultRequest {
                url(API_BASE_URL)
                contentType(ContentType.Application.Json)
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
                })
            }

            // The magic happens here!
            install(Auth) {
                bearer {
                    loadTokens {
                        // Load the token from our DataStore
                        val token = sessionStorage.authTokenStream.first()
                        if (token != null) {
                            BearerTokens(accessToken = token, refreshToken = "")
                        } else {
                            null // No token found
                        }
                    }

                    refreshTokens {
                        // This block is triggered when a 401 is received.
                        Log.w("KtorAuth", "Received 401 Unauthorized. Token might be expired or invalid.")
                        
                        try {
                            // Log current session state for debugging
                            sessionStorage.logCurrentSession("KtorAuth")
                            
                            // For now, we don't have a refresh token endpoint
                            // In a production app, you would:
                            // 1. Call refresh token API endpoint
                            // 2. If successful, return new BearerTokens
                            // 3. If failed, clear session and return null
                            
                            Log.w("KtorAuth", "No refresh token mechanism implemented. Clearing session.")
                            sessionStorage.clearSession()
                            
                        } catch (e: Exception) {
                            Log.e("KtorAuth", "Error during token refresh handling", e)
                            sessionStorage.clearSession()
                        }
                        
                        null // Indicates refresh failed, stopping the retry.
                    }
                }
            }
        }
    }

    // Separate HttpClient for Alpha Advantage API (no auth needed)
    single<HttpClient>(qualifier = named("alpha_vantage")) {
        HttpClient(Android) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("AlphaVantageClient", message)
                    }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<ApiService> { ApiServiceImpl(get()) }
    single<AlphaVantageApiService> { AlphaVantageApiServiceImpl(get(named("alpha_vantage"))) }
    single { AccountsApiService(get()) }
    single { ChatBotApiService(get()) }
    single<com.example.g_kash.otp.domain.OtpApiService> { com.example.g_kash.otp.data.OtpApiServiceImpl(get()) }
}

val appModule = module {
    // SINGLE SOURCE OF TRUTH FOR TOKEN STORAGE
    single { SessionStorage(androidContext()) }
    single { createHttpClient(sessionStorage = get()) }

    // AUTH REPOSITORY
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<AccountsRepository> { AccountsRepositoryImpl(get()) }
    single<WalletRepository> { WalletRepositoryImpl(get()) }
    single<FinancialLearningRepository> { FinancialLearningRepositoryImpl(get()) }
    single<ChatBotRepository> { ChatBotRepositoryImpl(get(), get()) }
    single<PointsRepository> { MockPointsRepository() }

    factory { CreateAccountUseCase(get()) }
    factory { CreatePinUseCase(get()) }
    factory { LoginUseCase(get()) }
    
    // KYC Use Cases
    factory { RegisterWithIdUseCase(get()) }
    factory { AddPhoneUseCase(get()) }
    factory { CreatePinKycUseCase(get()) }
    factory { LoginWithNationalIdUseCase(get()) }
    
    // OTP Use Cases
    factory { com.example.g_kash.otp.domain.SendOtpUseCase(get()) }
    factory { com.example.g_kash.otp.domain.VerifyOtpUseCase(get()) }
    
    // Points system use cases
    factory { GetUserPointsUseCase(get()) }
    factory { CompleteModuleUseCase(get()) }
    factory { PurchaseStockUseCase(get()) }
    factory { GetAvailableStocksUseCase(get()) }
    factory { GetLearningProgressUseCase(get()) }
    factory { GetPointsHistoryUseCase(get()) }
    factory { GetUserStockPurchasesUseCase(get()) }

    // YOUR VIEWMODELS
    viewModel { AuthViewModel(get()) }
    viewModel { UserViewModel(get()) }
    viewModel { CreateAccountViewModel(get()) }
    viewModel { CreatePinViewModel(get()) }
    viewModel { AccountsViewModel(get()) }
    viewModel { FinancialLearningViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ProfileViewModel() }
    viewModel { PointsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    
    // KYC ViewModel
    viewModel { KycViewModel(get(), get(), get(), get(), get(), get()) }

    // Investment ViewModel
    viewModel { InvestmentAccountCreationViewModel(get()) }

    // The definition for WalletViewModel should also be here
    viewModel { params -> WalletViewModel(walletRepository = get(), userId = params.get()) }
}