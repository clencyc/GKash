package com.example.g_kash.authentication.data

import android.content.SharedPreferences
import android.util.Log
import com.example.g_kash.authentication.domain.AuthRepository
import com.example.g_kash.data.SessionStorage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlin.text.clear

// API Service
interface ApiService {
    suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse
    suspend fun createPin(request: CreatePinRequest): CreatePinResponse
    suspend fun login(request: LoginRequest): LoginResponse
}

class ApiServiceImpl(private val httpClient: HttpClient) : ApiService {
    private val baseUrl = "https://gkash.onrender.com/api"

    override suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse {
        return httpClient.post("$baseUrl/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun createPin(request: CreatePinRequest): CreatePinResponse {
        return httpClient.post("$baseUrl/auth/create-pin") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
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

            if (response.success && response.token != null) {
                // Save the token on successful login
                sessionStorage.saveAuthToken(response.token)
                Log.d("AuthFlow", "Token saved via SessionStorage.")
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
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


// HTTP Client configuration
fun createHttpClient(): HttpClient {
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
    }
}