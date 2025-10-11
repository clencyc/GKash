package com.example.g_kash.authentication.data

import android.util.Log
import com.example.g_kash.accounts.data.Account
import com.example.g_kash.accounts.data.AccountsApiResponse
import com.example.g_kash.accounts.data.TotalBalanceResponse
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.data.SessionStorage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

// API Service
interface ApiService {
    suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse
    suspend fun createPin(request: CreatePinRequest): CreatePinResponse
    suspend fun login(request: LoginRequest): LoginResponse

    suspend fun getAccounts(): AccountsApiResponse
    suspend fun getTotalBalance(): TotalBalanceResponse
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    private val baseUrl = "https://gkash.onrender.com/api"

    override suspend fun registerUser(request: RegisterUserRequest): RegisterUserResponse {
        return client.post("$baseUrl/auth/register-user") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun createPin(request: CreatePinRequest): CreatePinResponse {
        return client.post("$baseUrl/auth/create-pin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        // FIX: Changed 'httpClient' to 'client'
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getAccounts(): AccountsApiResponse {
        return try {
            client.get("$baseUrl/accounts").body<AccountsApiResponse>()
        } catch (e: Exception) {
            // FIX: Added parentheses to emptyList()
            AccountsApiResponse(success = false, accounts = emptyList<Account>(), message = e.message)
        }
    }

    override suspend fun getTotalBalance(): TotalBalanceResponse {
        return try {
            client.get("$baseUrl/wallet/balance").body<TotalBalanceResponse>()
        } catch (e: Exception) {
            TotalBalanceResponse(success = false, totalBalance = 0.0, message = e.message)
        }
    }
}
class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sessionStorage: SessionStorage
) : AuthRepository {

    override fun getAuthTokenStream(): Flow<String?> {
        // Simply return the flow from our session storage. This is now the single source of truth.
        return sessionStorage.authTokenStream
    }

    override suspend fun registerUser(
        name: String,
        phoneNumber: String,
        idNumber: String
    ): Result<RegisterUserResponse> {
        return try {
            val request = RegisterUserRequest(name, phoneNumber, idNumber)
            val response = apiService.registerUser(request)
            
            // Save temp token if provided by the API
            sessionStorage.saveAuthToken(response.temp_token)
            Log.d("AuthFlow", "Temp token saved after user registration")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthFlow", "Register user error", e)
            Result.failure(e)
        }
    }

    override suspend fun createPin(userId: String, pin: String): Result<CreatePinResponse> {
        return try {
            val request = CreatePinRequest(pin)
            val response = apiService.createPin(request)
            
            // Save token and user data if provided by the API
            sessionStorage.saveSession(token = response.token, userId = response.user.user_nationalId)
            Log.d("AuthFlow", "Token saved after PIN creation")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e("AuthFlow", "Create PIN error", e)
            Result.failure(e)
        }
    }

    override suspend fun login(phoneNumber: String, pin: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(phoneNumber, pin)
            val response = apiService.login(request)

            // Use .let to execute a block of code only if token and user are not null
            if (response.success) {
                val token = response.token
                response.user?.let { user ->
                    token?.let {
                        sessionStorage.saveSession(token = it, userId = user.user_nationalId)
                        Log.d("AuthFlow", "Session saved (token & user ID).")
                        return Result.success(response)
                    }
                }
            }

            Result.failure(Exception(response.message ?: "Login failed: Incomplete data received"))

        } catch (e: Exception) {
            Log.e("AuthFlow", "Login exception", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        // On logout, clear all session data (token and user ID).
        // The UI will react automatically because it's observing the token flow.
        sessionStorage.clearSession()
        Log.d("AuthFlow", "User logged out, session cleared.")
    }
}


fun createHttpClient(sessionStorage: SessionStorage): HttpClient {
    return HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        // --- FIX: INSTALL THE AUTH PLUGIN ---
        install(Auth) {
            bearer {
                // This block tells Ktor how to get the token for every request
                loadTokens {
                    // Read the token from our DataStore Flow
                    val token = sessionStorage.authTokenStream.first()
                    Log.d("AuthFlow", "Loading token for request: ${if (token != null) "Token found (${token.take(10)}...)" else "No token"}")
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
                
                refreshTokens {
                    // This block is triggered when a 401 is received.
                    Log.d("AuthFlow", "Received 401. Token might be expired or invalid. Clearing session.")
                    // For now, if refresh fails (or isn't implemented), we clear the session.
                    sessionStorage.clearSession()
                    null // Indicates refresh failed, stopping the retry.
                }
            }
        }
    }
}
