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
    suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse
    suspend fun createPin(request: CreatePinRequest): CreatePinResponse
    suspend fun login(request: LoginRequest): LoginResponse

    suspend fun getAccounts(): AccountsApiResponse
    suspend fun getTotalBalance(): TotalBalanceResponse
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    private val baseUrl = "https://gkash.onrender.com/api"

    override suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse {
        // FIX: Changed 'httpClient' to 'client'
        return client.post("$baseUrl/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun createPin(request: CreatePinRequest): CreatePinResponse {
        // FIX: Changed 'httpClient' to 'client'
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
            client.get("/accounts/my-accounts").body<AccountsApiResponse>()
        } catch (e: Exception) {
            // FIX: Added parentheses to emptyList()
            AccountsApiResponse(success = false, accounts = emptyList<Account>(), message = e.message)
        }
    }

    override suspend fun getTotalBalance(): TotalBalanceResponse {
        return try {
            client.get("/wallet/balance").body<TotalBalanceResponse>()
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

    override suspend fun createAccount(
        name: String,
        phoneNumber: String,
        idNumber: String
    ): Result<CreateAccountResponse> {
        return try {
            val request = CreateAccountRequest(name, phoneNumber, idNumber)
            val response = apiService.createAccount(request)
            // IMPORTANT: If your signup API returns a token, save it here!
            // response.token?.let { sessionStorage.saveAuthToken(it) }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPin(userId: String, pin: String): Result<CreatePinResponse> {
        return try {
            val request = CreatePinRequest(userId, pin)
            val response = apiService.createPin(request)
            // IMPORTANT: If your create-pin API returns a token, save it here!
            // response.token?.let { sessionStorage.saveAuthToken(it) }
            Result.success(response)
        } catch (e: Exception) {
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
                        sessionStorage.saveSession(token = it, userId = user.id)
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
        // On logout, just clear the session storage.
        // The UI will react automatically because it's observing the token flow.
        sessionStorage.clearAuthToken()
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
                    if (token != null) {
                        BearerTokens(accessToken = token, refreshToken = "")
                    } else {
                        null
                    }
                }
            }
        }
    }
}
