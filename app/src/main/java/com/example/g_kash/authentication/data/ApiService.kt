package com.example.g_kash.authentication.data

import android.content.SharedPreferences
import android.util.Log
import com.example.g_kash.authentication.domain.AuthRepository
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

// Repository Implementation
class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences
) : AuthRepository {

    private val _authState = MutableStateFlow(AuthState())
    private val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_ID_NUMBER = "user_id_number"
    }

    init {
        // Check if user is already authenticated
        val token = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        if (token != null) {
            val user = getSavedUser()
            _authState.value = AuthState(
                isAuthenticated = true,
                user = user
            )
        }
    }

    override suspend fun createAccount(
        name: String,
        phoneNumber: String,
        idNumber: String
    ): Result<CreateAccountResponse> {
        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val request = CreateAccountRequest(name, phoneNumber, idNumber)
            val response = apiService.createAccount(request)

            _authState.value = _authState.value.copy(isLoading = false)
            Result.success(response)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to create account"
            )
            Result.failure(e)
        }
    }

    override suspend fun createPin(
        userId: String,
        pin: String
    ): Result<CreatePinResponse> {
        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val request = CreatePinRequest(userId, pin)
            val response = apiService.createPin(request)

            _authState.value = _authState.value.copy(isLoading = false)
            Result.success(response)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to create PIN"
            )
            Result.failure(e)
        }
    }

    override suspend fun login(
        phoneNumber: String,
        pin: String
    ): Result<LoginResponse> {
        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val request = LoginRequest(phoneNumber, pin)
            val response = apiService.login(request)

            Log.d("AuthFlow", "Login API Response: success=${response.success}, token=${response.token}, user=${response.user?.id ?: "null"}")


            if (response.success && response.token != null && response.user != null) {
                saveAuthToken(response.token)

                Log.d("AuthFlow", "Token saved to sharedPreferences: ${response.token}")
                saveUser(response.user)

                val user = User(
                    id = response.user.id,
                    name = response.user.name,
                    phoneNumber = response.user.phoneNumber,
                    idNumber = response.user.idNumber
                )

                _authState.value = AuthState(
                    isAuthenticated = true,
                    user = user,
                    isLoading = false
                )
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = response.message
                )
                Log.e("AuthFlow", "Login failed: ${response.message}")
            }

            Result.success(response)
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = e.message ?: "Login failed"
            )
            Result.failure(e)
        }
    }

    override suspend fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()

        Log.d("AuthFlow", "Token saved to sharedPreferences: $KEY_AUTH_TOKEN")
    }

    override suspend fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    override suspend fun clearAuthData() {
        sharedPreferences.edit().clear().apply()
        _authState.value = AuthState()
    }

    override fun getAuthState(): Flow<AuthState> = authState

    private fun saveUser(userData: UserData) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userData.id)
            .putString(KEY_USER_NAME, userData.name)
            .putString(KEY_USER_PHONE, userData.phoneNumber)
            .putString(KEY_USER_ID_NUMBER, userData.idNumber)
            .apply()
    }

    private fun mapUserDataToUser(userData: UserData?): User? {
        return userData?.let {
            User(
                id = it.id,
                name = it.name,
                phoneNumber = it.phoneNumber,
                idNumber = it.idNumber
            )
        }
    }
    private fun getSavedUser(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null)
        val name = sharedPreferences.getString(KEY_USER_NAME, null)
        val phone = sharedPreferences.getString(KEY_USER_PHONE, null)
        val idNumber = sharedPreferences.getString(KEY_USER_ID_NUMBER, null)

        return if (id != null && name != null && phone != null && idNumber != null) {
            User(id, name, phone, idNumber)
        } else null
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